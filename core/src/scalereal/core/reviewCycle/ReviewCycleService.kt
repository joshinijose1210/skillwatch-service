package scalereal.core.reviewCycle

import jakarta.inject.Singleton
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import scalereal.core.designations.DesignationRepository
import scalereal.core.emails.CheckInWithManagerStartedMail
import scalereal.core.emails.ManagerReviewTimelineStartedMail
import scalereal.core.emails.ReviewCycleStartedEmail
import scalereal.core.emails.ReviewCycleUpdatedEmail
import scalereal.core.emails.SelfReviewTimelineStartedMail
import scalereal.core.employees.EmployeeRepository
import scalereal.core.exception.DateException
import scalereal.core.exception.KraKpiAssociationException
import scalereal.core.exception.ReviewCycleException
import scalereal.core.kra.KRARepository
import scalereal.core.models.domain.ActiveReviewCycle
import scalereal.core.models.domain.CheckInWithManagerData
import scalereal.core.models.domain.CheckInWithManagerParams
import scalereal.core.models.domain.ManagerReviewCycleData
import scalereal.core.models.domain.MyManagerReviewCycleData
import scalereal.core.models.domain.ReviewCycle
import scalereal.core.models.domain.ReviewCycleTimeline
import scalereal.core.models.domain.StartedReviewCycle
import scalereal.core.models.domain.UserActivityData
import scalereal.core.modules.ModuleService
import scalereal.core.modules.Modules
import scalereal.core.organisations.OrganisationRepository
import scalereal.core.slack.CheckInSlackNotifications
import scalereal.core.slack.ManagerReviewSlackNotifications
import scalereal.core.slack.ReviewCycleSlackNotifications
import scalereal.core.slack.SelfReviewSlackNotifications
import scalereal.core.userActivity.UserActivityRepository
import java.sql.Date
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Singleton
class ReviewCycleService(
    private val repository: ReviewCycleRepository,
    private val reviewCycleStartedMail: ReviewCycleStartedEmail,
    private val reviewCycleUpdatedMail: ReviewCycleUpdatedEmail,
    private val selfReviewTimelineStartedMail: SelfReviewTimelineStartedMail,
    private val managerReviewTimelineStartedMail: ManagerReviewTimelineStartedMail,
    private val checkInWithManagerStartedMail: CheckInWithManagerStartedMail,
    private val userActivity: UserActivityRepository,
    moduleService: ModuleService,
    private val employeeRepository: EmployeeRepository,
    private val reviewCycleSlackNotifications: ReviewCycleSlackNotifications,
    private val selfReviewSlackNotifications: SelfReviewSlackNotifications,
    private val managerReviewSlackNotifications: ManagerReviewSlackNotifications,
    private val checkInSlackNotifications: CheckInSlackNotifications,
    private val kraRepository: KRARepository,
    private val designationRepository: DesignationRepository,
    private val organisationRepository: OrganisationRepository,
) {
    private val reviewCycleModuleId = moduleService.fetchModuleId(Modules.REVIEW_CYCLE.moduleName)

    @OptIn(DelicateCoroutinesApi::class)
    fun create(
        reviewCycleToCreate: ReviewCycle,
        userActivityData: UserActivityData,
    ) {
        try {
            val organisationTimeZone =
                organisationRepository.getOrganisationDetails(reviewCycleToCreate.organisationId).timeZone
            val reviewCycleWithActiveFlags = reviewCycleToCreate.withActiveFlags(organisationTimeZone)

            validateNoOverlap(reviewCycleWithActiveFlags)

            reviewCycleValidation(reviewCycleWithActiveFlags)

            unPublishPastReviewCycle(reviewCycleWithActiveFlags)

            kraKPIValidation(reviewCycleWithActiveFlags)

            repository.create(reviewCycleWithActiveFlags)

            addActivity(reviewCycleWithActiveFlags, userActivityData, "Created")

            GlobalScope.launch {
                sendReviewCycleMails(reviewCycleWithActiveFlags)
            }
        } catch (e: DateException) {
            throw DateException(e.message.toString())
        } catch (e: Exception) {
            when {
                (e.localizedMessage.contains("no_overlap_org")) ->
                    throw DateException("Review cycle has already been created for the selected range")

                (e.localizedMessage.contains("review_cycle_organisation_id_publish_idx")) ->
                    throw ReviewCycleException("Another Review Cycle is already active.")

                (e.localizedMessage.contains("no_overlap_review_cycle")) ->
                    throw DateException("Review cycle has already been created for the selected range")

                else -> throw e
            }
        }
    }

    fun fetch(
        organisationId: Long,
        page: Int,
        limit: Int,
    ): List<ReviewCycle> {
        val offset: Int = (page - 1) * limit
        val organisationTimeZone = organisationRepository.getOrganisationDetails(organisationId).timeZone
        return repository.fetch(organisationId, offset, limit).map { it.withActiveFlags(organisationTimeZone) }
    }

    fun count(organisationId: Long): Int = repository.count(organisationId)

    @OptIn(DelicateCoroutinesApi::class)
    fun update(
        reviewCycleToUpdate: ReviewCycle,
        userActivityData: UserActivityData,
        notifyEmployees: Boolean,
    ) {
        try {
            val organisationTimeZone = organisationRepository.getOrganisationDetails(reviewCycleToUpdate.organisationId).timeZone
            val reviewCycleOldData = repository.fetchReviewCycle(reviewCycleToUpdate.reviewCycleId)

            validateNoOverlap(reviewCycleToUpdate)

            reviewCycleValidation(reviewCycleToUpdate)

            unPublishPastReviewCycle(reviewCycleToUpdate)

            kraKPIValidation(reviewCycleToUpdate)

            repository.update(reviewCycleToUpdate)

            addActivity(reviewCycleToUpdate, userActivityData, "updated")

            val reviewCycleWithActiveFlags = reviewCycleToUpdate.withActiveFlags(organisationTimeZone)

            if (notifyEmployees) {
                GlobalScope.launch {
                    if (!reviewCycleOldData.publish && reviewCycleToUpdate.publish) {
                        sendReviewCycleMails(reviewCycleWithActiveFlags)
                    } else if (reviewCycleOldData.publish && reviewCycleToUpdate.publish) {
                        sendUpdatedReviewCycleMail(reviewCycleWithActiveFlags)
                    }
                }
            }
        } catch (e: DateException) {
            throw DateException(e.message.toString())
        } catch (e: Exception) {
            when {
                (e.localizedMessage.contains("no_overlap")) ->
                    throw DateException("Review cycle has already been created for the selected range")

                (e.localizedMessage.contains("review_cycle_organisation_id_publish_idx")) ->
                    throw ReviewCycleException("Another Review Cycle is already active.")

                (e.localizedMessage.contains("no_overlap_review_cycle")) ->
                    throw DateException("Review cycle has already been created for the selected range")

                else -> throw e
            }
        }
    }

    private fun validateNoOverlap(reviewCycleToValidate: ReviewCycle) {
        val existingReviewCycles =
            repository
                .fetch(reviewCycleToValidate.organisationId, 0, Int.MAX_VALUE)
                .filter { it.reviewCycleId != reviewCycleToValidate.reviewCycleId }
        val overlaps =
            existingReviewCycles.any { existingRC ->
                reviewCycleToValidate.startDate <= existingRC.startDate && reviewCycleToValidate.endDate >= existingRC.endDate
            }

        if (overlaps) {
            throw DateException("Review cycle has already been created for the selected range")
        }
    }

    private fun reviewCycleValidation(reviewCycle: ReviewCycle) {
        when {
            reviewCycle.endDate < reviewCycle.startDate ->
                throw DateException("End date should be greater than start date")

            reviewCycle.selfReviewEndDate < reviewCycle.selfReviewStartDate ->
                throw DateException("Self review End date should be greater than Self review start date")

            reviewCycle.managerReviewEndDate < reviewCycle.managerReviewStartDate ->
                throw DateException("Manager review End date should be greater than Manager review start date")

            reviewCycle.checkInWithManagerEndDate < reviewCycle.checkInWithManagerStartDate ->
                throw DateException("Check-in End date should be greater than Check-in start date")

            (reviewCycle.selfReviewStartDate !in (reviewCycle.startDate..reviewCycle.endDate)) ||
                (reviewCycle.selfReviewEndDate !in (reviewCycle.startDate..reviewCycle.endDate)) ||
                (reviewCycle.managerReviewStartDate !in (reviewCycle.startDate..reviewCycle.endDate)) ||
                (reviewCycle.managerReviewEndDate !in (reviewCycle.startDate..reviewCycle.endDate)) ->
                throw DateException("Self review and Manager review dates should be in between review cycle dates")

            (reviewCycle.checkInWithManagerStartDate !in (reviewCycle.startDate..reviewCycle.endDate)) ||
                (reviewCycle.checkInWithManagerEndDate !in (reviewCycle.startDate..reviewCycle.endDate)) ->
                throw DateException("Check-in dates should be in between review cycle dates")
        }
    }

    private fun kraKPIValidation(reviewCycle: ReviewCycle) {
        if (reviewCycle.publish) {
            if (!kraRepository.doAllKRAsHaveActiveKPIs(reviewCycle.organisationId)) {
                throw KraKpiAssociationException(
                    "Each KRA must have at least one KPI assigned to it. " +
                        "Please review the KRAs and ensure every KRA has a minimum of one associated KPI to proceed.",
                )
            }
            if (!designationRepository.doAllDesignationsHaveActiveKPIsForEachKRA(reviewCycle.organisationId)) {
                throw KraKpiAssociationException(
                    "Each designation must have at least one KPI for every KRA. Please review and update.",
                )
            }
        }
    }

    private fun sendReviewCycleMails(reviewCycle: ReviewCycle) {
        if (reviewCycle.publish) {
            if (reviewCycle.isReviewCycleActive) {
                reviewCycleStartedMail.sendMail(reviewCycle)
                reviewCycleSlackNotifications.reviewCycleStarted(reviewCycle)
            }

            if (reviewCycle.isSelfReviewActive) {
                selfReviewTimelineStartedMail.sendMail(
                    reviewCycle.selfReviewStartDate,
                    reviewCycle.selfReviewEndDate,
                    reviewCycle.organisationId,
                )
                selfReviewSlackNotifications.selfReviewStarted(reviewCycle.selfReviewEndDate, reviewCycle.organisationId)
            }

            if (reviewCycle.isManagerReviewActive) {
                managerReviewTimelineStartedMail.sendMail(
                    reviewCycle.managerReviewStartDate,
                    reviewCycle.managerReviewEndDate,
                    reviewCycle.organisationId,
                )
                managerReviewSlackNotifications.managerReviewStarted(reviewCycle.managerReviewEndDate, reviewCycle.organisationId)
            }

            if (reviewCycle.isCheckInWithManagerActive) {
                checkInWithManagerStartedMail.sendMailToManager(
                    reviewCycle.checkInWithManagerStartDate,
                    reviewCycle.checkInWithManagerEndDate,
                    reviewCycle.organisationId,
                )
                checkInWithManagerStartedMail.sendMailToEmployee(
                    reviewCycle.checkInWithManagerStartDate,
                    reviewCycle.checkInWithManagerEndDate,
                    reviewCycle.organisationId,
                )
                checkInSlackNotifications.checkInStartedToManager(reviewCycle.checkInWithManagerEndDate, reviewCycle.organisationId)
                checkInSlackNotifications.checkInStartedToEmployee(reviewCycle.checkInWithManagerEndDate, reviewCycle.organisationId)
            }
        }
    }

    private fun sendUpdatedReviewCycleMail(reviewCycle: ReviewCycle) {
        if (reviewCycle.publish) {
            if (reviewCycle.isReviewCycleActive) {
                reviewCycleUpdatedMail.sendMail(reviewCycle)
                reviewCycleSlackNotifications.reviewCycleEdited(reviewCycle)
            }

            if (reviewCycle.isSelfReviewActive) {
                selfReviewTimelineStartedMail.sendMail(
                    reviewCycle.selfReviewStartDate,
                    reviewCycle.selfReviewEndDate,
                    reviewCycle.organisationId,
                )
                selfReviewSlackNotifications.selfReviewStarted(reviewCycle.selfReviewEndDate, reviewCycle.organisationId)
            }

            if (reviewCycle.isManagerReviewActive) {
                managerReviewTimelineStartedMail.sendMail(
                    reviewCycle.managerReviewStartDate,
                    reviewCycle.managerReviewEndDate,
                    reviewCycle.organisationId,
                )
                managerReviewSlackNotifications.managerReviewStarted(reviewCycle.managerReviewEndDate, reviewCycle.organisationId)
            }

            if (reviewCycle.isCheckInWithManagerActive) {
                checkInWithManagerStartedMail.sendMailToManager(
                    reviewCycle.checkInWithManagerStartDate,
                    reviewCycle.checkInWithManagerEndDate,
                    reviewCycle.organisationId,
                )
                checkInWithManagerStartedMail.sendMailToEmployee(
                    reviewCycle.checkInWithManagerStartDate,
                    reviewCycle.checkInWithManagerEndDate,
                    reviewCycle.organisationId,
                )
                checkInSlackNotifications.checkInStartedToManager(reviewCycle.checkInWithManagerEndDate, reviewCycle.organisationId)
                checkInSlackNotifications.checkInStartedToEmployee(reviewCycle.checkInWithManagerEndDate, reviewCycle.organisationId)
            }
        }
    }

    private fun addActivity(
        reviewCycle: ReviewCycle,
        userActivityData: UserActivityData,
        activity: String,
    ) {
        val fullMonthDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
        val description = "${(reviewCycle.startDate).toLocalDate().format(
            fullMonthDateFormatter,
        )} - ${(reviewCycle.endDate).toLocalDate().format(fullMonthDateFormatter)}"
        userActivity.addActivity(
            userActivityData.actionBy,
            reviewCycleModuleId,
            "Review Cycle $activity",
            description,
            userActivityData.ipAddress,
        )
    }

    private fun unPublishPastReviewCycle(reviewCycle: ReviewCycle) {
        val organisationTimeZone = organisationRepository.getOrganisationDetails(reviewCycle.organisationId).timeZone
        val organisationCurrentDate = LocalDate.now(ZoneId.of(organisationTimeZone))

        if (reviewCycle.endDate.toLocalDate() < organisationCurrentDate) {
            reviewCycle.publish = false
        }
    }

    fun fetchSelfReviewCycle(
        organisationId: Long,
        reviewTypeId: List<Int>,
        reviewToId: List<Int>,
        reviewFromId: List<Int>,
        reviewCycleId: List<Int>,
        page: Int,
        limit: Int,
    ): List<ActiveReviewCycle> {
        val offset: Int = (page - 1) * limit
        val organisationTimeZone = organisationRepository.getOrganisationDetails(organisationId).timeZone
        return repository
            .fetchSelfReviewCycle(organisationId, reviewTypeId, reviewToId, reviewFromId, reviewCycleId, offset, limit)
            .map { it.withActiveFlags(organisationTimeZone) }
    }

    fun countSelfReviewCycle(
        organisationId: Long,
        reviewTypeId: List<Int>,
        reviewToId: List<Int>,
        reviewFromId: List<Int>,
        reviewCycleId: List<Int>,
    ): Int = repository.countSelfReviewCycle(organisationId, reviewTypeId, reviewToId, reviewFromId, reviewCycleId)

    fun fetchManagerReviewCycle(
        organisationId: Long,
        reviewTypeId: Int?,
        reviewToId: List<Int>,
        reviewFromId: Long,
        reviewCycleId: List<Int>,
        managerReviewDraft: Boolean?,
        managerReviewPublished: Boolean?,
        page: Int,
        limit: Int,
    ): List<ManagerReviewCycleData> {
        val organisationTimeZone = organisationRepository.getOrganisationDetails(organisationId).timeZone
        return repository
            .fetchManagerReviewCycle(
                organisationId = organisationId,
                reviewTypeId = reviewTypeId,
                reviewToId = reviewToId,
                reviewFromId = reviewFromId,
                reviewCycleId = reviewCycleId,
                managerReviewDraft = managerReviewDraft,
                managerReviewPublished = managerReviewPublished,
                offset = (page - 1) * limit,
                limit = limit,
            ).map { it.withActiveFlags(organisationTimeZone) }
    }

    fun countManagerReviewCycle(
        organisationId: Long,
        reviewTypeId: Int,
        reviewToId: List<Int>,
        reviewFromId: Long,
        reviewCycleId: List<Int>,
        managerReviewDraft: Boolean?,
        managerReviewPublished: Boolean?,
    ): Int =
        repository.countManagerReviewCycle(
            organisationId = organisationId,
            reviewTypeId = reviewTypeId,
            reviewToId = reviewToId,
            reviewFromId = reviewFromId,
            reviewCycleId = reviewCycleId,
            managerReviewDraft = managerReviewDraft,
            managerReviewPublished = managerReviewPublished,
        )

    fun fetchMyManagerReviewCycle(
        organisationId: Long,
        reviewTypeId: Int?,
        reviewToId: Long,
        reviewFromId: List<Int>,
        reviewCycleId: List<Int>,
        page: Int,
        limit: Int,
    ): List<MyManagerReviewCycleData> {
        val offset: Int = (page - 1) * limit
        val organisationTimeZone = organisationRepository.getOrganisationDetails(organisationId).timeZone
        return repository
            .fetchMyManagerReviewCycle(
                organisationId,
                reviewTypeId,
                reviewToId,
                reviewFromId,
                reviewCycleId,
                offset,
                limit,
            ).map { it.withActiveFlags(organisationTimeZone) }
    }

    fun countMyManagerReviewCycle(
        organisationId: Long,
        reviewTypeId: Int,
        reviewToId: Long,
        reviewFromId: List<Int>,
        reviewCycleId: List<Int>,
    ): Int = repository.countMyManagerReviewCycle(organisationId, reviewTypeId, reviewToId, reviewFromId, reviewCycleId)

    fun fetchCheckInWithManager(
        checkInWithManagerParams: CheckInWithManagerParams,
        sortRating: String?,
        page: Int,
        limit: Int,
    ): List<CheckInWithManagerData> {
        val organisationTimeZone = organisationRepository.getOrganisationDetails(checkInWithManagerParams.organisationId).timeZone
        val organisationCurrentDate = LocalDate.now(ZoneId.of(organisationTimeZone))
        val checkInDetails = repository.fetchCheckInWithManager(checkInWithManagerParams.updateRatingsRange())
        val updatedCheckInDetails =
            checkInDetails.map { checkInDetail ->
                val reviewCycle = repository.fetchReviewCycle(checkInDetail.reviewCycleId)
                val currentManagerDetails =
                    employeeRepository.getCurrentManagerDetails(
                        checkInWithManagerParams.organisationId,
                        checkInDetail.reviewToId,
                    )

                when {
                    reviewCycle.endDate.toLocalDate() >= organisationCurrentDate &&
                        checkInDetail.firstManagerId == null &&
                        checkInDetail.secondManagerId == null -> {
                        checkInDetail.copy(
                            firstManagerId = currentManagerDetails.firstManagerId,
                            firstManagerEmployeeId = currentManagerDetails.firstManagerEmployeeId,
                            firstManagerFirstName = currentManagerDetails.firstManagerFirstName,
                            firstManagerLastName = currentManagerDetails.firstManagerLastName,
                            secondManagerId = currentManagerDetails.secondManagerId,
                            secondManagerEmployeeId = currentManagerDetails.secondManagerEmployeeId,
                            secondManagerFirstName = currentManagerDetails.secondManagerFirstName,
                            secondManagerLastName = currentManagerDetails.secondManagerLastName,
                        )
                    }
                    reviewCycle.endDate.toLocalDate() >= organisationCurrentDate &&
                        checkInDetail.firstManagerId != null &&
                        checkInDetail.secondManagerId == null -> {
                        checkInDetail.copy(
                            secondManagerId = currentManagerDetails.secondManagerId,
                            secondManagerEmployeeId = currentManagerDetails.secondManagerEmployeeId,
                            secondManagerFirstName = currentManagerDetails.secondManagerFirstName,
                            secondManagerLastName = currentManagerDetails.secondManagerLastName,
                        )
                    }
                    reviewCycle.endDate.toLocalDate() >= organisationCurrentDate &&
                        checkInDetail.firstManagerId == null &&
                        checkInDetail.secondManagerId != null -> {
                        checkInDetail.copy(
                            firstManagerId = currentManagerDetails.firstManagerId,
                            firstManagerEmployeeId = currentManagerDetails.firstManagerEmployeeId,
                            firstManagerFirstName = currentManagerDetails.firstManagerFirstName,
                            firstManagerLastName = currentManagerDetails.firstManagerLastName,
                        )
                    }
                    else -> checkInDetail
                }
            }
        val sortedData =
            when (sortRating) {
                "asc" -> updatedCheckInDetails.sortedWith(compareBy { it.checkInAverageRating?.abs() })
                "desc" -> updatedCheckInDetails.sortedWith(compareByDescending { it.checkInAverageRating?.abs() })
                else -> updatedCheckInDetails
            }
        val startIndex = (page - 1) * limit
        val endIndex = startIndex + limit
        return sortedData
            .subList(
                startIndex.coerceAtMost(sortedData.size),
                endIndex.coerceAtMost(sortedData.size),
            ).map { it.withActiveFlags(organisationTimeZone) }
    }

    fun countCheckInWithManager(checkInWithManagerParams: CheckInWithManagerParams): Int =
        repository.countCheckInWithManager(checkInWithManagerParams.updateRatingsRange())

    fun fetchReviewCycleData(
        organisationId: Long,
        reviewToId: Long,
    ): List<ReviewCycleTimeline> {
        val organisationTimeZone = organisationRepository.getOrganisationDetails(organisationId).timeZone
        return repository
            .fetchReviewCycleData(
                organisationId,
                reviewToId,
            ).map { it.withActiveFlags(organisationTimeZone) }
    }

    fun fetchReviewCycle(reviewCycleId: Long) = repository.fetchReviewCycle(reviewCycleId)

    fun isReviewCycleStartedAt(
        organisationId: Long,
        organisationCurrentDate: Date,
    ): StartedReviewCycle = repository.isReviewCycleStartedAt(organisationCurrentDate, organisationId)

    fun isSelfReviewStartedAt(
        organisationId: Long,
        organisationCurrentDate: Date,
    ): StartedReviewCycle = repository.isSelfReviewStartedAt(organisationCurrentDate, organisationId)

    fun isManagerReviewStartedAt(
        organisationId: Long,
        organisationCurrentDate: Date,
    ): StartedReviewCycle = repository.isManagerReviewStartedAt(organisationCurrentDate, organisationId)

    fun isCheckInStartedAt(
        organisationId: Long,
        organisationCurrentDate: Date,
    ): StartedReviewCycle = repository.isCheckInStartedAt(organisationCurrentDate, organisationId)

    fun fetchActiveReviewCycle(organisationId: Long): ReviewCycle? {
        val organisationTimeZone = organisationRepository.getOrganisationDetails(organisationId).timeZone

        return repository
            .fetchActiveReviewCycle(
                organisationId,
            )?.withActiveFlags(organisationTimeZone)
    }

    fun unPublishReviewCycle(reviewCycleId: Long) = repository.unPublishReviewCycle(reviewCycleId)

    fun getPreviousReviewCycleId(organisationId: Long) = repository.getPreviousReviewCycleId(organisationId)
}

package scalereal.db.reviewCycle

import jakarta.inject.Inject
import jakarta.inject.Singleton
import norm.command
import norm.query
import reviewCycle.CreateReviewCycleParams
import reviewCycle.CreateReviewCycleQuery
import reviewCycle.GetActiveReviewCycleParams
import reviewCycle.GetActiveReviewCycleQuery
import reviewCycle.GetAllManagerReviewCycleCountParams
import reviewCycle.GetAllManagerReviewCycleCountQuery
import reviewCycle.GetAllManagerReviewCycleParams
import reviewCycle.GetAllManagerReviewCycleQuery
import reviewCycle.GetAllManagerReviewCycleResult
import reviewCycle.GetAllMyManagerReviewCycleCountParams
import reviewCycle.GetAllMyManagerReviewCycleCountQuery
import reviewCycle.GetAllMyManagerReviewCycleParams
import reviewCycle.GetAllMyManagerReviewCycleQuery
import reviewCycle.GetAllMyManagerReviewCycleResult
import reviewCycle.GetAllReviewCycleCountParams
import reviewCycle.GetAllReviewCycleCountQuery
import reviewCycle.GetAllReviewCyclesParams
import reviewCycle.GetAllReviewCyclesQuery
import reviewCycle.GetAllReviewCyclesResult
import reviewCycle.GetAllSelfReviewCycleCountParams
import reviewCycle.GetAllSelfReviewCycleCountQuery
import reviewCycle.GetAllSelfReviewCycleParams
import reviewCycle.GetAllSelfReviewCycleQuery
import reviewCycle.GetAllSelfReviewCycleResult
import reviewCycle.GetAllSummaryReviewCycleCountParams
import reviewCycle.GetAllSummaryReviewCycleCountQuery
import reviewCycle.GetAllSummaryReviewCycleParams
import reviewCycle.GetAllSummaryReviewCycleQuery
import reviewCycle.GetAllSummaryReviewCycleResult
import reviewCycle.GetManagerReviewDataParams
import reviewCycle.GetManagerReviewDataQuery
import reviewCycle.GetPreviousReviewCycleIdParams
import reviewCycle.GetPreviousReviewCycleIdQuery
import reviewCycle.GetPreviousReviewCyclesParams
import reviewCycle.GetPreviousReviewCyclesQuery
import reviewCycle.GetReviewCycleDataParams
import reviewCycle.GetReviewCycleDataQuery
import reviewCycle.GetReviewTimelineDataParams
import reviewCycle.GetReviewTimelineDataQuery
import reviewCycle.GetReviewTimelineDataResult
import reviewCycle.IsCheckInTimelineStartedParams
import reviewCycle.IsCheckInTimelineStartedQuery
import reviewCycle.IsManagerReviewTimelineStartedParams
import reviewCycle.IsManagerReviewTimelineStartedQuery
import reviewCycle.IsReviewCycleStartedParams
import reviewCycle.IsReviewCycleStartedQuery
import reviewCycle.IsReviewSubmissionStartedParams
import reviewCycle.IsReviewSubmissionStartedQuery
import reviewCycle.IsSelfReviewStartedParams
import reviewCycle.IsSelfReviewStartedQuery
import reviewCycle.UnpublishReviewCycleCommand
import reviewCycle.UnpublishReviewCycleParams
import reviewCycle.UpdateReviewCycleParams
import reviewCycle.UpdateReviewCycleQuery
import scalereal.core.models.domain.ActiveReviewCycle
import scalereal.core.models.domain.CheckInWithManagerData
import scalereal.core.models.domain.CheckInWithManagerParams
import scalereal.core.models.domain.EmployeeReviewDetails
import scalereal.core.models.domain.ManagerReviewCycleData
import scalereal.core.models.domain.MyManagerReviewCycleData
import scalereal.core.models.domain.ReviewCycle
import scalereal.core.models.domain.ReviewCycleDates
import scalereal.core.models.domain.ReviewCycleTimeline
import scalereal.core.models.domain.StartedReviewCycle
import scalereal.core.reviewCycle.ReviewCycleRepository
import java.sql.Date
import javax.sql.DataSource

@Singleton
class ReviewCycleRepositoryImpl(
    @Inject private val dataSource: DataSource,
) : ReviewCycleRepository {
    override fun fetch(
        organisationId: Long,
        offset: Int,
        limit: Int,
    ): List<ReviewCycle> =
        dataSource.connection.use { connection ->
            GetAllReviewCyclesQuery()
                .query(connection, GetAllReviewCyclesParams(organisationId = organisationId, offset = offset, limit = limit))
                .map { it.toReviewCycle() }
        }

    override fun count(organisationId: Long): Int =
        dataSource.connection.use { connection ->
            GetAllReviewCycleCountQuery()
                .query(
                    connection,
                    GetAllReviewCycleCountParams(organisationId),
                )[0]
                .reviewCycleCount
                ?.toInt() ?: 0
        }

    private fun GetAllReviewCyclesResult.toReviewCycle() =
        ReviewCycle(
            organisationId = organisationId,
            reviewCycleId = id,
            startDate = startDate,
            endDate = endDate,
            publish = publish,
            lastModified = lastModified,
            selfReviewStartDate = selfReviewStartDate,
            selfReviewEndDate = selfReviewEndDate,
            managerReviewStartDate = managerReviewStartDate,
            managerReviewEndDate = managerReviewEndDate,
            checkInWithManagerStartDate = checkInStartDate,
            checkInWithManagerEndDate = checkInEndDate,
            isSelfReviewDatePassed = false,
            isManagerReviewDatePassed = false,
            isCheckInWithManagerDatePassed = false,
        )

    override fun create(reviewCycle: ReviewCycle): ReviewCycle =
        dataSource.connection
            .use { connection ->
                CreateReviewCycleQuery()
                    .query(
                        connection,
                        CreateReviewCycleParams(
                            start_date = reviewCycle.startDate,
                            end_date = reviewCycle.endDate,
                            publish = reviewCycle.publish,
                            self_review_start_date = reviewCycle.selfReviewStartDate,
                            self_review_end_date = reviewCycle.selfReviewEndDate,
                            manager_review_start_date = reviewCycle.managerReviewStartDate,
                            manager_review_end_date = reviewCycle.managerReviewEndDate,
                            check_in_start_date = reviewCycle.checkInWithManagerStartDate,
                            check_in_end_date = reviewCycle.checkInWithManagerEndDate,
                            organisation_id = reviewCycle.organisationId,
                        ),
                    ).map {
                        ReviewCycle(
                            it.organisationId,
                            it.id,
                            it.startDate,
                            it.endDate,
                            it.publish,
                            it.lastModified,
                            it.selfReviewStartDate,
                            it.selfReviewEndDate,
                            it.managerReviewStartDate,
                            it.managerReviewEndDate,
                            it.checkInStartDate,
                            it.checkInEndDate,
                            isSelfReviewDatePassed = false,
                            isManagerReviewDatePassed = false,
                            isCheckInWithManagerDatePassed = false,
                        )
                    }
            }.first()

    override fun update(reviewCycle: ReviewCycle): ReviewCycle =
        dataSource.connection.use { connection ->
            UpdateReviewCycleQuery()
                .query(
                    connection,
                    UpdateReviewCycleParams(
                        id = reviewCycle.reviewCycleId,
                        start_date = reviewCycle.startDate,
                        end_date = reviewCycle.endDate,
                        publish = reviewCycle.publish,
                        self_review_start_date = reviewCycle.selfReviewStartDate,
                        self_review_end_date = reviewCycle.selfReviewEndDate,
                        manager_review_start_date = reviewCycle.managerReviewStartDate,
                        manager_review_end_date = reviewCycle.managerReviewEndDate,
                        check_in_start_date = reviewCycle.checkInWithManagerStartDate,
                        check_in_end_date = reviewCycle.checkInWithManagerEndDate,
                        organisation_id = reviewCycle.organisationId,
                    ),
                ).map {
                    ReviewCycle(
                        it.organisationId,
                        it.id,
                        it.startDate,
                        it.endDate,
                        it.publish,
                        it.lastModified,
                        it.selfReviewStartDate,
                        it.selfReviewEndDate,
                        it.managerReviewStartDate,
                        it.managerReviewEndDate,
                        it.checkInStartDate,
                        it.checkInEndDate,
                        isSelfReviewDatePassed = false,
                        isManagerReviewDatePassed = false,
                        isCheckInWithManagerDatePassed = false,
                    )
                }.first()
        }

    override fun fetchSelfReviewCycle(
        organisationId: Long,
        reviewTypeId: List<Int>,
        reviewToId: List<Int>,
        reviewFromId: List<Int>,
        reviewCycleId: List<Int>,
        offset: Int,
        limit: Int,
    ): List<ActiveReviewCycle> =
        dataSource.connection.use { connection ->
            GetAllSelfReviewCycleQuery()
                .query(
                    connection,
                    GetAllSelfReviewCycleParams(
                        organisationId = organisationId,
                        reviewTypeId = reviewTypeId.toTypedArray(),
                        reviewToId = reviewToId.toTypedArray(),
                        reviewFromId = reviewFromId.toTypedArray(),
                        reviewCycleId = reviewCycleId.toTypedArray(),
                        offset = offset,
                        limit = limit,
                    ),
                ).map { it.toActiveReviewCycle() }
        }

    override fun countSelfReviewCycle(
        organisationId: Long,
        reviewTypeId: List<Int>,
        reviewToId: List<Int>,
        reviewFromId: List<Int>,
        reviewCycleId: List<Int>,
    ): Int =
        dataSource.connection.use { connection ->
            GetAllSelfReviewCycleCountQuery()
                .query(
                    connection,
                    GetAllSelfReviewCycleCountParams(
                        organisationId = organisationId,
                        reviewTypeId = reviewTypeId.toTypedArray(),
                        reviewToId = reviewToId.toTypedArray(),
                        reviewFromId = reviewFromId.toTypedArray(),
                        reviewCycleId = reviewCycleId.toTypedArray(),
                    ),
                )[0]
                .reviewCycleCount
                ?.toInt() ?: 0
        }

    override fun fetchManagerReviewCycle(
        organisationId: Long,
        reviewTypeId: Int?,
        reviewToId: List<Int>,
        reviewFromId: Long,
        reviewCycleId: List<Int>,
        managerReviewDraft: Boolean?,
        managerReviewPublished: Boolean?,
        offset: Int,
        limit: Int,
    ): List<ManagerReviewCycleData> =
        dataSource.connection.use { connection ->
            GetAllManagerReviewCycleQuery()
                .query(
                    connection,
                    GetAllManagerReviewCycleParams(
                        organisationId = organisationId,
                        reviewTypeId = reviewTypeId,
                        reviewToId = reviewToId.toTypedArray(),
                        reviewFromId = reviewFromId,
                        reviewCycleId = reviewCycleId.toTypedArray(),
                        managerReviewDraft = managerReviewDraft,
                        managerReviewPublished = managerReviewPublished,
                        offset = offset,
                        limit = limit,
                    ),
                ).map { it.toManagerReviewCycle() }
        }

    override fun countManagerReviewCycle(
        organisationId: Long,
        reviewTypeId: Int,
        reviewToId: List<Int>,
        reviewFromId: Long,
        reviewCycleId: List<Int>,
        managerReviewDraft: Boolean?,
        managerReviewPublished: Boolean?,
    ): Int =
        dataSource.connection.use { connection ->
            GetAllManagerReviewCycleCountQuery()
                .query(
                    connection,
                    GetAllManagerReviewCycleCountParams(
                        organisationId = organisationId,
                        reviewTypeId = reviewTypeId,
                        reviewToId = reviewToId.toTypedArray(),
                        reviewFromId = reviewFromId,
                        reviewCycleId = reviewCycleId.toTypedArray(),
                        managerReviewDraft = managerReviewDraft,
                        managerReviewPublished = managerReviewPublished,
                    ),
                )[0]
                .managerReviewCycleCount
                ?.toInt() ?: 0
        }

    override fun fetchMyManagerReviewCycle(
        organisationId: Long,
        reviewTypeId: Int?,
        reviewToId: Long,
        reviewFromId: List<Int>,
        reviewCycleId: List<Int>,
        offset: Int,
        limit: Int,
    ): List<MyManagerReviewCycleData> =
        dataSource.connection.use { connection ->
            GetAllMyManagerReviewCycleQuery()
                .query(
                    connection,
                    GetAllMyManagerReviewCycleParams(
                        organisationId = organisationId,
                        reviewTypeId = reviewTypeId,
                        reviewToId = reviewToId,
                        reviewFromId = reviewFromId.toTypedArray(),
                        reviewCycleId = reviewCycleId.toTypedArray(),
                        offset = offset,
                        limit = limit,
                    ),
                ).map { it.toMyManagerReviewCycle() }
        }

    override fun countMyManagerReviewCycle(
        organisationId: Long,
        reviewTypeId: Int,
        reviewToId: Long,
        reviewFromId: List<Int>,
        reviewCycleId: List<Int>,
    ): Int =
        dataSource.connection.use { connection ->
            GetAllMyManagerReviewCycleCountQuery()
                .query(
                    connection,
                    GetAllMyManagerReviewCycleCountParams(
                        organisationId = organisationId,
                        reviewTypeId = reviewTypeId,
                        reviewToId = reviewToId,
                        reviewFromId = reviewFromId.toTypedArray(),
                        reviewCycleId = reviewCycleId.toTypedArray(),
                    ),
                )[0]
                .managerReviewCycleCount
                ?.toInt() ?: 0
        }

    override fun fetchCheckInWithManager(checkInParams: CheckInWithManagerParams): List<CheckInWithManagerData> =
        dataSource.connection.use { connection ->
            GetAllSummaryReviewCycleQuery()
                .query(
                    connection,
                    GetAllSummaryReviewCycleParams(
                        organisationId = checkInParams.organisationId,
                        managerId = checkInParams.managerId.toTypedArray(),
                        reviewToId = checkInParams.reviewToId.toTypedArray(),
                        reviewCycleId = checkInParams.reviewCycleId.toTypedArray(),
                        selfReviewDraft = checkInParams.selfReviewDraft,
                        selfReviewPublished = checkInParams.selfReviewPublish,
                        firstManagerReviewDraft = checkInParams.firstManagerReviewDraft,
                        firstManagerReviewPublished = checkInParams.firstManagerReviewPublish,
                        secondManagerReviewDraft = checkInParams.secondManagerReviewDraft,
                        secondManagerReviewPublished = checkInParams.secondManagerReviewPublish,
                        checkInDraft = checkInParams.checkInDraft,
                        checkInPublished = checkInParams.checkInPublished,
                        minRange = checkInParams.minFilterRating.toBigDecimal(),
                        maxRange = checkInParams.maxFilterRating.toBigDecimal(),
                    ),
                ).map { it.toCheckInWithManager() }
        }

    override fun countCheckInWithManager(checkInParams: CheckInWithManagerParams): Int =
        dataSource.connection.use { connection ->
            GetAllSummaryReviewCycleCountQuery()
                .query(
                    connection,
                    GetAllSummaryReviewCycleCountParams(
                        organisationId = checkInParams.organisationId,
                        managerId = checkInParams.managerId.toTypedArray(),
                        reviewToId = checkInParams.reviewToId.toTypedArray(),
                        reviewCycleId = checkInParams.reviewCycleId.toTypedArray(),
                        selfReviewDraft = checkInParams.selfReviewDraft,
                        selfReviewPublished = checkInParams.selfReviewPublish,
                        firstManagerReviewDraft = checkInParams.firstManagerReviewDraft,
                        firstManagerReviewPublished = checkInParams.firstManagerReviewPublish,
                        secondManagerReviewDraft = checkInParams.secondManagerReviewDraft,
                        secondManagerReviewPublished = checkInParams.secondManagerReviewPublish,
                        checkInDraft = checkInParams.checkInDraft,
                        checkInPublished = checkInParams.checkInPublished,
                        minRange = checkInParams.minFilterRating.toBigDecimal(),
                        maxRange = checkInParams.maxFilterRating.toBigDecimal(),
                    ),
                )[0]
                .summaryReviewCount
                ?.toInt() ?: 0
        }

    override fun fetchReviewCycleData(
        organisationId: Long,
        reviewToId: Long,
    ): List<ReviewCycleTimeline> =
        dataSource.connection.use { connection ->
            GetReviewTimelineDataQuery()
                .query(connection, GetReviewTimelineDataParams(organisationId = organisationId, reviewToId = reviewToId))
                .map { it.toReviewCycleTimeline(reviewToId) }
        }

    override fun fetchEmployeeReview(
        organisationId: Long,
        reviewCycleId: Long,
        reviewFromId: Long,
    ): List<EmployeeReviewDetails> =
        dataSource.connection.use { connection ->
            GetManagerReviewDataQuery()
                .query(
                    connection,
                    GetManagerReviewDataParams(
                        organisationId = organisationId,
                        reviewCycleId = reviewCycleId,
                        reviewFromId = reviewFromId,
                    ),
                ).map {
                    EmployeeReviewDetails(
                        id = it.id,
                        employeeId = it.employeeId,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        checkInFromId = it.checkInFromId,
                        firstManagerId = it.firstManagerId,
                        secondManagerId = it.secondManagerId,
                        selfReviewDraft = it.selfReviewDraft ?: false,
                        selfReviewPublish = it.selfReviewPublish ?: false,
                        selfReviewDate = it.selfReviewDate,
                        firstManagerReviewDraft = it.firstManagerReviewDraft ?: false,
                        firstManagerReviewPublish = it.firstManagerReviewPublished ?: false,
                        firstManagerReviewDate = it.firstManagerReviewDate,
                        secondManagerReviewDraft = it.secondManagerReviewDraft ?: false,
                        secondManagerReviewPublish = it.secondManagerReviewPublished ?: false,
                        secondManagerReviewDate = it.secondManagerReviewDate,
                        checkInFromEmployeeId = it.checkInFromEmployeeId,
                        checkInFromFirstName = it.checkInFromFirstName,
                        checkInFromLastName = it.checkInFromLastName,
                        checkInWithManagerDraft = it.checkInWithManagerDraft ?: false,
                        checkInWithManagerPublish = it.checkInWithManagerPublish ?: false,
                        checkInWithManagerDate = it.checkInWithManagerDate,
                    )
                }
        }

    override fun fetchReviewCycle(reviewCycleId: Long): ReviewCycle =
        dataSource.connection.use { connection ->
            GetReviewCycleDataQuery()
                .query(connection, GetReviewCycleDataParams(reviewCycleId = reviewCycleId))
                .map {
                    ReviewCycle(
                        organisationId = it.organisationId,
                        reviewCycleId = it.id,
                        startDate = it.startDate,
                        endDate = it.endDate,
                        publish = it.publish,
                        lastModified = it.lastModified,
                        selfReviewStartDate = it.selfReviewStartDate,
                        selfReviewEndDate = it.selfReviewEndDate,
                        managerReviewStartDate = it.managerReviewStartDate,
                        managerReviewEndDate = it.managerReviewEndDate,
                        checkInWithManagerStartDate = it.checkInStartDate,
                        checkInWithManagerEndDate = it.checkInEndDate,
                        isSelfReviewDatePassed = false,
                        isManagerReviewDatePassed = false,
                        isCheckInWithManagerDatePassed = false,
                    )
                }.first()
        }

    override fun isReviewCycleStartedAt(
        date: Date,
        organisationId: Long,
    ): StartedReviewCycle =
        dataSource.connection.use { connection ->
            IsReviewCycleStartedQuery()
                .query(connection, IsReviewCycleStartedParams(todayDate = date, organisationId))
                .map {
                    StartedReviewCycle(
                        exists = it.exists ?: false,
                        id = it.id,
                    )
                }.first()
        }

    override fun isSelfReviewStartedAt(
        date: Date,
        organisationId: Long,
    ): StartedReviewCycle =
        dataSource.connection.use { connection ->
            IsSelfReviewStartedQuery()
                .query(connection, IsSelfReviewStartedParams(todayDate = date, organisationId))
                .map {
                    StartedReviewCycle(
                        exists = it.exists ?: false,
                        id = it.id,
                    )
                }.first()
        }

    override fun isManagerReviewStartedAt(
        date: Date?,
        organisationId: Long,
    ): StartedReviewCycle =
        dataSource.connection.use { connection ->
            IsManagerReviewTimelineStartedQuery()
                .query(connection, IsManagerReviewTimelineStartedParams(todayDate = date, organisationId))
                .map {
                    StartedReviewCycle(
                        exists = it.exists ?: false,
                        id = it.id,
                    )
                }.first()
        }

    override fun isCheckInStartedAt(
        date: Date?,
        organisationId: Long,
    ): StartedReviewCycle =
        dataSource.connection.use { connection ->
            IsCheckInTimelineStartedQuery()
                .query(connection, IsCheckInTimelineStartedParams(todayDate = date, organisationId))
                .map {
                    StartedReviewCycle(
                        exists = it.exists ?: false,
                        id = it.id,
                    )
                }.first()
        }

    override fun fetchActiveReviewCycle(organisationId: Long): ReviewCycle? =
        dataSource.connection.use { connection ->
            GetActiveReviewCycleQuery()
                .query(connection, GetActiveReviewCycleParams(organisationId))
                .map {
                    ReviewCycle(
                        organisationId = it.organisationId,
                        reviewCycleId = it.id,
                        startDate = it.startDate,
                        endDate = it.endDate,
                        publish = it.publish,
                        lastModified = it.lastModified,
                        selfReviewStartDate = it.selfReviewStartDate,
                        selfReviewEndDate = it.selfReviewEndDate,
                        managerReviewStartDate = it.managerReviewStartDate,
                        managerReviewEndDate = it.managerReviewEndDate,
                        checkInWithManagerStartDate = it.checkInStartDate,
                        checkInWithManagerEndDate = it.checkInEndDate,
                        isSelfReviewDatePassed = false,
                        isManagerReviewDatePassed = false,
                        isCheckInWithManagerDatePassed = false,
                    )
                }.firstOrNull()
        }

    override fun unPublishReviewCycle(reviewCycleId: Long) =
        dataSource.connection.use { connection ->
            UnpublishReviewCycleCommand()
                .command(connection, UnpublishReviewCycleParams(publish = false, id = reviewCycleId))
        }

    override fun isReviewSubmissionStarted(
        organisationId: Long,
        date: Date,
    ): Boolean =
        dataSource.connection.use { connection ->
            IsReviewSubmissionStartedQuery()
                .query(
                    connection,
                    IsReviewSubmissionStartedParams(organisationId = organisationId, currentDate = date),
                ).map { it.exists ?: false }
                .first()
        }

    override fun getPreviousReviewCycleId(organisationId: Long): List<Long> =
        dataSource.connection.use { connection ->
            GetPreviousReviewCycleIdQuery()
                .query(connection, GetPreviousReviewCycleIdParams(organisationId))
                .map { it.id }
        }

    override fun getReviewCycles(
        organisationId: Long,
        reviewCycleId: Long,
        numberOfCycles: Long,
    ): List<ReviewCycleDates> =
        dataSource.connection.use { connection ->
            GetPreviousReviewCyclesQuery()
                .query(
                    connection,
                    GetPreviousReviewCyclesParams(
                        organisationId = organisationId,
                        currentCycleId = reviewCycleId,
                        numberOfCycles = numberOfCycles,
                    ),
                ).map { ReviewCycleDates(it.id, it.startDate, it.endDate) }
        }

    private fun GetReviewTimelineDataResult.toReviewCycleTimeline(reviewToId: Long) =
        ReviewCycleTimeline(
            organisationId = organisationId,
            reviewCycleId = id,
            startDate = startDate,
            endDate = endDate,
            publish = publish,
            selfReviewStartDate = selfReviewStartDate,
            selfReviewEndDate = selfReviewEndDate,
            managerReviewStartDate = managerReviewStartDate,
            managerReviewEndDate = managerReviewEndDate,
            checkInWithManagerStartDate = checkInStartDate,
            checkInWithManagerEndDate = checkInEndDate,
            selfReviewDraft = selfReviewDraft ?: false,
            selfReviewPublish = selfReviewPublish ?: false,
            selfReviewDate = selfReviewDate,
            firstManagerId = firstManagerId,
            firstManagerEmployeeId = firstManagerEmployeeId,
            firstManagerFirstName = firstManagerFirstName,
            firstManagerLastName = firstManagerLastName,
            firstManagerReviewDraft = firstManagerReviewDraft ?: false,
            firstManagerReviewPublish = firstManagerReviewPublish ?: false,
            firstManagerReviewDate = firstManagerReviewDate,
            secondManagerId = secondManagerId,
            secondManagerEmployeeId = secondManagerEmployeeId,
            secondManagerFirstName = secondManagerFirstname,
            secondManagerLastName = secondManagerLastName,
            secondManagerReviewDraft = secondManagerReviewDraft ?: false,
            secondManagerReviewPublish = secondManagerReviewPublish ?: false,
            secondManagerReviewDate = secondManagerReviewDate,
            checkInFromId = checkInFromId,
            checkInFromEmployeeId = checkInFromEmployeeId,
            checkInFromFirstName = checkInFromFirstName,
            checkInFromLastName = checkInFromLastName,
            checkInWithManagerDraft = checkInWithManagerDraft ?: false,
            checkInWithManagerPublish = checkInWithManagerPublish ?: false,
            checkInWithManagerDate = checkInWithManagerDate,
            empDetails =
                fetchEmployeeReview(
                    organisationId = organisationId,
                    reviewCycleId = id,
                    reviewFromId = reviewToId,
                ),
            selfAverageRating = selfAverageRating ?: (-1.00).toBigDecimal(),
            firstManagerAverageRating = firstManagerAverageRating ?: (-1.00).toBigDecimal(),
            secondManagerAverageRating = secondManagerAverageRating ?: (-1.00).toBigDecimal(),
            checkInWithManagerAverageRating = checkInWithManagerAverageRating ?: (-1.00).toBigDecimal(),
            isOrWasManager = isOrWasManager,
            isSelfReviewDatePassed = false,
            isManagerReviewDatePassed = false,
            isCheckInWithManagerDatePassed = false,
        )

    private fun GetAllSelfReviewCycleResult.toActiveReviewCycle() =
        ActiveReviewCycle(
            reviewCycleId = id,
            startDate = startDate,
            endDate = endDate,
            selfReviewStartDate = selfReviewStartDate,
            selfReviewEndDate = selfReviewEndDate,
            draft = draft,
            publish = published,
            updatedAt = updatedAt,
            averageRating = averageRating ?: (-1.00).toBigDecimal(),
            isReviewCyclePublish = publish,
            isSelfReviewDatePassed = false,
        )

    private fun GetAllManagerReviewCycleResult.toManagerReviewCycle() =
        ManagerReviewCycleData(
            reviewCycleId = reviewCycleId,
            startDate = startDate,
            endDate = endDate,
            managerReviewStartDate = managerReviewStartDate,
            managerReviewEndDate = managerReviewEndDate,
            team = teamName,
            reviewToId = reviewToId,
            reviewToEmployeeId = reviewToEmployeeId,
            firstName = firstName,
            lastName = lastName,
            draft = draft,
            publish = published,
            averageRating = averageRating ?: (-1.00).toBigDecimal(),
            isReviewCyclePublish = publish,
            isManagerReviewDatePassed = false,
        )

    private fun GetAllMyManagerReviewCycleResult.toMyManagerReviewCycle() =
        MyManagerReviewCycleData(
            reviewCycleId = reviewCycleId,
            startDate = startDate,
            endDate = endDate,
            managerReviewStartDate = managerReviewStartDate,
            managerReviewEndDate = managerReviewEndDate,
            team = teamName,
            reviewToId = reviewToId,
            reviewToEmployeeId = reviewToEmployeeId,
            firstName = firstName,
            lastName = lastName,
            reviewFromId = reviewFromId,
            reviewFromEmployeeId = reviewFromEmployeeId,
            managerFirstName = managerFirstName,
            managerLastName = managerLastName,
            draft = draft,
            publish = published,
            isReviewCyclePublish = publish,
            averageRating = averageRating ?: (-1.00).toBigDecimal(),
            isManagerReviewDatePassed = false,
        )

    private fun GetAllSummaryReviewCycleResult.toCheckInWithManager() =
        CheckInWithManagerData(
            reviewCycleId = reviewCycleId,
            startDate = startDate,
            endDate = endDate,
            publish = publish,
            checkInStartDate = checkInStartDate,
            checkInEndDate = checkInEndDate,
            reviewToId = reviewToId,
            reviewToEmployeeId = reviewToEmployeeId,
            firstName = firstName,
            lastName = lastName,
            selfReviewDraft = selfReviewDraft ?: false,
            selfReviewPublish = selfReviewPublish ?: false,
            selfAverageRating = selfAverageRating ?: (-1.00).toBigDecimal(),
            firstManagerReviewDraft = firstManagerReviewDraft ?: false,
            firstManagerReviewPublish = firstManagerReviewPublish ?: false,
            firstManagerAverageRating = firstManagerAverageRating ?: (-1.00).toBigDecimal(),
            secondManagerReviewDraft = secondManagerReviewDraft ?: false,
            secondManagerReviewPublish = secondManagerReviewPublish ?: false,
            secondManagerAverageRating = secondManagerAverageRating ?: (-1.00).toBigDecimal(),
            checkInFromId = checkInFromId,
            checkInDraft = checkInDraft ?: false,
            checkInPublish = checkInPublish ?: false,
            checkInAverageRating = checkInAverageRating ?: (-1.00).toBigDecimal(),
            firstManagerId = firstManagerId,
            firstManagerEmployeeId = firstManagerEmployeeId,
            firstManagerFirstName = firstManagerFirstName,
            firstManagerLastName = firstManagerLastName,
            secondManagerId = secondManagerId,
            secondManagerEmployeeId = secondManagerEmployeeId,
            secondManagerFirstName = secondManagerFirstName,
            secondManagerLastName = secondManagerLastName,
            isCheckInWithManagerDatePassed = false,
        )
}

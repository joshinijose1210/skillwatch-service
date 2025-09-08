package scalereal.core.cronJob

import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import scalereal.core.emails.CheckInWithManagerReminderEmail
import scalereal.core.emails.CheckInWithManagerStartedMail
import scalereal.core.emails.FeedbackRequestReminderMail
import scalereal.core.emails.GoalCompletedMail
import scalereal.core.emails.ManagerReviewReminderMail
import scalereal.core.emails.ManagerReviewTimelineStartedMail
import scalereal.core.emails.ReviewCycleStartedEmail
import scalereal.core.emails.ReviewCycleUnpublishedMail
import scalereal.core.emails.SelfReviewReminderMail
import scalereal.core.emails.SelfReviewTimelineStartedMail
import scalereal.core.employees.EmployeeRepository
import scalereal.core.employees.EmployeeService
import scalereal.core.feedbacks.FeedbackRequestService
import scalereal.core.models.FeedbackReminder
import scalereal.core.organisations.OrganisationService
import scalereal.core.review.SelfReviewService
import scalereal.core.reviewCycle.ReviewCycleService
import scalereal.core.slack.CheckInSlackNotifications
import scalereal.core.slack.FeedbackRequestReminder
import scalereal.core.slack.GoalCompletedNotification
import scalereal.core.slack.ManagerReviewSlackNotifications
import scalereal.core.slack.ReviewCycleSlackNotifications
import scalereal.core.slack.SelfReviewSlackNotifications
import scalereal.core.slack.SlackService
import java.sql.Date
import java.sql.Timestamp
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Singleton
class CronJobService(
    private val reviewCycleService: ReviewCycleService,
    private val reviewCycleStartedMail: ReviewCycleStartedEmail,
    private val selfReviewTimelineStartedMail: SelfReviewTimelineStartedMail,
    private val managerReviewTimelineStartedMail: ManagerReviewTimelineStartedMail,
    private val checkInWithManagerStartedMail: CheckInWithManagerStartedMail,
    private val reviewService: SelfReviewService,
    private val selfReviewReminderMail: SelfReviewReminderMail,
    private val employeeService: EmployeeService,
    private val managerReviewReminderMail: ManagerReviewReminderMail,
    private val organisationService: OrganisationService,
    private val checkInWithManagerReminderEmail: CheckInWithManagerReminderEmail,
    private val reviewCycleUnpublishedMail: ReviewCycleUnpublishedMail,
    private val employeeRepository: EmployeeRepository,
    private val reviewCycleSlackNotifications: ReviewCycleSlackNotifications,
    private val selfReviewSlackNotifications: SelfReviewSlackNotifications,
    private val managerReviewSlackNotifications: ManagerReviewSlackNotifications,
    private val checkInSlackNotifications: CheckInSlackNotifications,
    private val goalCompletedMail: GoalCompletedMail,
    private val goalCompletedSlackNotification: GoalCompletedNotification,
    private val feedbackRequestReminderMail: FeedbackRequestReminderMail,
    private val feedbackRequestService: FeedbackRequestService,
    private val feedbackRequestReminderSlackNotification: FeedbackRequestReminder,
    private val slackService: SlackService,
) {
    @Scheduled(cron = "0 */15 * * * *")
    @ExecuteOn(TaskExecutors.IO)
    fun taskScheduled() {
        sendReviewCycleStartedMail()
        sendSelfReviewStartedMail()
        sendManagerReviewStartedMail()
        sendCheckInStartedMail()
        sendSelfReviewReminderMail()
        sendManagerReviewReminderMail()
        sendCheckInWithManagerReminderMail()
    }

    fun sendReviewCycleStartedMail() {
        val organisations = organisationService.fetchAllOrganisations()
        organisations.forEach { organisation ->
            val zoneId = ZoneId.of(organisation.timeZone)

            if (!justCrossedTargetHour(zoneId)) return@forEach

            val organisationCurrentDate = Date.valueOf(LocalDate.now(ZoneId.of(organisation.timeZone)))
            val startedReviewCycle = reviewCycleService.isReviewCycleStartedAt(organisation.id, organisationCurrentDate)

            if (startedReviewCycle.exists && startedReviewCycle.id != null) {
                val reviewCycle = reviewCycleService.fetchReviewCycle(startedReviewCycle.id)
                reviewCycleStartedMail.sendMail(reviewCycle)
                reviewCycleSlackNotifications.reviewCycleStarted(reviewCycle)
            }
        }
    }

    fun sendSelfReviewStartedMail() {
        val organisations = organisationService.fetchAllOrganisations()
        organisations.forEach { organisation ->
            val zoneId = ZoneId.of(organisation.timeZone)

            if (!justCrossedTargetHour(zoneId)) return@forEach

            val organisationCurrentDate = Date.valueOf(LocalDate.now(ZoneId.of(organisation.timeZone)))
            val startedSelfReview = reviewCycleService.isSelfReviewStartedAt(organisation.id, organisationCurrentDate)

            if (startedSelfReview.exists && startedSelfReview.id != null) {
                val reviewCycle = reviewCycleService.fetchReviewCycle(startedSelfReview.id)
                selfReviewTimelineStartedMail.sendMail(
                    reviewCycle.selfReviewStartDate,
                    reviewCycle.selfReviewEndDate,
                    reviewCycle.organisationId,
                )
                selfReviewSlackNotifications.selfReviewStarted(reviewCycle.selfReviewEndDate, reviewCycle.organisationId)
            }
        }
    }

    fun sendManagerReviewStartedMail() {
        val organisations = organisationService.fetchAllOrganisations()
        organisations.forEach { organisation ->
            val zoneId = ZoneId.of(organisation.timeZone)

            if (!justCrossedTargetHour(zoneId)) return@forEach

            val organisationCurrentDate = Date.valueOf(LocalDate.now(ZoneId.of(organisation.timeZone)))
            val startedManagerReview = reviewCycleService.isManagerReviewStartedAt(organisation.id, organisationCurrentDate)

            if (startedManagerReview.exists && startedManagerReview.id != null) {
                val reviewCycle = reviewCycleService.fetchReviewCycle(startedManagerReview.id)
                managerReviewTimelineStartedMail.sendMail(
                    reviewCycle.managerReviewStartDate,
                    reviewCycle.managerReviewEndDate,
                    reviewCycle.organisationId,
                )
                managerReviewSlackNotifications.managerReviewStarted(reviewCycle.managerReviewEndDate, reviewCycle.organisationId)
            }
        }
    }

    fun sendCheckInStartedMail() {
        val organisations = organisationService.fetchAllOrganisations()
        organisations.forEach { organisation ->
            val zoneId = ZoneId.of(organisation.timeZone)

            if (!justCrossedTargetHour(zoneId)) return@forEach

            val organisationCurrentDate = Date.valueOf(LocalDate.now(ZoneId.of(organisation.timeZone)))
            val startedCheckIn = reviewCycleService.isCheckInStartedAt(organisation.id, organisationCurrentDate)

            if (startedCheckIn.exists && startedCheckIn.id != null) {
                val reviewCycle = reviewCycleService.fetchReviewCycle(startedCheckIn.id)
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
                checkInSlackNotifications.checkInStartedToManager(
                    reviewCycle.checkInWithManagerEndDate,
                    reviewCycle.organisationId,
                )
                checkInSlackNotifications.checkInStartedToEmployee(
                    reviewCycle.checkInWithManagerEndDate,
                    reviewCycle.organisationId,
                )
            }
        }
    }

    fun sendSelfReviewReminderMail() {
        val organisations = organisationService.fetchAllOrganisations()
        organisations.forEach { organisation ->
            val zoneId = ZoneId.of(organisation.timeZone)

            if (!justCrossedTargetHour(zoneId)) return@forEach

            val reviewCycle =
                reviewCycleService
                    .fetchActiveReviewCycle(organisation.id)
                    ?.withActiveFlags(organisation.timeZone) ?: return@forEach
            val incompleteSelfReviews = reviewService.fetchInCompleteSelfReviewsEmployees(reviewCycle.reviewCycleId, organisation.id)

            val organisationCurrentDate = LocalDate.now(ZoneId.of(organisation.timeZone))
            val selfReviewEndDate = reviewCycle.selfReviewEndDate.toLocalDate()

            if (!reviewCycle.isSelfReviewActive) {
                return@forEach
            }

            val emailIds = incompleteSelfReviews.map { it.emailId }

            when (organisationCurrentDate) {
                selfReviewEndDate.minusDays(5) -> {
                    if (incompleteSelfReviews.isNotEmpty()) {
                        selfReviewReminderMail.sendFiveDayReminderEmail(emailIds, reviewCycle.selfReviewEndDate, reviewCycle.organisationId)
                        selfReviewSlackNotifications.selfReviewFiveDayReminder(
                            emailIds,
                            reviewCycle.selfReviewEndDate,
                            reviewCycle.organisationId,
                        )
                    }
                }

                selfReviewEndDate -> {
                    if (incompleteSelfReviews.isNotEmpty()) {
                        selfReviewReminderMail.sendLastDayReminderEmail(emailIds, reviewCycle.organisationId)
                        selfReviewSlackNotifications.selfReviewLastDayReminder(emailIds, reviewCycle.organisationId)
                    }
                }
            }
        }
    }

    fun sendManagerReviewReminderMail() {
        val organisations = organisationService.fetchAllOrganisations()
        organisations.forEach { organisation ->
            val zoneId = ZoneId.of(organisation.timeZone)

            if (!justCrossedTargetHour(zoneId)) return@forEach

            val reviewCycle =
                reviewCycleService
                    .fetchActiveReviewCycle(organisation.id)
                    ?.withActiveFlags(organisation.timeZone) ?: return@forEach

            val organisationCurrentDate = LocalDate.now(ZoneId.of(organisation.timeZone))
            val managerReviewEndDate = reviewCycle.managerReviewEndDate.toLocalDate()

            if (!reviewCycle.isManagerReviewActive) {
                return@forEach
            }
            val managerList = employeeService.getManagers(reviewCycle.organisationId)

            when (organisationCurrentDate) {
                managerReviewEndDate.minusDays(5) -> {
                    managerReviewReminderMail.sendFiveDayReminderEmail(
                        managerList,
                        reviewCycle.managerReviewEndDate,
                        reviewCycle.reviewCycleId,
                    )
                    managerReviewSlackNotifications.managerReviewFiveDayReminder(
                        managerList,
                        reviewCycle.managerReviewEndDate,
                        reviewCycle.reviewCycleId,
                    )
                }

                managerReviewEndDate -> {
                    managerReviewReminderMail.sendLastDayReminderEmail(managerList, reviewCycle.reviewCycleId)
                    managerReviewSlackNotifications.managerReviewLastDayReminder(managerList, reviewCycle.reviewCycleId)
                }
            }
        }
    }

    fun sendCheckInWithManagerReminderMail() {
        val organisations = organisationService.fetchAllOrganisations()
        organisations.forEach { organisation ->
            val zoneId = ZoneId.of(organisation.timeZone)

            if (!justCrossedTargetHour(zoneId)) return@forEach

            val reviewCycle =
                reviewCycleService
                    .fetchActiveReviewCycle(organisation.id)
                    ?.withActiveFlags(organisation.timeZone) ?: return@forEach

            val organisationCurrentDate = LocalDate.now(ZoneId.of(organisation.timeZone))
            val checkInEndDate = reviewCycle.checkInWithManagerEndDate.toLocalDate()

            if (!reviewCycle.isCheckInWithManagerActive) {
                return@forEach
            }
            val managerList = employeeService.getManagers(reviewCycle.organisationId)

            when (organisationCurrentDate) {
                checkInEndDate.minusDays(5) -> {
                    checkInWithManagerReminderEmail.sendFiveDayReminderEmailToManager(
                        managerList,
                        reviewCycle.checkInWithManagerEndDate,
                        reviewCycle.reviewCycleId,
                    )
                    checkInWithManagerReminderEmail.sendFiveDayReminderEmailToEmployee(
                        managerList,
                        reviewCycle.checkInWithManagerEndDate,
                        reviewCycle.reviewCycleId,
                    )
                    checkInSlackNotifications.checkInFiveDayReminderToManager(
                        managerList,
                        reviewCycle.checkInWithManagerEndDate,
                        reviewCycle.reviewCycleId,
                    )
                    checkInSlackNotifications.checkInFiveDayReminderToEmployee(
                        managerList,
                        reviewCycle.checkInWithManagerEndDate,
                        reviewCycle.reviewCycleId,
                    )
                }

                checkInEndDate -> {
                    checkInWithManagerReminderEmail.sendLastDayReminderEmailToManager(managerList, reviewCycle.reviewCycleId)
                    checkInWithManagerReminderEmail.sendLastDayReminderEmailToEmployee(
                        managerList,
                        reviewCycle.reviewCycleId,
                        reviewCycle.startDate,
                        reviewCycle.endDate,
                    )
                    checkInSlackNotifications.checkInLastDayReminderToManager(managerList, reviewCycle.reviewCycleId)
                    checkInSlackNotifications.checkInLastDayReminderToEmployee(
                        managerList,
                        reviewCycle.reviewCycleId,
                        reviewCycle.startDate,
                        reviewCycle.endDate,
                    )
                }
            }
        }
    }

    private fun justCrossedTargetHour(zoneId: ZoneId): Boolean {
        val targetHour = 10 // targeting to send at 10 AM
        val now = Instant.now()
        val nowInOrganisationTZ = now.atZone(zoneId)
        val fifteenMinutesAgoInOrganisationTZ = now.minus(15, ChronoUnit.MINUTES).atZone(zoneId)

        return nowInOrganisationTZ.hour == targetHour && fifteenMinutesAgoInOrganisationTZ.hour < targetHour
    }

    @Scheduled(cron = "0 */15 * * * *")
    @ExecuteOn(TaskExecutors.IO)
    fun unPublishReviewCycle() {
        val organisations = organisationService.fetchAllOrganisations()
        organisations.forEach { organisation ->
            val organisationCurrentDate = LocalDate.now(ZoneId.of(organisation.timeZone))
            val reviewCycle = reviewCycleService.fetchActiveReviewCycle(organisation.id) ?: return@forEach
            val reviewCycleEndDate = reviewCycle.endDate.toLocalDate()

            if (organisationCurrentDate.isAfter(reviewCycleEndDate)) {
                reviewCycleService.unPublishReviewCycle(reviewCycle.reviewCycleId)
                reviewCycleUnpublishedMail.sendMail(reviewCycle, organisation.id)
                reviewCycleSlackNotifications.reviewCycleEnded(reviewCycle, organisation.id)
            }
        }
    }

    @Scheduled(cron = "0 0 0 1 * *")
    fun increaseEmployeesExperience() {
        employeeRepository.increaseEmployeesExperience()
    }

    @Scheduled(cron = "0 0 0 * * *", zoneId = "Asia/Kolkata")
    fun sendGoalDeadlineNotifications() {
        val goalsWithPastDeadline = reviewService.fetchGoalsWithPastDeadline()
        goalsWithPastDeadline.map { goal ->
            goalCompletedMail.sendMail(goal)
            goalCompletedSlackNotification.sendNotification(goal)
        }
    }

    @Scheduled(cron = "0 */15 * * * *")
    @ExecuteOn(TaskExecutors.IO)
    fun sendFeedbackRequestReminder() {
        val feedbackRequestDetails = feedbackRequestService.getPendingFeedbackRequest()
        if (feedbackRequestDetails.isNotEmpty()) {
            feedbackRequestDetails.map { feedbackRequestDetail ->
                val nowInOrganisationZone = ZonedDateTime.now(ZoneId.of(feedbackRequestDetail.organisationTimeZone))

                if (nowInOrganisationZone.hour == 0 && nowInOrganisationZone.minute < 15) {
                    if (shouldSendFeedbackRequestReminder(feedbackRequestDetail.date, feedbackRequestDetail.organisationTimeZone)) {
                        feedbackRequestReminderMail.sendMail(feedbackRequestDetail)
                        feedbackRequestDetail.feedbackFromId?.let {
                            feedbackRequestReminderSlackNotification.sendNotification(
                                requestedById = feedbackRequestDetail.requestedById,
                                feedbackFromId = it,
                                date = feedbackRequestDetail.date,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun shouldSendFeedbackRequestReminder(
        date: Date,
        organisationTimeZone: String,
    ): Boolean {
        val feedbackReqDateInOrgTZ = date.toLocalDate()
        val organisationCurrentDate = LocalDate.now(ZoneId.of(organisationTimeZone))

        val daysSinceCreation = ChronoUnit.DAYS.between(feedbackReqDateInOrgTZ, organisationCurrentDate)
        return daysSinceCreation in 7..21 && daysSinceCreation % 7 == 0L
    }

    @Scheduled(cron = "0 */15 * * * *")
    @ExecuteOn(TaskExecutors.IO)
    fun sendBiWeeklyFeedbackReminder() {
        val now = Instant.now()
        val organisations = organisationService.fetchAllOrganisations()

        organisations.forEach { organisation ->
            val zoneId = ZoneId.of(organisation.timeZone)
            val todayInOrganisationTZ = LocalDate.now(zoneId)
            val nowInOrgZone = ZonedDateTime.now(zoneId)
            val fifteenMinutesAgoInOrgZone = now.minus(15, ChronoUnit.MINUTES).atZone(zoneId)

            // Only send at Monday 12:00 PM (organisation's time)
            val justCrossedMondayNoon =
                nowInOrgZone.dayOfWeek == DayOfWeek.MONDAY &&
                    nowInOrgZone.hour == 12 &&
                    (fifteenMinutesAgoInOrgZone.hour < 12 || fifteenMinutesAgoInOrgZone.dayOfWeek != DayOfWeek.MONDAY)

            if (!justCrossedMondayNoon) return@forEach

            val schedule = organisationService.getFeedbackReminderSchedule(organisation.id)
            if (!schedule.isBiWeeklyFeedbackReminderEnabled) return@forEach

            val lastSent =
                schedule.lastFeedbackReminderSentDate
                    ?.toInstant()
                    ?.atZone(zoneId)
                    ?.toLocalDate()
            val shouldSend = lastSent == null || ChronoUnit.DAYS.between(lastSent, todayInOrganisationTZ) >= 14

            if (!shouldSend) return@forEach

            val reminderValues = FeedbackReminder.entries.toTypedArray()
            val nextIndex = ((schedule.lastFeedbackReminderIndex ?: -1) + 1) % reminderValues.size
            val reminderToSend = reminderValues[nextIndex].message

            val sent = slackService.postMessageByWebhook(organisation.id, reminderToSend)
            if (sent) {
                organisationService.updateFeedbackReminderSchedule(
                    organisationId = organisation.id,
                    feedbackReminderIndex = nextIndex,
                    lastFeedbackReminderSent = Timestamp.from(now),
                )
            }
        }
    }
}

package dashboard

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import scalereal.core.dashboard.DashboardRepository
import scalereal.core.dashboard.DashboardService
import scalereal.core.models.domain.FeedbacksData
import scalereal.core.models.domain.Goal
import scalereal.core.models.domain.OverviewData
import java.sql.Date
import java.sql.Timestamp

class DashboardServiceImplTest : StringSpec() {
    private val dashboardRepository = mockk<DashboardRepository>()
    private val dashboardService = DashboardService(dashboardRepository)

    init {
        "should fetch feedback overview" {
            val feedbackOverview =
                listOf(
                    OverviewData(
                        reviewCycleId = 1,
                        firstName = "Rushad",
                        startDate = Date.valueOf("2022-11-07"),
                        endDate = Date.valueOf("2023-06-25"),
                        selfReviewStartDate = Date.valueOf("2022-11-10"),
                        selfReviewEndDate = Date.valueOf("2022-11-18"),
                        selfReviewDraft = null,
                        selfReviewPublish = null,
                        positive = 2,
                        improvement = 1,
                        appreciation = 3,
                    ),
                )
            val organisationId = 1L
            val employeeId = 1L
            every { dashboardRepository.fetchFeedbackOverview(any(), any()) } returns feedbackOverview
            dashboardService.fetchFeedbackOverview(organisationId = organisationId, id = employeeId) shouldBe feedbackOverview
            verify(exactly = 1) { dashboardRepository.fetchFeedbackOverview(organisationId = organisationId, id = employeeId) }
        }

        "should fetch employee feedback for given review cycle id" {
            val feedbackData =
                listOf(
                    FeedbacksData(
                        isExternalFeedback = false,
                        feedbackType = "Improvement",
                        feedback = "negative feedback",
                        feedbackFromId = 2,
                        feedbackFromEmployeeId = "SR0006",
                        feedbackFromFirstName = "Yogesh",
                        feedbackFromLastName = "Jadhav",
                        feedbackFromRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        submitDate = Timestamp.valueOf("2023-05-11 13:34:33.863392"),
                        isDraft = false,
                    ),
                    FeedbacksData(
                        isExternalFeedback = false,
                        feedbackType = "Positive",
                        feedback = "positive feedback",
                        feedbackFromId = 2,
                        feedbackFromEmployeeId = "SR0006",
                        feedbackFromFirstName = "Yogesh",
                        feedbackFromLastName = "Jadhav",
                        feedbackFromRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        submitDate = Timestamp.valueOf("2023-05-11 13:34:33.863392"),
                        isDraft = false,
                    ),
                )
            val organisationId = 1L
            val employeeId = listOf(1)
            val reviewCycleId = listOf(2)
            val feedbackTypeId = listOf(1, 2)
            every { dashboardRepository.fetchEmployeeFeedback(any(), any(), any(), any(), any(), any()) } returns feedbackData
            dashboardService.fetchEmployeeFeedback(
                organisationId = organisationId,
                id = employeeId,
                reviewCycleId = reviewCycleId,
                feedbackTypeId = feedbackTypeId,
                page = 1,
                limit = 10,
            ) shouldBe feedbackData
            verify(exactly = 1) {
                dashboardRepository.fetchEmployeeFeedback(
                    organisationId = organisationId,
                    id = employeeId,
                    reviewCycleId = reviewCycleId,
                    feedbackTypeId = feedbackTypeId,
                    offset = 0,
                    limit = 10,
                )
            }
        }

        "should return employee feedback count for given review cycle id" {
            val organisationId = 1L
            val employeeId = listOf(1)
            val reviewCycleId = listOf(2)
            val feedbackTypeId = listOf(1, 2)
            every { dashboardRepository.countEmployeeFeedback(any(), any(), any(), any()) } returns 2
            dashboardService.countEmployeeFeedback(
                organisationId = organisationId,
                id = employeeId,
                reviewCycleId = reviewCycleId,
                feedbackTypeId = feedbackTypeId,
            ) shouldBe 2
            verify(exactly = 1) {
                dashboardRepository.countEmployeeFeedback(
                    organisationId = organisationId,
                    id = employeeId,
                    reviewCycleId = reviewCycleId,
                    feedbackTypeId = feedbackTypeId,
                )
            }
        }

        "should fetch goals for given review cycle and employee id" {
            val actionItems =
                listOf(
                    Goal(
                        id = 1,
                        goalId = "G1",
                        typeId = 1,
                        description = "First Action Item",
                        createdAt = Timestamp.valueOf("2023-02-21 03:04:33.863392"),
                        targetDate = Date.valueOf("2023-07-21"),
                        progressId = 1,
                        progressName = null,
                        createdBy = 1,
                        assignedTo = 2,
                    ),
                    Goal(
                        id = 2,
                        goalId = "G2",
                        typeId = 1,
                        description = "Second Action Item",
                        createdAt = Timestamp.valueOf("2023-02-21 03:04:33.863392"),
                        targetDate = Date.valueOf("2023-07-21"),
                        progressId = 2,
                        progressName = null,
                        createdBy = 1,
                        assignedTo = 2,
                    ),
                    Goal(
                        id = 3,
                        goalId = "G3",
                        typeId = 1,
                        description = "Third Action Item",
                        createdAt = Timestamp.valueOf("2023-02-21 03:04:33.863392"),
                        targetDate = Date.valueOf("2023-07-21"),
                        progressId = 3,
                        progressName = null,
                        createdBy = 1,
                        assignedTo = 2,
                    ),
                )
            val organisationId = 1L
            val employeeId = 1L
            val reviewCycleId = listOf(1, 2)
            every {
                dashboardRepository.fetchGoal(any(), any(), any(), any(), any())
            } returns listOf(actionItems[0], actionItems[1]) andThen listOf(actionItems[2])
            dashboardService.fetchGoal(
                organisationId = organisationId,
                id = employeeId,
                reviewCycleId = reviewCycleId,
                page = 1,
                limit = 10,
            ) shouldBe actionItems
            verify(exactly = 1) {
                dashboardRepository.fetchGoal(
                    organisationId = organisationId,
                    id = employeeId,
                    reviewCycleId = reviewCycleId[0],
                    offset = 0,
                    limit = 10,
                )
            }
            verify(exactly = 1) {
                dashboardRepository.fetchGoal(
                    organisationId = organisationId,
                    id = employeeId,
                    reviewCycleId = reviewCycleId[1],
                    offset = 0,
                    limit = 10,
                )
            }
        }

        "should return goals count for given review cycle and employee id" {
            val organisationId = 1L
            val employeeId = 1L
            val reviewCycleId = listOf(1, 2)
            every { dashboardRepository.countGoal(any(), any(), any()) } returns 2 andThen 1
            dashboardService.countGoal(
                organisationId = organisationId,
                id = employeeId,
                reviewCycleId = reviewCycleId,
            ) shouldBe 3
            verify(exactly = 1) {
                dashboardRepository.countGoal(
                    organisationId = organisationId,
                    id = employeeId,
                    reviewCycleId = reviewCycleId[0],
                )
            }
            verify(exactly = 1) {
                dashboardRepository.countGoal(
                    organisationId = organisationId,
                    id = employeeId,
                    reviewCycleId = reviewCycleId[1],
                )
            }
        }

        "should return empty list and count 0 for goals if given review cycle id is -99" {
            val organisationId = 1L
            val employeeId = 1L
            val reviewCycleId = listOf(-99)
            dashboardService.fetchGoal(
                organisationId = organisationId,
                id = employeeId,
                reviewCycleId = reviewCycleId,
                page = 1,
                limit = 10,
            ) shouldBe emptyList()
            dashboardService.countGoal(
                organisationId = organisationId,
                id = employeeId,
                reviewCycleId = reviewCycleId,
            ) shouldBe 0
        }
    }
}

package goals

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import scalereal.core.employees.EmployeeRepository
import scalereal.core.exception.GoalException
import scalereal.core.goals.GoalRepository
import scalereal.core.goals.GoalService
import scalereal.core.models.GoalProgress
import scalereal.core.models.domain.Goal
import scalereal.core.models.domain.GoalGroup
import scalereal.core.models.domain.ReviewCycleDates
import scalereal.core.reviewCycle.ReviewCycleRepository
import java.sql.Date
import java.sql.Timestamp

class GoalServiceImplTest : StringSpec() {
    private val goalRepository = mockk<GoalRepository>()
    private val reviewCycleRepository = mockk<ReviewCycleRepository>()
    private val employeeRepository = mockk<EmployeeRepository>()
    private val goalService = GoalService(goalRepository, reviewCycleRepository, employeeRepository)

    init {
        afterTest {
            clearMocks(goalRepository)
        }

        "should fetch grouped goals correctly with split queries" {
            val organisationId = 1L
            val reviewToId = 3L
            val reviewCycleId = 6L

            val cycle1 =
                ReviewCycleDates(
                    id = 100L,
                    startDate = Date.valueOf("2025-01-15"),
                    endDate = Date.valueOf("2025-01-16"),
                )
            val cycle2 =
                ReviewCycleDates(
                    id = 101L,
                    startDate = Date.valueOf("2025-02-19"),
                    endDate = Date.valueOf("2025-02-21"),
                )
            val cycles = listOf(cycle1, cycle2)

            val itemsFor100 =
                listOf(
                    Goal(
                        id = 1L,
                        goalId = "G1",
                        typeId = 1,
                        description = "Goal for testing",
                        createdAt = Timestamp.valueOf("2025-01-09 14:30:26.876743"),
                        targetDate = Date.valueOf("2025-01-31"),
                        progressId = 1,
                        progressName = "To Do",
                        createdBy = 1,
                        assignedTo = 2,
                    ),
                )
            val itemsFor101 =
                listOf(
                    Goal(
                        id = 2L,
                        goalId = "G2",
                        typeId = 1,
                        description = "Goal for clear code",
                        createdAt = Timestamp.valueOf("2025-01-16 12:59:24.152143"),
                        targetDate = Date.valueOf("2025-01-31"),
                        progressId = 1,
                        progressName = "To Do",
                        createdBy = 1,
                        assignedTo = 2,
                    ),
                )

            every {
                reviewCycleRepository.getReviewCycles(organisationId, reviewCycleId, 3)
            } returns cycles

            every {
                goalRepository.getGoalsForCycle(reviewToId, cycle1.id)
            } returns itemsFor100

            every {
                goalRepository.getGoalsForCycle(reviewToId, cycle2.id)
            } returns itemsFor101

            val expectedGroups =
                listOf(
                    GoalGroup(
                        startDate = cycle1.startDate,
                        endDate = cycle1.endDate,
                        goals = itemsFor100,
                    ),
                    GoalGroup(
                        startDate = cycle2.startDate,
                        endDate = cycle2.endDate,
                        goals = itemsFor101,
                    ),
                )

            val result =
                goalService.fetchGoal(
                    organisationId = organisationId,
                    reviewToId = reviewToId,
                    reviewCycleId = reviewCycleId,
                )

            result shouldBe expectedGroups

            verify(exactly = 1) {
                reviewCycleRepository.getReviewCycles(organisationId, reviewCycleId, 3)
            }
            verify(exactly = 1) {
                goalRepository.getGoalsForCycle(reviewToId, cycle1.id)
                goalRepository.getGoalsForCycle(reviewToId, cycle2.id)
            }
        }

        "should update goal progress when item exists" {
            val id = 100L
            val progressId = 2
            val actionBy = 2L
            val goal =
                Goal(
                    id = id,
                    goalId = "G1",
                    typeId = 1,
                    description = "goal description",
                    createdAt = Timestamp.valueOf("2025-01-10 10:10:10.000000"),
                    targetDate = Date.valueOf("2025-01-31"),
                    progressId = progressId,
                    progressName = GoalProgress.IN_PROGRESS.progressName,
                    createdBy = 1,
                    assignedTo = 2,
                )
            val updatedGoal =
                Goal(
                    id = id,
                    goalId = "G1",
                    typeId = 1,
                    description = "Updated goal",
                    createdAt = Timestamp.valueOf("2025-01-10 10:10:10.000000"),
                    targetDate = Date.valueOf("2025-01-31"),
                    progressId = progressId,
                    progressName = GoalProgress.IN_PROGRESS.progressName,
                    createdBy = 1,
                    assignedTo = 2,
                )

            every { goalRepository.getGoalById(id) } returns goal
            every { goalRepository.updateGoalProgress(id, progressId) } returns updatedGoal

            val result = goalService.updateGoalProgress(id, progressId, actionBy)

            result shouldBe updatedGoal

            verify(exactly = 1) { goalRepository.getGoalById(id) }
            verify(exactly = 1) { goalRepository.updateGoalProgress(id, progressId) }
        }

        "should throw exception when goal does not exist" {
            val id = 101L
            val progressId = 3
            val actionBy = 2L

            every { goalRepository.getGoalById(id) } returns null

            val exception =
                shouldThrow<GoalException> {
                    goalService.updateGoalProgress(id, progressId, actionBy)
                }

            exception.message shouldBe "Goal does not exist for id: $id."

            verify(exactly = 1) { goalRepository.getGoalById(id) }
            verify(exactly = 0) { goalRepository.updateGoalProgress(any(), any()) }
        }
    }
}

package scalereal.core.dashboard

import jakarta.inject.Singleton
import scalereal.core.models.GoalProgress
import scalereal.core.models.domain.Goal
import scalereal.core.models.domain.OverviewData

@Singleton
class DashboardService(
    private val repository: DashboardRepository,
) {
    fun fetchFeedbackOverview(
        organisationId: Long,
        id: Long,
    ): List<OverviewData> = repository.fetchFeedbackOverview(organisationId = organisationId, id = id)

    fun fetchEmployeeFeedback(
        organisationId: Long,
        id: List<Int>,
        reviewCycleId: List<Int>,
        feedbackTypeId: List<Int>,
        page: Int,
        limit: Int,
    ): List<Any> =
        repository.fetchEmployeeFeedback(
            organisationId = organisationId,
            id = id,
            reviewCycleId = reviewCycleId,
            feedbackTypeId = feedbackTypeId,
            offset = (page - 1) * limit,
            limit = limit,
        )

    fun countEmployeeFeedback(
        organisationId: Long,
        id: List<Int>,
        reviewCycleId: List<Int>,
        feedbackTypeId: List<Int>,
    ): Int = repository.countEmployeeFeedback(organisationId, id, reviewCycleId, feedbackTypeId)

    fun fetchGoal(
        organisationId: Long,
        id: Long,
        reviewCycleId: List<Int>,
        page: Int,
        limit: Int,
    ): List<Goal> {
        if (-99 in reviewCycleId) return emptyList()

        val progressMap =
            GoalProgress
                .getProgressListWithId()
                .associateBy { it.progressId }

        return reviewCycleId.flatMap { cycleId ->
            repository
                .fetchGoal(
                    organisationId = organisationId,
                    id = id,
                    reviewCycleId = cycleId,
                    offset = (page - 1) * limit,
                    limit = limit,
                ).onEach { goal ->
                    goal.progressName =
                        goal.progressId
                            .let { progressMap[it]?.progressName }
                }
        }
    }

    fun countGoal(
        organisationId: Long,
        id: Long,
        reviewCycleId: List<Int>,
    ): Int {
        var goalCount = 0
        if (reviewCycleId.contains(-99)) {
            return goalCount
        }
        reviewCycleId.forEach { cycleId ->
            val goals =
                repository.countGoal(organisationId = organisationId, id = id, reviewCycleId = cycleId)
            goalCount += goals
        }
        return goalCount
    }
}

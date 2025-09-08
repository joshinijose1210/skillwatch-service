package scalereal.core.dashboard

import scalereal.core.models.domain.Goal
import scalereal.core.models.domain.OverviewData

interface DashboardRepository {
    fun fetchFeedbackOverview(
        organisationId: Long,
        id: Long,
    ): List<OverviewData>

    fun fetchEmployeeFeedback(
        organisationId: Long,
        id: List<Int>,
        reviewCycleId: List<Int>,
        feedbackTypeId: List<Int>,
        offset: Int,
        limit: Int,
    ): List<Any>

    fun countEmployeeFeedback(
        organisationId: Long,
        id: List<Int>,
        reviewCycleId: List<Int>,
        feedbackTypeId: List<Int>,
    ): Int

    fun fetchGoal(
        organisationId: Long,
        id: Long,
        reviewCycleId: Int,
        offset: Int,
        limit: Int,
    ): List<Goal>

    fun countGoal(
        organisationId: Long,
        id: Long,
        reviewCycleId: Int,
    ): Int
}

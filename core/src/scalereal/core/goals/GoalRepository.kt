package scalereal.core.goals

import scalereal.core.models.domain.CreateGoalRequest
import scalereal.core.models.domain.Goal
import scalereal.core.models.domain.GoalDetails

interface GoalRepository {
    fun getGoalsForCycle(
        goalToId: Long,
        reviewCycleId: Long,
    ): List<Goal>

    fun updateGoalProgress(
        id: Long,
        progressId: Int,
    ): Goal

    fun isGoalExists(id: Long): Boolean

    fun getMaxGoalId(organisationId: Long): Int

    fun updateGoalDetails(
        id: Long,
        description: String?,
        typeId: Int?,
    ): Goal

    fun getGoalById(id: Long): Goal?

    fun fetchAllGoals(
        requestBy: List<Int>,
        organisationId: Long,
        progressId: List<Int>,
        typeId: List<Int>,
        reviewCycleId: List<Int>,
        offset: Int,
        limit: Int,
    ): List<GoalDetails>

    fun countAllGoals(
        requestBy: List<Int>,
        organisationId: Long,
        progressId: List<Int>,
        typeId: List<Int>,
        reviewCycleId: List<Int>,
    ): Int

    fun create(
        organisationId: Long,
        createdBy: Long,
        createGoalRequest: CreateGoalRequest,
    ): Long
}

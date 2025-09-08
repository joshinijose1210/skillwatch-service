package scalereal.core.goals

import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeRepository
import scalereal.core.exception.GoalException
import scalereal.core.models.GoalProgress
import scalereal.core.models.GoalType
import scalereal.core.models.domain.CreateGoalRequest
import scalereal.core.models.domain.CreateGoalResponse
import scalereal.core.models.domain.Goal
import scalereal.core.models.domain.GoalDetails
import scalereal.core.models.domain.GoalGroup
import scalereal.core.reviewCycle.ReviewCycleRepository
import kotlin.collections.contains

@Singleton
class GoalService(
    private val goalRepository: GoalRepository,
    private val reviewCycleRepository: ReviewCycleRepository,
    private val employeeRepository: EmployeeRepository,
) {
    fun fetchGoal(
        organisationId: Long,
        reviewToId: Long,
        reviewCycleId: Long,
    ): List<GoalGroup> {
        val progressMap =
            GoalProgress
                .getProgressListWithId()
                .associateBy { it.progressId }
        val reviewCycles = reviewCycleRepository.getReviewCycles(organisationId, reviewCycleId, numberOfCycles = 3)
        return reviewCycles.map { reviewCycle ->
            val goalsWithProgress =
                goalRepository
                    .getGoalsForCycle(goalToId = reviewToId, reviewCycleId = reviewCycle.id)
                    .map { goal ->
                        goal.copy(
                            progressName = progressMap[goal.progressId]?.progressName,
                        )
                    }

            GoalGroup(
                startDate = reviewCycle.startDate,
                endDate = reviewCycle.endDate,
                goals = goalsWithProgress,
            )
        }
    }

    fun updateGoalProgress(
        id: Long,
        progressId: Int,
        actionBy: Long,
    ): Goal {
        val goal =
            goalRepository.getGoalById(id)
                ?: throw GoalException("Goal does not exist for id: $id.")

        if (progressId !in GoalProgress.entries.map { it.progressId }) {
            throw GoalException("Invalid goal progress.")
        }
        if (goal.assignedTo != actionBy) {
            throw GoalException("Only the owner is allowed to update goal progress.")
        }
        return goalRepository.updateGoalProgress(id, progressId)
    }

    fun updateGoalDetails(
        id: Long,
        description: String?,
        typeId: Int?,
        actionBy: Long,
    ): Goal {
        val goal =
            goalRepository.getGoalById(id)
                ?: throw GoalException("Goal does not exist for id: $id.")

        if (goal.createdBy != actionBy) {
            throw GoalException("Only the creator can update description or goal type.")
        }

        if (typeId !in GoalType.entries.map { it.typeId }) {
            throw GoalException("Invalid goal type id.")
        }

        return goalRepository.updateGoalDetails(
            id = id,
            description = description,
            typeId = typeId,
        )
    }

    private val typeMap: Map<Int, String> =
        GoalType.entries.associate { it.typeId to it.typeName }

    private val progressMap: Map<Int, String> =
        GoalProgress.entries.associate { it.progressId to it.progressName }

    fun fetchAllGoals(
        requestBy: List<Int>,
        organisationId: Long,
        progressId: List<Int>,
        typeId: List<Int>,
        reviewCycleId: List<Int>,
        page: Int,
        limit: Int,
    ): List<GoalDetails> =
        goalRepository
            .fetchAllGoals(
                requestBy,
                organisationId,
                progressId,
                typeId,
                reviewCycleId,
                offset = (page - 1) * limit,
                limit = limit,
            ).map { it.enrich(typeMap, progressMap) }

    fun countAllGoals(
        requestBy: List<Int>,
        organisationId: Long,
        progressId: List<Int>,
        typeId: List<Int>,
        reviewCycleId: List<Int>,
    ): Int =
        goalRepository.countAllGoals(
            requestBy,
            organisationId,
            progressId,
            typeId,
            reviewCycleId,
        )

    fun create(
        createGoalRequest: CreateGoalRequest,
        authenticatedUserId: Long,
    ): CreateGoalResponse {
        val authenticatedUser = employeeRepository.getEmployeeDataByUniqueId(authenticatedUserId)

        if (createGoalRequest.typeId !in GoalType.entries.map { it.typeId }) {
            throw GoalException("Invalid goal type id.")
        }

        val lastGoalId = goalRepository.getMaxGoalId(authenticatedUser.organisationId)

        val id = goalRepository.create(authenticatedUser.organisationId, authenticatedUser.id, createGoalRequest.withGoalId(lastGoalId))

        return CreateGoalResponse(id = id)
    }
}

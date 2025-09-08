package scalereal.db.goals

import goals.AddGoalParams
import goals.AddGoalQuery
import goals.GetAllGoalsCountParams
import goals.GetAllGoalsCountQuery
import goals.GetAllGoalsParams
import goals.GetAllGoalsQuery
import goals.GetGoalByIdParams
import goals.GetGoalByIdQuery
import goals.GetGoalByIdResult
import goals.GetGoalsParams
import goals.GetGoalsQuery
import goals.GetMaxGoalIdParams
import goals.GetMaxGoalIdQuery
import goals.IsGoalExistsParams
import goals.IsGoalExistsQuery
import goals.UpdateGoalParams
import goals.UpdateGoalProgressParams
import goals.UpdateGoalProgressQuery
import goals.UpdateGoalProgressResult
import goals.UpdateGoalQuery
import goals.UpdateGoalResult
import jakarta.inject.Inject
import jakarta.inject.Singleton
import norm.query
import scalereal.core.goals.GoalRepository
import scalereal.core.models.domain.CreateGoalRequest
import scalereal.core.models.domain.Goal
import scalereal.core.models.domain.GoalDetails
import javax.sql.DataSource

@Singleton
class GoalRepositoryImpl(
    @Inject private val dataSource: DataSource,
) : GoalRepository {
    override fun getGoalsForCycle(
        goalToId: Long,
        reviewCycleId: Long,
    ): List<Goal> =
        dataSource.connection.use { connection ->
            GetGoalsQuery()
                .query(
                    connection,
                    GetGoalsParams(
                        goalToId = goalToId,
                        reviewCycleId = reviewCycleId,
                    ),
                ).map {
                    Goal(
                        id = it.id,
                        goalId = it.goalId,
                        typeId = it.typeId,
                        description = it.description,
                        createdAt = it.createdAt,
                        targetDate = it.targetDate,
                        progressId = it.progressId,
                        progressName = null,
                        createdBy = it.createdBy,
                        assignedTo = it.assignedTo,
                    )
                }
        }

    override fun updateGoalProgress(
        id: Long,
        progressId: Int,
    ): Goal =
        dataSource.connection.use { connection ->
            UpdateGoalProgressQuery()
                .query(connection, UpdateGoalProgressParams(progressId = progressId, id = id))
                .map { it.toGoals() }
                .first()
        }

    override fun isGoalExists(id: Long): Boolean =
        dataSource.connection.use { connection ->
            IsGoalExistsQuery()
                .query(connection, IsGoalExistsParams(id))
                .map { it.exists ?: false }
                .first()
        }

    override fun getMaxGoalId(organisationId: Long): Int =
        dataSource.connection.use { connection ->
            GetMaxGoalIdQuery()
                .query(connection, GetMaxGoalIdParams(organisationId))
                .map { it.maxId ?: 0 }
                .first()
        }

    override fun updateGoalDetails(
        id: Long,
        description: String?,
        typeId: Int?,
    ): Goal =
        dataSource.connection.use { connection ->
            UpdateGoalQuery()
                .query(
                    connection,
                    UpdateGoalParams(
                        id = id,
                        description = description,
                        typeId = typeId,
                    ),
                ).map { it.toGoal() }
                .first()
        }

    override fun getGoalById(id: Long): Goal? =
        dataSource.connection.use { connection ->
            GetGoalByIdQuery()
                .query(connection, GetGoalByIdParams(id = id))
                .map { it.toGoal() }
                .firstOrNull()
        }

    override fun fetchAllGoals(
        requestBy: List<Int>,
        organisationId: Long,
        progressId: List<Int>,
        typeId: List<Int>,
        reviewCycleId: List<Int>,
        offset: Int,
        limit: Int,
    ): List<GoalDetails> =
        dataSource.connection.use { connection ->
            GetAllGoalsQuery()
                .query(
                    connection,
                    GetAllGoalsParams(
                        assignedTo = requestBy.toTypedArray(),
                        organisationId = organisationId,
                        progressId = progressId.toTypedArray(),
                        typeId = typeId.toTypedArray(),
                        reviewCycleId = reviewCycleId.toTypedArray(),
                        offset = offset,
                        limit = limit,
                    ),
                ).map {
                    GoalDetails(
                        id = it.id,
                        goalId = it.goalId,
                        description = it.description ?: "",
                        typeId = it.typeId,
                        typeName = null,
                        progressId = it.progressId,
                        progressName = null,
                        createdBy = it.createdBy,
                        createdByName = it.createdByName,
                        assignedTo = it.assignedTo,
                        assignedToName = it.assignedToName,
                        targetDate = it.targetDate,
                    )
                }
        }

    override fun countAllGoals(
        requestBy: List<Int>,
        organisationId: Long,
        progressId: List<Int>,
        typeId: List<Int>,
        reviewCycleId: List<Int>,
    ): Int =
        dataSource.connection.use { connection ->
            GetAllGoalsCountQuery()
                .query(
                    connection,
                    GetAllGoalsCountParams(
                        assignedTo = requestBy.toTypedArray(),
                        organisationId = organisationId,
                        goalStatus = progressId.toTypedArray(),
                        goalType = typeId.toTypedArray(),
                        reviewCycleId = reviewCycleId.toTypedArray(),
                    ),
                )[0]
                .goalCount
                ?.toInt() ?: 0
        }

    override fun create(
        organisationId: Long,
        createdBy: Long,
        createGoalRequest: CreateGoalRequest,
    ): Long =
        dataSource.connection.use { connection ->
            AddGoalQuery()
                .query(
                    connection,
                    AddGoalParams(
                        description = createGoalRequest.description,
                        targetDate = createGoalRequest.targetDate,
                        goalId = createGoalRequest.goalId,
                        typeId = createGoalRequest.typeId,
                        assignedTo = createGoalRequest.assignedTo,
                        createdBy = createdBy,
                        organisationId = organisationId,
                    ),
                ).map { it.id }
                .first()
        }

    private fun UpdateGoalResult.toGoal() =
        Goal(
            id = id,
            goalId = goalId,
            typeId = typeId,
            description = description,
            createdAt = createdAt,
            targetDate = targetDate,
            progressId = progressId,
            progressName = null,
            createdBy = createdBy,
            assignedTo = assignedTo,
        )

    private fun GetGoalByIdResult.toGoal() =
        Goal(
            id = id,
            goalId = "G$goalId",
            typeId = typeId,
            description = description,
            createdAt = createdAt,
            targetDate = targetDate,
            progressId = progressId,
            progressName = null,
            createdBy = createdBy,
            assignedTo = assignedTo,
        )

    private fun UpdateGoalProgressResult.toGoals() =
        Goal(
            id = id,
            goalId = goalId,
            typeId = typeId,
            description = description,
            createdAt = createdAt,
            targetDate = targetDate,
            progressId = progressId,
            progressName = null,
            createdBy = createdBy,
            assignedTo = assignedTo,
        )
}

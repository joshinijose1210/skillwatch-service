package goals

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class UpdateGoalParams(
  val description: String?,
  val typeId: Int?,
  val id: Long?
)

class UpdateGoalParamSetter : ParamSetter<UpdateGoalParams> {
  override fun map(ps: PreparedStatement, params: UpdateGoalParams) {
    ps.setObject(1, params.description)
    ps.setObject(2, params.typeId)
    ps.setObject(3, params.id)
  }
}

data class UpdateGoalResult(
  val id: Long,
  val goalId: String?,
  val typeId: Int,
  val description: String?,
  val createdAt: Timestamp,
  val targetDate: Date,
  val progressId: Int,
  val createdBy: Long,
  val assignedTo: Long
)

class UpdateGoalRowMapper : RowMapper<UpdateGoalResult> {
  override fun map(rs: ResultSet): UpdateGoalResult = UpdateGoalResult(
  id = rs.getObject("id") as kotlin.Long,
    goalId = rs.getObject("goal_id") as kotlin.String?,
    typeId = rs.getObject("type_id") as kotlin.Int,
    description = rs.getObject("description") as kotlin.String?,
    createdAt = rs.getObject("created_at") as java.sql.Timestamp,
    targetDate = rs.getObject("target_date") as java.sql.Date,
    progressId = rs.getObject("progress_id") as kotlin.Int,
    createdBy = rs.getObject("created_by") as kotlin.Long,
    assignedTo = rs.getObject("assigned_to") as kotlin.Long)
}

class UpdateGoalQuery : Query<UpdateGoalParams, UpdateGoalResult> {
  override val sql: String = """
      |UPDATE goals
      |SET
      |    description = COALESCE(?, description),
      |    type_id = COALESCE(?, type_id)
      |WHERE id = ?
      |RETURNING
      |    id,
      |    'G' || goal_id AS goal_id,
      |    type_id,
      |    description,
      |    created_at,
      |    target_date,
      |    progress_id,
      |    created_by,
      |    assigned_to;
      |""".trimMargin()

  override val mapper: RowMapper<UpdateGoalResult> = UpdateGoalRowMapper()

  override val paramSetter: ParamSetter<UpdateGoalParams> = UpdateGoalParamSetter()
}

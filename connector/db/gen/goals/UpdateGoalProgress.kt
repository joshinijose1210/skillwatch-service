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

data class UpdateGoalProgressParams(
  val progressId: Int?,
  val id: Long?
)

class UpdateGoalProgressParamSetter : ParamSetter<UpdateGoalProgressParams> {
  override fun map(ps: PreparedStatement, params: UpdateGoalProgressParams) {
    ps.setObject(1, params.progressId)
    ps.setObject(2, params.id)
  }
}

data class UpdateGoalProgressResult(
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

class UpdateGoalProgressRowMapper : RowMapper<UpdateGoalProgressResult> {
  override fun map(rs: ResultSet): UpdateGoalProgressResult = UpdateGoalProgressResult(
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

class UpdateGoalProgressQuery : Query<UpdateGoalProgressParams, UpdateGoalProgressResult> {
  override val sql: String = """
      |UPDATE goals
      |SET progress_id = ?
      |WHERE goals.id = ?
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

  override val mapper: RowMapper<UpdateGoalProgressResult> = UpdateGoalProgressRowMapper()

  override val paramSetter: ParamSetter<UpdateGoalProgressParams> = UpdateGoalProgressParamSetter()
}

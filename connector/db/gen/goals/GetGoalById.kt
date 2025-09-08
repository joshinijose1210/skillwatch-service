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

data class GetGoalByIdParams(
  val id: Long?
)

class GetGoalByIdParamSetter : ParamSetter<GetGoalByIdParams> {
  override fun map(ps: PreparedStatement, params: GetGoalByIdParams) {
    ps.setObject(1, params.id)
  }
}

data class GetGoalByIdResult(
  val id: Long,
  val description: String?,
  val createdAt: Timestamp,
  val targetDate: Date,
  val progressId: Int,
  val createdBy: Long,
  val assignedTo: Long,
  val goalId: Int,
  val typeId: Int
)

class GetGoalByIdRowMapper : RowMapper<GetGoalByIdResult> {
  override fun map(rs: ResultSet): GetGoalByIdResult = GetGoalByIdResult(
  id = rs.getObject("id") as kotlin.Long,
    description = rs.getObject("description") as kotlin.String?,
    createdAt = rs.getObject("created_at") as java.sql.Timestamp,
    targetDate = rs.getObject("target_date") as java.sql.Date,
    progressId = rs.getObject("progress_id") as kotlin.Int,
    createdBy = rs.getObject("created_by") as kotlin.Long,
    assignedTo = rs.getObject("assigned_to") as kotlin.Long,
    goalId = rs.getObject("goal_id") as kotlin.Int,
    typeId = rs.getObject("type_id") as kotlin.Int)
}

class GetGoalByIdQuery : Query<GetGoalByIdParams, GetGoalByIdResult> {
  override val sql: String = """
      |SELECT
      |  id,
      |  description,
      |  created_at,
      |  target_date,
      |  progress_id,
      |  created_by,
      |  assigned_to,
      |  goal_id,
      |  type_id
      |FROM
      |  goals
      |WHERE
      |goals.id = ?;
      """.trimMargin()

  override val mapper: RowMapper<GetGoalByIdResult> = GetGoalByIdRowMapper()

  override val paramSetter: ParamSetter<GetGoalByIdParams> = GetGoalByIdParamSetter()
}

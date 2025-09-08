package review

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

data class GetGoalsParams(
  val reviewDetailsId: Long?
)

class GetGoalsParamSetter : ParamSetter<GetGoalsParams> {
  override fun map(ps: PreparedStatement, params: GetGoalsParams) {
    ps.setObject(1, params.reviewDetailsId)
  }
}

data class GetGoalsResult(
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

class GetGoalsRowMapper : RowMapper<GetGoalsResult> {
  override fun map(rs: ResultSet): GetGoalsResult = GetGoalsResult(
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

class GetGoalsQuery : Query<GetGoalsParams, GetGoalsResult> {
  override val sql: String = """
      |SELECT
      |  g.id,
      |  'G' || g.goal_id AS goal_id,
      |  g.type_id,
      |  g.description,
      |  g.created_at,
      |  g.target_date,
      |  g.progress_id,
      |  g.created_by,
      |  g.assigned_to
      |FROM
      |  goals g
      |WHERE
      |  review_details_id = ? ;
      |""".trimMargin()

  override val mapper: RowMapper<GetGoalsResult> = GetGoalsRowMapper()

  override val paramSetter: ParamSetter<GetGoalsParams> = GetGoalsParamSetter()
}

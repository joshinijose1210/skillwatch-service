package review

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

class GetGoalsWithPastDeadlineParams

class GetGoalsWithPastDeadlineParamSetter : ParamSetter<GetGoalsWithPastDeadlineParams> {
  override fun map(ps: PreparedStatement, params: GetGoalsWithPastDeadlineParams) {
  }
}

data class GetGoalsWithPastDeadlineResult(
  val id: Long,
  val goalId: String?,
  val assignedTo: Long,
  val createdBy: Long
)

class GetGoalsWithPastDeadlineRowMapper : RowMapper<GetGoalsWithPastDeadlineResult> {
  override fun map(rs: ResultSet): GetGoalsWithPastDeadlineResult = GetGoalsWithPastDeadlineResult(
  id = rs.getObject("id") as kotlin.Long,
    goalId = rs.getObject("goal_id") as kotlin.String?,
    assignedTo = rs.getObject("assigned_to") as kotlin.Long,
    createdBy = rs.getObject("created_by") as kotlin.Long)
}

class GetGoalsWithPastDeadlineQuery : Query<GetGoalsWithPastDeadlineParams,
    GetGoalsWithPastDeadlineResult> {
  override val sql: String = """
      |SELECT
      |    g.id,
      |    'G' || g.goal_id AS goal_id,
      |    g.assigned_to,
      |    g.created_by
      |FROM goals g
      |WHERE DATE(g.target_date) = CURRENT_DATE - INTERVAL '1 day';
      |""".trimMargin()

  override val mapper: RowMapper<GetGoalsWithPastDeadlineResult> =
      GetGoalsWithPastDeadlineRowMapper()

  override val paramSetter: ParamSetter<GetGoalsWithPastDeadlineParams> =
      GetGoalsWithPastDeadlineParamSetter()
}

package goals

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetMaxGoalIdParams(
  val organisationId: Long?
)

class GetMaxGoalIdParamSetter : ParamSetter<GetMaxGoalIdParams> {
  override fun map(ps: PreparedStatement, params: GetMaxGoalIdParams) {
    ps.setObject(1, params.organisationId)
  }
}

data class GetMaxGoalIdResult(
  val maxId: Int?
)

class GetMaxGoalIdRowMapper : RowMapper<GetMaxGoalIdResult> {
  override fun map(rs: ResultSet): GetMaxGoalIdResult = GetMaxGoalIdResult(
  maxId = rs.getObject("max_id") as kotlin.Int?)
}

class GetMaxGoalIdQuery : Query<GetMaxGoalIdParams, GetMaxGoalIdResult> {
  override val sql: String = """
      |SELECT MAX(goal_id) as max_id FROM goals g WHERE g.organisation_id = ?;
      |""".trimMargin()

  override val mapper: RowMapper<GetMaxGoalIdResult> = GetMaxGoalIdRowMapper()

  override val paramSetter: ParamSetter<GetMaxGoalIdParams> = GetMaxGoalIdParamSetter()
}

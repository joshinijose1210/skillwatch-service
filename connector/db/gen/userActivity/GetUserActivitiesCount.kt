package userActivity

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetUserActivitiesCountParams(
  val organisationId: Long?
)

class GetUserActivitiesCountParamSetter : ParamSetter<GetUserActivitiesCountParams> {
  override fun map(ps: PreparedStatement, params: GetUserActivitiesCountParams) {
    ps.setObject(1, params.organisationId)
  }
}

data class GetUserActivitiesCountResult(
  val userActivitiesCount: Long?
)

class GetUserActivitiesCountRowMapper : RowMapper<GetUserActivitiesCountResult> {
  override fun map(rs: ResultSet): GetUserActivitiesCountResult = GetUserActivitiesCountResult(
  userActivitiesCount = rs.getObject("user_activities_count") as kotlin.Long?)
}

class GetUserActivitiesCountQuery : Query<GetUserActivitiesCountParams,
    GetUserActivitiesCountResult> {
  override val sql: String = """
      |SELECT COUNT(user_activity.activity) AS user_activities_count
      |FROM
      |  user_activity
      |  JOIN employees ON user_activity.employee_id = employees.id
      |WHERE
      |  employees.organisation_id = ?;
      """.trimMargin()

  override val mapper: RowMapper<GetUserActivitiesCountResult> = GetUserActivitiesCountRowMapper()

  override val paramSetter: ParamSetter<GetUserActivitiesCountParams> =
      GetUserActivitiesCountParamSetter()
}

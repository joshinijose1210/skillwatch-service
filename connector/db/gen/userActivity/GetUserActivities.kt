package userActivity

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetUserActivitiesParams(
  val organisationId: Long?,
  val offset: Int?,
  val limit: Int?
)

class GetUserActivitiesParamSetter : ParamSetter<GetUserActivitiesParams> {
  override fun map(ps: PreparedStatement, params: GetUserActivitiesParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.offset)
    ps.setObject(3, params.limit)
  }
}

data class GetUserActivitiesResult(
  val activity: String,
  val createdAt: Timestamp,
  val firstName: String,
  val lastName: String,
  val empId: String
)

class GetUserActivitiesRowMapper : RowMapper<GetUserActivitiesResult> {
  override fun map(rs: ResultSet): GetUserActivitiesResult = GetUserActivitiesResult(
  activity = rs.getObject("activity") as kotlin.String,
    createdAt = rs.getObject("created_at") as java.sql.Timestamp,
    firstName = rs.getObject("first_name") as kotlin.String,
    lastName = rs.getObject("last_name") as kotlin.String,
    empId = rs.getObject("emp_id") as kotlin.String)
}

class GetUserActivitiesQuery : Query<GetUserActivitiesParams, GetUserActivitiesResult> {
  override val sql: String = """
      |SELECT
      |  user_activity.activity,
      |  user_activity.created_at,
      |  employees.first_name,
      |  employees.last_name,
      |  employees.emp_id
      |FROM
      |  user_activity
      |  JOIN employees ON user_activity.employee_id = employees.id
      |WHERE
      |  employees.organisation_id = ?
      |ORDER BY
      | user_activity.created_at DESC
      |OFFSET (?::INT)
      |LIMIT (?::INT);
      """.trimMargin()

  override val mapper: RowMapper<GetUserActivitiesResult> = GetUserActivitiesRowMapper()

  override val paramSetter: ParamSetter<GetUserActivitiesParams> = GetUserActivitiesParamSetter()
}

package reviewCycle

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Array
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAllMyManagerReviewCycleCountParams(
  val organisationId: Long?,
  val reviewToId: Long?,
  val reviewCycleId: Array<Int>?,
  val reviewFromId: Array<Int>?,
  val reviewTypeId: Int?
)

class GetAllMyManagerReviewCycleCountParamSetter :
    ParamSetter<GetAllMyManagerReviewCycleCountParams> {
  override fun map(ps: PreparedStatement, params: GetAllMyManagerReviewCycleCountParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.reviewToId)
    ps.setArray(3, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setArray(4, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setArray(5, ps.connection.createArrayOf("int4", params.reviewFromId))
    ps.setArray(6, ps.connection.createArrayOf("int4", params.reviewFromId))
    ps.setObject(7, params.reviewTypeId)
    ps.setObject(8, params.organisationId)
    ps.setObject(9, params.reviewToId)
    ps.setArray(10, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setArray(11, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setArray(12, ps.connection.createArrayOf("int4", params.reviewFromId))
    ps.setArray(13, ps.connection.createArrayOf("int4", params.reviewFromId))
  }
}

data class GetAllMyManagerReviewCycleCountResult(
  val managerReviewCycleCount: Long?
)

class GetAllMyManagerReviewCycleCountRowMapper : RowMapper<GetAllMyManagerReviewCycleCountResult> {
  override fun map(rs: ResultSet): GetAllMyManagerReviewCycleCountResult =
      GetAllMyManagerReviewCycleCountResult(
  managerReviewCycleCount = rs.getObject("manager_review_cycle_count") as kotlin.Long?)
}

class GetAllMyManagerReviewCycleCountQuery : Query<GetAllMyManagerReviewCycleCountParams,
    GetAllMyManagerReviewCycleCountResult> {
  override val sql: String = """
      |SELECT COUNT(start_date) AS manager_review_cycle_count
      |FROM (
      |SELECT
      |   review_cycle.id AS review_cycle_id,
      |   review_cycle.start_date,
      |   review_cycle.end_date,
      |   review_cycle.manager_review_start_date,
      |   review_cycle.manager_review_end_date,
      |   review_cycle.publish,
      |   COALESCE(employees.id, null) AS review_to_id,
      |   COALESCE(employees.emp_id, null) AS review_to_employee_id,
      |   COALESCE(employees.first_name, null) AS first_name,
      |   COALESCE(employees.last_name, null) AS last_name,
      |   COALESCE(manager_details.id, null) AS review_from_id,
      |   COALESCE(manager_details.emp_id, null) AS review_from_employee_id,
      |   COALESCE(manager_details.first_name, null) AS manager_first_name,
      |   COALESCE(manager_details.last_name, null) AS manager_last_name,
      |   COALESCE(teams.team_name, null) AS team_name,
      |   null AS draft,
      |   null AS published,
      |   null AS average_rating
      |FROM
      |  review_cycle
      |  LEFT JOIN employees ON employees.organisation_id = review_cycle.organisation_id
      |  LEFT JOIN employee_manager_mapping_view ON employee_manager_mapping_view.emp_id = employees.id
      |  LEFT JOIN employees AS manager_details ON manager_details.id = employee_manager_mapping_view.manager_id
      |  LEFT JOIN employees_team_mapping ON employees.id = employees_team_mapping.emp_id
      |  LEFT JOIN teams ON teams.id = employees_team_mapping.team_id
      |  LEFT JOIN review_details ON review_details.review_cycle_id = review_cycle.id
      |  AND review_details.review_to = employees.id
      |WHERE
      |  review_details.review_cycle_id IS NULL
      |  AND review_cycle.organisation_id = ?
      |  AND employees.id = ?
      |  AND employees.status = true
      |  AND employees.created_at::date <= review_cycle.end_date
      |  AND (?::INT[] = '{-99}' OR review_cycle.id = ANY (?::INT[]))
      |  AND (?::INT[] = '{-99}' OR manager_details.id = ANY (?::INT[]))
      |UNION
      |SELECT
      |   review_cycle.id AS review_cycle_id,
      |   review_cycle.start_date,
      |   review_cycle.end_date,
      |   review_cycle.manager_review_start_date,
      |   review_cycle.manager_review_end_date,
      |   review_cycle.publish,
      |   COALESCE(employees.id, null) AS review_to_id,
      |   COALESCE(employees.emp_id, null) AS review_to_employee_id,
      |   COALESCE(employees.first_name, null) AS first_name,
      |   COALESCE(employees.last_name, null) AS last_name,
      |   COALESCE(manager_details.id, null) AS review_from_id,
      |   COALESCE(manager_details.emp_id, null) AS review_from_employee_id,
      |   COALESCE(manager_details.first_name, null) AS manager_first_name,
      |   COALESCE(manager_details.last_name, null) AS manager_last_name,
      |   COALESCE(teams.team_name, null) AS team_name,
      |   COALESCE(review_details.draft, null) AS draft,
      |   COALESCE(review_details.published, null) AS published,
      |   COALESCE(review_details.average_rating, null) AS average_rating
      |FROM
      |  review_cycle
      |  LEFT JOIN employees ON employees.organisation_id = review_cycle.organisation_id
      |  JOIN review_details ON review_details.review_cycle_id = review_cycle.id AND review_details.review_to = employees.id
      |  AND review_details.review_type_id = ?
      |  LEFT JOIN employees AS manager_details ON manager_details.id = review_details.review_from
      |  LEFT JOIN employees_team_mapping ON employees.id = employees_team_mapping.emp_id
      |  LEFT JOIN teams ON teams.id = employees_team_mapping.team_id
      |WHERE
      |  review_cycle.organisation_id = ?
      |  AND employees.id = ?
      |  AND employees.status = true
      |  AND employees.created_at::date <= review_cycle.end_date
      |  AND (?::INT[] = '{-99}' OR review_details.review_cycle_id = ANY (?::INT[]))
      |  AND (?::INT[] = '{-99}' OR manager_details.id = ANY (?::INT[]))
      |  )
      |  AS my_manager_data;
      """.trimMargin()

  override val mapper: RowMapper<GetAllMyManagerReviewCycleCountResult> =
      GetAllMyManagerReviewCycleCountRowMapper()

  override val paramSetter: ParamSetter<GetAllMyManagerReviewCycleCountParams> =
      GetAllMyManagerReviewCycleCountParamSetter()
}

package reviewCycle

import java.math.BigDecimal
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Array
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAllMyManagerReviewCycleParams(
  val organisationId: Long?,
  val reviewToId: Long?,
  val reviewCycleId: Array<Int>?,
  val reviewFromId: Array<Int>?,
  val reviewTypeId: Int?,
  val offset: Int?,
  val limit: Int?
)

class GetAllMyManagerReviewCycleParamSetter : ParamSetter<GetAllMyManagerReviewCycleParams> {
  override fun map(ps: PreparedStatement, params: GetAllMyManagerReviewCycleParams) {
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
    ps.setObject(14, params.offset)
    ps.setObject(15, params.limit)
  }
}

data class GetAllMyManagerReviewCycleResult(
  val reviewCycleId: Long,
  val startDate: Date,
  val endDate: Date,
  val managerReviewStartDate: Date,
  val managerReviewEndDate: Date,
  val publish: Boolean,
  val reviewToId: Long,
  val reviewToEmployeeId: String,
  val firstName: String,
  val lastName: String,
  val reviewFromId: Long?,
  val reviewFromEmployeeId: String?,
  val managerFirstName: String?,
  val managerLastName: String?,
  val teamName: String,
  val draft: Boolean?,
  val published: Boolean?,
  val averageRating: BigDecimal?
)

class GetAllMyManagerReviewCycleRowMapper : RowMapper<GetAllMyManagerReviewCycleResult> {
  override fun map(rs: ResultSet): GetAllMyManagerReviewCycleResult =
      GetAllMyManagerReviewCycleResult(
  reviewCycleId = rs.getObject("review_cycle_id") as kotlin.Long,
    startDate = rs.getObject("start_date") as java.sql.Date,
    endDate = rs.getObject("end_date") as java.sql.Date,
    managerReviewStartDate = rs.getObject("manager_review_start_date") as java.sql.Date,
    managerReviewEndDate = rs.getObject("manager_review_end_date") as java.sql.Date,
    publish = rs.getObject("publish") as kotlin.Boolean,
    reviewToId = rs.getObject("review_to_id") as kotlin.Long,
    reviewToEmployeeId = rs.getObject("review_to_employee_id") as kotlin.String,
    firstName = rs.getObject("first_name") as kotlin.String,
    lastName = rs.getObject("last_name") as kotlin.String,
    reviewFromId = rs.getObject("review_from_id") as kotlin.Long?,
    reviewFromEmployeeId = rs.getObject("review_from_employee_id") as kotlin.String?,
    managerFirstName = rs.getObject("manager_first_name") as kotlin.String?,
    managerLastName = rs.getObject("manager_last_name") as kotlin.String?,
    teamName = rs.getObject("team_name") as kotlin.String,
    draft = rs.getObject("draft") as kotlin.Boolean?,
    published = rs.getObject("published") as kotlin.Boolean?,
    averageRating = rs.getObject("average_rating") as java.math.BigDecimal?)
}

class GetAllMyManagerReviewCycleQuery : Query<GetAllMyManagerReviewCycleParams,
    GetAllMyManagerReviewCycleResult> {
  override val sql: String = """
      |SELECT
      |  my_manager_data.review_cycle_id,
      |  my_manager_data.start_date,
      |  my_manager_data.end_date,
      |  manager_review_start_date,
      |  my_manager_data.manager_review_end_date,
      |  my_manager_data.publish,
      |  my_manager_data.review_to_id,
      |  my_manager_data.review_to_employee_id,
      |  my_manager_data.first_name,
      |  my_manager_data.last_name,
      |  my_manager_data.review_from_id,
      |  my_manager_data.review_from_employee_id,
      |  my_manager_data.manager_first_name,
      |  my_manager_data.manager_last_name,
      |  my_manager_data.team_name,
      |  my_manager_data.draft,
      |  my_manager_data.published,
      |  my_manager_data.average_rating
      |  FROM (
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
      |  AS my_manager_data
      |ORDER BY
      |  my_manager_data.publish DESC,
      |  daterange(my_manager_data.start_date, my_manager_data.end_date) DESC
      |OFFSET (?::INT)
      |LIMIT (?::INT) ;
      """.trimMargin()

  override val mapper: RowMapper<GetAllMyManagerReviewCycleResult> =
      GetAllMyManagerReviewCycleRowMapper()

  override val paramSetter: ParamSetter<GetAllMyManagerReviewCycleParams> =
      GetAllMyManagerReviewCycleParamSetter()
}

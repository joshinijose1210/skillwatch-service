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

data class GetAllManagerReviewCycleParams(
  val reviewFromId: Long?,
  val reviewTypeId: Int?,
  val organisationId: Long?,
  val reviewToId: Array<Int>?,
  val reviewCycleId: Array<Int>?,
  val managerReviewDraft: Boolean?,
  val managerReviewPublished: Boolean?,
  val offset: Int?,
  val limit: Int?
)

class GetAllManagerReviewCycleParamSetter : ParamSetter<GetAllManagerReviewCycleParams> {
  override fun map(ps: PreparedStatement, params: GetAllManagerReviewCycleParams) {
    ps.setObject(1, params.reviewFromId)
    ps.setObject(2, params.reviewFromId)
    ps.setObject(3, params.reviewTypeId)
    ps.setObject(4, params.reviewFromId)
    ps.setObject(5, params.organisationId)
    ps.setObject(6, params.reviewFromId)
    ps.setArray(7, ps.connection.createArrayOf("int4", params.reviewToId))
    ps.setArray(8, ps.connection.createArrayOf("int4", params.reviewToId))
    ps.setArray(9, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setArray(10, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setObject(11, params.managerReviewDraft)
    ps.setObject(12, params.managerReviewDraft)
    ps.setObject(13, params.managerReviewPublished)
    ps.setObject(14, params.offset)
    ps.setObject(15, params.limit)
  }
}

data class GetAllManagerReviewCycleResult(
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
  val teamName: String,
  val draft: Boolean?,
  val published: Boolean?,
  val averageRating: BigDecimal?
)

class GetAllManagerReviewCycleRowMapper : RowMapper<GetAllManagerReviewCycleResult> {
  override fun map(rs: ResultSet): GetAllManagerReviewCycleResult = GetAllManagerReviewCycleResult(
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
    teamName = rs.getObject("team_name") as kotlin.String,
    draft = rs.getObject("draft") as kotlin.Boolean?,
    published = rs.getObject("published") as kotlin.Boolean?,
    averageRating = rs.getObject("average_rating") as java.math.BigDecimal?)
}

class GetAllManagerReviewCycleQuery : Query<GetAllManagerReviewCycleParams,
    GetAllManagerReviewCycleResult> {
  override val sql: String = """
      |SELECT
      |  review_cycle.id AS review_cycle_id,
      |  review_cycle.start_date,
      |  review_cycle.end_date,
      |  review_cycle.manager_review_start_date,
      |  review_cycle.manager_review_end_date,
      |  review_cycle.publish,
      |  employees.id AS review_to_id,
      |  employees.emp_id AS review_to_employee_id,
      |  employees.first_name,
      |  employees.last_name,
      |  employees_team_mapping_view.team_name,
      |  review_details.draft,
      |  review_details.published,
      |  review_details.average_rating
      |FROM
      |  review_cycle
      |  LEFT JOIN employee_manager_mapping AS emm
      |    ON ((emm.manager_id = ? AND type = 1)
      |        OR (emm.manager_id = ? AND type = 2))
      |  LEFT JOIN employees ON employees.id = emm.emp_id
      |  LEFT JOIN review_details ON review_cycle.id = review_details.review_cycle_id
      |    AND review_details.review_type_id = ?
      |    AND review_details.review_from = ?
      |    AND review_details.review_to = emm.emp_id
      |  LEFT JOIN employees_team_mapping_view ON employees.id = employees_team_mapping_view.emp_id
      |WHERE
      |  review_cycle.organisation_id = ?
      |  AND emm.emp_id != ?
      |  AND (emm.created_at::date <= review_cycle.self_review_end_date
      |        AND (emm.updated_at::date IS NULL OR emm.updated_at::date >= review_cycle.end_date))
      |  AND employees.status = true
      |  AND employees.created_at::date <= review_cycle.end_date
      |  AND (?::INT[] = '{-99}' OR emm.emp_id = ANY (?::INT[]))
      |  AND (?::INT[] = '{-99}' OR review_cycle.id = ANY (?::INT[]))
      |  AND (?::BOOLEAN IS NULL OR (COALESCE(review_details.draft, false) = ?
      |  AND COALESCE(review_details.published, false) = ?))
      |ORDER BY
      |  review_cycle.publish DESC,
      |  daterange(review_cycle.start_date, review_cycle.end_date) DESC
      |OFFSET (?::INT)
      |LIMIT (?::INT);
      """.trimMargin()

  override val mapper: RowMapper<GetAllManagerReviewCycleResult> =
      GetAllManagerReviewCycleRowMapper()

  override val paramSetter: ParamSetter<GetAllManagerReviewCycleParams> =
      GetAllManagerReviewCycleParamSetter()
}

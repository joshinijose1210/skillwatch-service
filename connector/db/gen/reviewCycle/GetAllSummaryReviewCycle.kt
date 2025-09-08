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

data class GetAllSummaryReviewCycleParams(
  val organisationId: Long?,
  val minRange: BigDecimal?,
  val maxRange: BigDecimal?,
  val managerId: Array<Int>?,
  val reviewToId: Array<Int>?,
  val reviewCycleId: Array<Int>?,
  val selfReviewDraft: Boolean?,
  val selfReviewPublished: Boolean?,
  val firstManagerReviewDraft: Boolean?,
  val firstManagerReviewPublished: Boolean?,
  val secondManagerReviewDraft: Boolean?,
  val secondManagerReviewPublished: Boolean?,
  val checkInDraft: Boolean?,
  val checkInPublished: Boolean?
)

class GetAllSummaryReviewCycleParamSetter : ParamSetter<GetAllSummaryReviewCycleParams> {
  override fun map(ps: PreparedStatement, params: GetAllSummaryReviewCycleParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.minRange)
    ps.setObject(3, params.maxRange)
    ps.setArray(4, ps.connection.createArrayOf("int4", params.managerId))
    ps.setArray(5, ps.connection.createArrayOf("int4", params.managerId))
    ps.setArray(6, ps.connection.createArrayOf("int4", params.managerId))
    ps.setArray(7, ps.connection.createArrayOf("int4", params.reviewToId))
    ps.setArray(8, ps.connection.createArrayOf("int4", params.reviewToId))
    ps.setArray(9, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setArray(10, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setObject(11, params.selfReviewDraft)
    ps.setObject(12, params.selfReviewDraft)
    ps.setObject(13, params.selfReviewPublished)
    ps.setObject(14, params.firstManagerReviewDraft)
    ps.setObject(15, params.firstManagerReviewDraft)
    ps.setObject(16, params.firstManagerReviewPublished)
    ps.setObject(17, params.secondManagerReviewDraft)
    ps.setObject(18, params.secondManagerReviewDraft)
    ps.setObject(19, params.secondManagerReviewPublished)
    ps.setObject(20, params.checkInDraft)
    ps.setObject(21, params.checkInDraft)
    ps.setObject(22, params.checkInPublished)
  }
}

data class GetAllSummaryReviewCycleResult(
  val reviewCycleId: Long,
  val startDate: Date,
  val endDate: Date,
  val publish: Boolean,
  val checkInStartDate: Date,
  val checkInEndDate: Date,
  val employeeid: String,
  val reviewToId: Long,
  val reviewToEmployeeId: String,
  val firstName: String,
  val lastName: String,
  val selfReviewDraft: Boolean?,
  val selfReviewPublish: Boolean?,
  val selfAverageRating: BigDecimal?,
  val firstManagerReviewDraft: Boolean?,
  val firstManagerReviewPublish: Boolean?,
  val firstManagerAverageRating: BigDecimal?,
  val secondManagerReviewDraft: Boolean?,
  val secondManagerReviewPublish: Boolean?,
  val secondManagerAverageRating: BigDecimal?,
  val checkInFromId: Long?,
  val checkInDraft: Boolean?,
  val checkInPublish: Boolean?,
  val checkInAverageRating: BigDecimal?,
  val firstManagerId: Long?,
  val firstManagerEmployeeId: String?,
  val firstManagerFirstName: String?,
  val firstManagerLastName: String?,
  val secondManagerId: Long?,
  val secondManagerEmployeeId: String?,
  val secondManagerFirstName: String?,
  val secondManagerLastName: String?
)

class GetAllSummaryReviewCycleRowMapper : RowMapper<GetAllSummaryReviewCycleResult> {
  override fun map(rs: ResultSet): GetAllSummaryReviewCycleResult = GetAllSummaryReviewCycleResult(
  reviewCycleId = rs.getObject("review_cycle_id") as kotlin.Long,
    startDate = rs.getObject("start_date") as java.sql.Date,
    endDate = rs.getObject("end_date") as java.sql.Date,
    publish = rs.getObject("publish") as kotlin.Boolean,
    checkInStartDate = rs.getObject("check_in_start_date") as java.sql.Date,
    checkInEndDate = rs.getObject("check_in_end_date") as java.sql.Date,
    employeeid = rs.getObject("employeeid") as kotlin.String,
    reviewToId = rs.getObject("review_to_id") as kotlin.Long,
    reviewToEmployeeId = rs.getObject("review_to_employee_id") as kotlin.String,
    firstName = rs.getObject("first_name") as kotlin.String,
    lastName = rs.getObject("last_name") as kotlin.String,
    selfReviewDraft = rs.getObject("self_review_draft") as kotlin.Boolean?,
    selfReviewPublish = rs.getObject("self_review_publish") as kotlin.Boolean?,
    selfAverageRating = rs.getObject("self_average_rating") as java.math.BigDecimal?,
    firstManagerReviewDraft = rs.getObject("first_manager_review_draft") as kotlin.Boolean?,
    firstManagerReviewPublish = rs.getObject("first_manager_review_publish") as kotlin.Boolean?,
    firstManagerAverageRating = rs.getObject("first_manager_average_rating") as
      java.math.BigDecimal?,
    secondManagerReviewDraft = rs.getObject("second_manager_review_draft") as kotlin.Boolean?,
    secondManagerReviewPublish = rs.getObject("second_manager_review_publish") as kotlin.Boolean?,
    secondManagerAverageRating = rs.getObject("second_manager_average_rating") as
      java.math.BigDecimal?,
    checkInFromId = rs.getObject("check_in_from_id") as kotlin.Long?,
    checkInDraft = rs.getObject("check_in_draft") as kotlin.Boolean?,
    checkInPublish = rs.getObject("check_in_publish") as kotlin.Boolean?,
    checkInAverageRating = rs.getObject("check_in_average_rating") as java.math.BigDecimal?,
    firstManagerId = rs.getObject("first_manager_id") as kotlin.Long?,
    firstManagerEmployeeId = rs.getObject("first_manager_employee_id") as kotlin.String?,
    firstManagerFirstName = rs.getObject("first_manager_first_name") as kotlin.String?,
    firstManagerLastName = rs.getObject("first_manager_last_name") as kotlin.String?,
    secondManagerId = rs.getObject("second_manager_id") as kotlin.Long?,
    secondManagerEmployeeId = rs.getObject("second_manager_employee_id") as kotlin.String?,
    secondManagerFirstName = rs.getObject("second_manager_first_name") as kotlin.String?,
    secondManagerLastName = rs.getObject("second_manager_last_name") as kotlin.String?)
}

class GetAllSummaryReviewCycleQuery : Query<GetAllSummaryReviewCycleParams,
    GetAllSummaryReviewCycleResult> {
  override val sql: String = """
      |SELECT
      |  review_cycle.id as review_cycle_id,
      |  review_cycle.start_date,
      |  review_cycle.end_date,
      |  review_cycle.publish,
      |  review_cycle.check_in_start_date,
      |  review_cycle.check_in_end_date,
      |  employees.emp_id AS employeeId,
      |  employees.id AS review_to_id,
      |  employees.emp_id AS review_to_employee_id,
      |  employees.first_name,
      |  employees.last_name,
      |  self_review.draft AS self_review_draft,
      |  self_review.published AS self_review_publish,
      |  self_review.average_rating AS self_average_rating,
      |  first_manager_review.draft AS first_manager_review_draft,
      |  first_manager_review.published AS first_manager_review_publish,
      |  first_manager_review.average_rating AS first_manager_average_rating,
      |  second_manager_review.draft AS second_manager_review_draft,
      |  second_manager_review.published AS second_manager_review_publish,
      |  second_manager_review.average_rating AS second_manager_average_rating,
      |  check_in_review.review_from AS check_in_from_id,
      |  check_in_review.draft AS check_in_draft,
      |  check_in_review.published AS check_in_publish,
      |  check_in_review.average_rating AS check_in_average_rating,
      |  COALESCE(first_manager_details.id, null) AS first_manager_id,
      |  COALESCE(first_manager_details.emp_id, null) AS first_manager_employee_id,
      |  COALESCE(first_manager_details.first_name, null) AS first_manager_first_name,
      |  COALESCE(first_manager_details.last_name, null) AS first_manager_last_name,
      |  COALESCE(second_manager_details.id, null) AS second_manager_id,
      |  COALESCE(second_manager_details.emp_id, null) AS second_manager_employee_id,
      |  COALESCE(second_manager_details.first_name, null) AS second_manager_first_name,
      |  COALESCE(second_manager_details.last_name, null) AS second_manager_last_name
      |FROM
      |  review_cycle
      |  INNER JOIN employees ON employees.organisation_id = review_cycle.organisation_id
      |  LEFT JOIN employee_manager_mapping AS firstManagerData ON
      |    (firstManagerData.emp_id = employees.id AND firstManagerData.type = 1
      |    AND firstManagerData.created_at::date <= review_cycle.self_review_end_date
      |    AND (firstManagerData.updated_at::date IS NULL OR firstManagerData.updated_at::date >= review_cycle.check_in_end_date))
      |  LEFT JOIN employee_manager_mapping AS secondManagerData ON
      |    (secondManagerData.emp_id = employees.id AND secondManagerData.type = 2
      |    AND secondManagerData.created_at::date <= review_cycle.self_review_end_date
      |    AND (secondManagerData.updated_at::date IS NULL OR secondManagerData.updated_at::date >= review_cycle.check_in_end_date))
      |  LEFT JOIN employees AS current_first_manager ON current_first_manager.id = firstManagerData.manager_id
      |  LEFT JOIN employees AS current_second_manager ON current_second_manager.id = secondManagerData.manager_id
      |  LEFT JOIN review_details AS self_review ON self_review.review_cycle_id = review_cycle.id
      |  AND self_review.review_to = employees.id
      |  AND self_review.review_type_id = 1
      |  LEFT JOIN review_details AS check_in_review ON check_in_review.review_cycle_id = review_cycle.id
      |  AND check_in_review.review_to = employees.id
      |  AND check_in_review.review_type_id = 3
      |  LEFT JOIN review_details AS first_manager_review ON first_manager_review.review_cycle_id = review_cycle.id
      |  AND first_manager_review.review_to = employees.id
      |  AND first_manager_review.review_type_id = 2
      |  AND first_manager_review.review_from IN (SELECT manager_id FROM employee_manager_mapping WHERE emp_id = employees.id AND type = 1
      |  AND first_manager_review.updated_at BETWEEN created_at AND COALESCE(updated_at, now()))
      |  LEFT JOIN review_details AS second_manager_review ON second_manager_review.review_cycle_id = review_cycle.id
      |  AND second_manager_review.review_to = employees.id
      |  AND second_manager_review.review_type_id = 2
      |  AND second_manager_review.review_from IN (SELECT manager_id FROM employee_manager_mapping WHERE emp_id = employees.id AND type = 2
      |  AND second_manager_review.updated_at BETWEEN created_at AND COALESCE(updated_at, now()))
      |  LEFT JOIN employees AS first_manager_details ON first_manager_details.id = first_manager_review.review_from
      |  LEFT JOIN employees AS second_manager_details ON second_manager_details.id = second_manager_review.review_from
      |WHERE
      |  review_cycle.organisation_id = ?
      |  AND employees.status = true
      |  AND employees.created_at::date <= review_cycle.end_date
      |  AND COALESCE (check_in_review.average_rating, 0.0) BETWEEN ? AND ?
      |  AND (?::INT[] = '{-99}' OR (current_first_manager.id = ANY (?::INT[])
      |  OR current_second_manager.id = ANY (?::INT[]))
      |  AND ((firstManagerData.manager_id IS NULL OR firstManagerData.emp_id != firstManagerData.manager_id)
      |  AND (secondManagerData.manager_id IS NULL OR secondManagerData.emp_id != secondManagerData.manager_id)))
      |  AND (?::INT[] = '{-99}' OR employees.id = ANY (?::INT[]))
      |  AND (?::INT[] = '{-99}' OR review_cycle.id = ANY (?::INT[]))
      |  AND (?::BOOLEAN IS NULL
      |  OR (COALESCE(self_review.draft, false) = ? AND COALESCE(self_review.published, false) = ?))
      |  AND (?::BOOLEAN IS NULL OR (COALESCE(first_manager_review.draft, false) = ?
      |  AND COALESCE(first_manager_review.published, false) = ?))
      |  AND (?::BOOLEAN IS NULL OR (COALESCE(second_manager_review.draft, false) = ?
      |  AND COALESCE(second_manager_review.published, false) = ?))
      |  AND (?::BOOLEAN IS NULL
      |  OR (COALESCE(check_in_review.draft, false) = ? AND COALESCE(check_in_review.published, false) = ?))
      |ORDER BY
      |  review_cycle.publish DESC,
      |  (daterange(review_cycle.start_date, review_cycle.end_date)) DESC;
      """.trimMargin()

  override val mapper: RowMapper<GetAllSummaryReviewCycleResult> =
      GetAllSummaryReviewCycleRowMapper()

  override val paramSetter: ParamSetter<GetAllSummaryReviewCycleParams> =
      GetAllSummaryReviewCycleParamSetter()
}

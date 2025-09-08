package reviewCycle

import java.math.BigDecimal
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetReviewTimelineDataParams(
  val reviewToId: Long?,
  val organisationId: Long?
)

class GetReviewTimelineDataParamSetter : ParamSetter<GetReviewTimelineDataParams> {
  override fun map(ps: PreparedStatement, params: GetReviewTimelineDataParams) {
    ps.setObject(1, params.reviewToId)
    ps.setObject(2, params.reviewToId)
    ps.setObject(3, params.reviewToId)
    ps.setObject(4, params.reviewToId)
    ps.setObject(5, params.reviewToId)
    ps.setObject(6, params.reviewToId)
    ps.setObject(7, params.reviewToId)
    ps.setObject(8, params.reviewToId)
    ps.setObject(9, params.organisationId)
    ps.setObject(10, params.organisationId)
    ps.setObject(11, params.organisationId)
    ps.setObject(12, params.organisationId)
  }
}

data class GetReviewTimelineDataResult(
  val id: Long,
  val organisationId: Long,
  val startDate: Date,
  val endDate: Date,
  val publish: Boolean,
  val selfReviewStartDate: Date,
  val selfReviewEndDate: Date,
  val managerReviewStartDate: Date,
  val managerReviewEndDate: Date,
  val checkInStartDate: Date,
  val checkInEndDate: Date,
  val selfReviewDraft: Boolean?,
  val selfReviewPublish: Boolean?,
  val selfReviewDate: Timestamp?,
  val selfAverageRating: BigDecimal?,
  val firstManagerReviewDraft: Boolean?,
  val firstManagerReviewPublish: Boolean?,
  val firstManagerReviewDate: Timestamp?,
  val firstManagerAverageRating: BigDecimal?,
  val secondManagerReviewDraft: Boolean?,
  val secondManagerReviewPublish: Boolean?,
  val secondManagerReviewDate: Timestamp?,
  val secondManagerAverageRating: BigDecimal?,
  val checkInFromId: Long?,
  val checkInWithManagerDraft: Boolean?,
  val checkInWithManagerPublish: Boolean?,
  val checkInWithManagerDate: Timestamp?,
  val checkInWithManagerAverageRating: BigDecimal?,
  val checkInFromEmployeeId: String?,
  val checkInFromFirstName: String?,
  val checkInFromLastName: String?,
  val firstManagerId: Long?,
  val firstManagerEmployeeId: String?,
  val firstManagerFirstName: String?,
  val firstManagerLastName: String?,
  val secondManagerId: Long?,
  val secondManagerEmployeeId: String?,
  val secondManagerFirstname: String?,
  val secondManagerLastName: String?,
  val isOrWasManager: Boolean?
)

class GetReviewTimelineDataRowMapper : RowMapper<GetReviewTimelineDataResult> {
  override fun map(rs: ResultSet): GetReviewTimelineDataResult = GetReviewTimelineDataResult(
  id = rs.getObject("id") as kotlin.Long,
    organisationId = rs.getObject("organisation_id") as kotlin.Long,
    startDate = rs.getObject("start_date") as java.sql.Date,
    endDate = rs.getObject("end_date") as java.sql.Date,
    publish = rs.getObject("publish") as kotlin.Boolean,
    selfReviewStartDate = rs.getObject("self_review_start_date") as java.sql.Date,
    selfReviewEndDate = rs.getObject("self_review_end_date") as java.sql.Date,
    managerReviewStartDate = rs.getObject("manager_review_start_date") as java.sql.Date,
    managerReviewEndDate = rs.getObject("manager_review_end_date") as java.sql.Date,
    checkInStartDate = rs.getObject("check_in_start_date") as java.sql.Date,
    checkInEndDate = rs.getObject("check_in_end_date") as java.sql.Date,
    selfReviewDraft = rs.getObject("self_review_draft") as kotlin.Boolean?,
    selfReviewPublish = rs.getObject("self_review_publish") as kotlin.Boolean?,
    selfReviewDate = rs.getObject("self_review_date") as java.sql.Timestamp?,
    selfAverageRating = rs.getObject("self_average_rating") as java.math.BigDecimal?,
    firstManagerReviewDraft = rs.getObject("first_manager_review_draft") as kotlin.Boolean?,
    firstManagerReviewPublish = rs.getObject("first_manager_review_publish") as kotlin.Boolean?,
    firstManagerReviewDate = rs.getObject("first_manager_review_date") as java.sql.Timestamp?,
    firstManagerAverageRating = rs.getObject("first_manager_average_rating") as
      java.math.BigDecimal?,
    secondManagerReviewDraft = rs.getObject("second_manager_review_draft") as kotlin.Boolean?,
    secondManagerReviewPublish = rs.getObject("second_manager_review_publish") as kotlin.Boolean?,
    secondManagerReviewDate = rs.getObject("second_manager_review_date") as java.sql.Timestamp?,
    secondManagerAverageRating = rs.getObject("second_manager_average_rating") as
      java.math.BigDecimal?,
    checkInFromId = rs.getObject("check_in_from_id") as kotlin.Long?,
    checkInWithManagerDraft = rs.getObject("check_in_with_manager_draft") as kotlin.Boolean?,
    checkInWithManagerPublish = rs.getObject("check_in_with_manager_publish") as kotlin.Boolean?,
    checkInWithManagerDate = rs.getObject("check_in_with_manager_date") as java.sql.Timestamp?,
    checkInWithManagerAverageRating = rs.getObject("check_in_with_manager_average_rating") as
      java.math.BigDecimal?,
    checkInFromEmployeeId = rs.getObject("check_in_from_employee_id") as kotlin.String?,
    checkInFromFirstName = rs.getObject("check_in_from_first_name") as kotlin.String?,
    checkInFromLastName = rs.getObject("check_in_from_last_name") as kotlin.String?,
    firstManagerId = rs.getObject("first_manager_id") as kotlin.Long?,
    firstManagerEmployeeId = rs.getObject("first_manager_employee_id") as kotlin.String?,
    firstManagerFirstName = rs.getObject("first_manager_first_name") as kotlin.String?,
    firstManagerLastName = rs.getObject("first_manager_last_name") as kotlin.String?,
    secondManagerId = rs.getObject("second_manager_id") as kotlin.Long?,
    secondManagerEmployeeId = rs.getObject("second_manager_employee_id") as kotlin.String?,
    secondManagerFirstname = rs.getObject("second_manager_firstname") as kotlin.String?,
    secondManagerLastName = rs.getObject("second_manager_last_name") as kotlin.String?,
    isOrWasManager = rs.getObject("is_or_was_manager") as kotlin.Boolean?)
}

class GetReviewTimelineDataQuery : Query<GetReviewTimelineDataParams, GetReviewTimelineDataResult> {
  override val sql: String = """
      |SELECT
      |  review_cycle.id,
      |  review_cycle.organisation_id AS organisation_id,
      |  review_cycle.start_date,
      |  review_cycle.end_date,
      |  review_cycle.publish,
      |  review_cycle.self_review_start_date,
      |  review_cycle.self_review_end_date,
      |  review_cycle.manager_review_start_date,
      |  review_cycle.manager_review_end_date,
      |  review_cycle.check_in_start_date,
      |  review_cycle.check_in_end_date,
      |  self_review_details.draft AS self_review_draft,
      |  self_review_details.published AS self_review_publish,
      |  self_review_details.updated_at AS self_review_date,
      |  self_review_details.average_rating AS self_average_rating,
      |  first_manager_review_details.draft AS first_manager_review_draft,
      |  first_manager_review_details.published AS first_manager_review_publish,
      |  first_manager_review_details.updated_at AS first_manager_review_date,
      |  first_manager_review_details.average_rating AS first_manager_average_rating,
      |  second_manager_review_details.draft AS second_manager_review_draft,
      |  second_manager_review_details.published AS second_manager_review_publish,
      |  second_manager_review_details.updated_at AS second_manager_review_date,
      |  second_manager_review_details.average_rating AS second_manager_average_rating,
      |  check_in_with_manager.review_from AS check_in_from_id,
      |  check_in_with_manager.draft AS check_in_with_manager_draft,
      |  check_in_with_manager.published AS check_in_with_manager_publish,
      |  check_in_with_manager.updated_at AS check_in_with_manager_date,
      |  check_in_with_manager.average_rating AS check_in_with_manager_average_rating,
      |  COALESCE(check_in_from_details.emp_id, null) AS check_in_from_employee_id,
      |  COALESCE(check_in_from_details.first_name, null) AS check_in_from_first_name,
      |  COALESCE(check_in_from_details.last_name, null) AS check_in_from_last_name,
      |  COALESCE(first_manager_details.id, null) as first_manager_id,
      |  COALESCE(first_manager_details.emp_id, null) as first_manager_employee_id,
      |  COALESCE(first_manager_details.first_name, null) as first_manager_first_name,
      |  COALESCE(first_manager_details.last_name, null) as first_manager_last_name,
      |  COALESCE(second_manager_details.id, null) as second_manager_id,
      |  COALESCE(second_manager_details.emp_id, null) as second_manager_employee_id,
      |  COALESCE(second_manager_details.first_name, null) as second_manager_firstname,
      |  COALESCE(second_manager_details.last_name, null) as second_manager_last_name,
      |  COALESCE((SELECT true FROM employee_manager_mapping WHERE employee_manager_mapping.manager_id = ? LIMIT 1), false) AS is_or_was_manager
      |FROM
      |  review_cycle
      |  LEFT JOIN employees ON employees.id = ?
      |  LEFT JOIN employee_manager_mapping_view AS firstManagerMapping ON firstManagerMapping.emp_id = ?
      |  AND firstManagerMapping.type = 1
      |  LEFT JOIN employee_manager_mapping_view AS secondManagerMapping ON secondManagerMapping.emp_id = ?
      |  AND secondManagerMapping.type = 2
      |  LEFT JOIN review_details AS self_review_details
      |  ON self_review_details.review_cycle_id = review_cycle.id
      |  AND self_review_details.review_type_id = 1
      |  AND self_review_details.review_to = ?
      |  LEFT JOIN review_details AS first_manager_review_details
      |  ON first_manager_review_details.review_cycle_id = review_cycle.id
      |  AND first_manager_review_details.review_type_id = 2
      |  AND first_manager_review_details.review_to = ?
      |  AND first_manager_review_details.review_from = firstManagerMapping.manager_id
      |  LEFT JOIN review_details AS second_manager_review_details
      |  ON second_manager_review_details.review_cycle_id = review_cycle.id
      |  AND second_manager_review_details.review_type_id = 2
      |  AND second_manager_review_details.review_to = ?
      |  AND second_manager_review_details.review_from = secondManagerMapping.manager_id
      |  LEFT JOIN review_details AS check_in_with_manager
      |  ON check_in_with_manager.review_cycle_id = review_cycle.id
      |  AND check_in_with_manager.review_type_id = 3
      |  AND check_in_with_manager.review_to = ?
      |  LEFT JOIN employees AS first_manager_details
      |  ON first_manager_details.id = firstManagerMapping.manager_id
      |  AND first_manager_details.status = true
      |  AND first_manager_details.organisation_id = ?
      |  LEFT JOIN employees AS second_manager_details
      |  ON second_manager_details.id = secondManagerMapping.manager_id
      |  AND second_manager_details.status = true
      |  AND second_manager_details.organisation_id = ?
      |  LEFT JOIN employees AS check_in_from_details
      |  ON check_in_from_details.id = check_in_with_manager.review_from
      |  AND check_in_from_details.organisation_id = ?
      |WHERE
      |  review_cycle.organisation_id = ?
      |  AND employees.status = true
      |  AND review_cycle.publish = true
      |ORDER BY
      |  review_cycle.start_date DESC limit 1;
      |""".trimMargin()

  override val mapper: RowMapper<GetReviewTimelineDataResult> = GetReviewTimelineDataRowMapper()

  override val paramSetter: ParamSetter<GetReviewTimelineDataParams> =
      GetReviewTimelineDataParamSetter()
}

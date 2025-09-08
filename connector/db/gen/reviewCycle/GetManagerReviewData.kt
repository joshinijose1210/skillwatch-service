package reviewCycle

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetManagerReviewDataParams(
  val reviewCycleId: Long?,
  val organisationId: Long?,
  val reviewFromId: Long?
)

class GetManagerReviewDataParamSetter : ParamSetter<GetManagerReviewDataParams> {
  override fun map(ps: PreparedStatement, params: GetManagerReviewDataParams) {
    ps.setObject(1, params.reviewCycleId)
    ps.setObject(2, params.reviewCycleId)
    ps.setObject(3, params.reviewCycleId)
    ps.setObject(4, params.reviewCycleId)
    ps.setObject(5, params.organisationId)
    ps.setObject(6, params.reviewFromId)
    ps.setObject(7, params.reviewFromId)
    ps.setObject(8, params.reviewFromId)
    ps.setObject(9, params.reviewFromId)
    ps.setObject(10, params.organisationId)
  }
}

data class GetManagerReviewDataResult(
  val id: Long,
  val employeeId: String,
  val firstName: String,
  val lastName: String,
  val firstManagerId: Long?,
  val secondManagerId: Long?,
  val selfReviewDraft: Boolean?,
  val selfReviewPublish: Boolean?,
  val selfReviewDate: Date?,
  val firstManagerReviewDraft: Boolean?,
  val firstManagerReviewPublished: Boolean?,
  val firstManagerReviewDate: Date?,
  val secondManagerReviewDraft: Boolean?,
  val secondManagerReviewPublished: Boolean?,
  val secondManagerReviewDate: Date?,
  val checkInFromId: Long?,
  val checkInFromEmployeeId: String?,
  val checkInFromFirstName: String?,
  val checkInFromLastName: String?,
  val checkInWithManagerDraft: Boolean?,
  val checkInWithManagerPublish: Boolean?,
  val checkInWithManagerDate: Date?
)

class GetManagerReviewDataRowMapper : RowMapper<GetManagerReviewDataResult> {
  override fun map(rs: ResultSet): GetManagerReviewDataResult = GetManagerReviewDataResult(
  id = rs.getObject("id") as kotlin.Long,
    employeeId = rs.getObject("employee_id") as kotlin.String,
    firstName = rs.getObject("first_name") as kotlin.String,
    lastName = rs.getObject("last_name") as kotlin.String,
    firstManagerId = rs.getObject("first_manager_id") as kotlin.Long?,
    secondManagerId = rs.getObject("second_manager_id") as kotlin.Long?,
    selfReviewDraft = rs.getObject("self_review_draft") as kotlin.Boolean?,
    selfReviewPublish = rs.getObject("self_review_publish") as kotlin.Boolean?,
    selfReviewDate = rs.getObject("self_review_date") as java.sql.Date?,
    firstManagerReviewDraft = rs.getObject("first_manager_review_draft") as kotlin.Boolean?,
    firstManagerReviewPublished = rs.getObject("first_manager_review_published") as kotlin.Boolean?,
    firstManagerReviewDate = rs.getObject("first_manager_review_date") as java.sql.Date?,
    secondManagerReviewDraft = rs.getObject("second_manager_review_draft") as kotlin.Boolean?,
    secondManagerReviewPublished = rs.getObject("second_manager_review_published") as
      kotlin.Boolean?,
    secondManagerReviewDate = rs.getObject("second_manager_review_date") as java.sql.Date?,
    checkInFromId = rs.getObject("check_in_from_id") as kotlin.Long?,
    checkInFromEmployeeId = rs.getObject("check_in_from_employee_id") as kotlin.String?,
    checkInFromFirstName = rs.getObject("check_in_from_first_name") as kotlin.String?,
    checkInFromLastName = rs.getObject("check_in_from_last_name") as kotlin.String?,
    checkInWithManagerDraft = rs.getObject("check_in_with_manager_draft") as kotlin.Boolean?,
    checkInWithManagerPublish = rs.getObject("check_in_with_manager_publish") as kotlin.Boolean?,
    checkInWithManagerDate = rs.getObject("check_in_with_manager_date") as java.sql.Date?)
}

class GetManagerReviewDataQuery : Query<GetManagerReviewDataParams, GetManagerReviewDataResult> {
  override val sql: String = """
      |SELECT
      |  employees.id,
      |  employees.emp_id AS employee_id,
      |  employees.first_name,
      |  employees.last_name,
      |  firstManagerMapping.manager_id AS first_manager_id,
      |  COALESCE(secondManagerMapping.manager_id, null) AS second_manager_id,
      |  self_review_details.draft AS self_review_draft,
      |  self_review_details.published AS self_review_publish,
      |  self_review_details.updated_at::DATE AS self_review_date,
      |  first_manager_review_details.draft AS first_manager_review_draft,
      |  first_manager_review_details.published AS first_manager_review_published,
      |  first_manager_review_details.updated_at::DATE AS first_manager_review_date,
      |  second_manager_review_details.draft AS second_manager_review_draft,
      |  second_manager_review_details.published AS second_manager_review_published,
      |  second_manager_review_details.updated_at::DATE AS second_manager_review_date,
      |  check_in_with_manager.review_from AS check_in_from_id,
      |  COALESCE(check_in_from_details.emp_id, null) AS check_in_from_employee_id,
      |  COALESCE(check_in_from_details.first_name, null) AS check_in_from_first_name,
      |  COALESCE(check_in_from_details.last_name, null) AS check_in_from_last_name,
      |  check_in_with_manager.draft AS check_in_with_manager_draft,
      |  check_in_with_manager.published AS check_in_with_manager_publish,
      |  check_in_with_manager.updated_at::DATE AS check_in_with_manager_date
      |FROM
      |  employees
      |  JOIN employee_manager_mapping_view AS firstManagerMapping ON employees.id = firstManagerMapping.emp_id
      |  AND firstManagerMapping.type = 1
      |  LEFT JOIN employee_manager_mapping_view AS secondManagerMapping ON employees.id = secondManagerMapping.emp_id
      |  AND secondManagerMapping.type = 2
      |  LEFT JOIN review_details AS first_manager_review_details
      |  ON first_manager_review_details.review_cycle_id = ?
      |  AND first_manager_review_details.review_type_id = 2
      |  AND first_manager_review_details.review_to = employees.id
      |  AND first_manager_review_details.review_from = firstManagerMapping.manager_id
      |  LEFT JOIN review_details AS second_manager_review_details
      |  ON second_manager_review_details.review_cycle_id = ?
      |  AND second_manager_review_details.review_type_id = 2
      |  AND second_manager_review_details.review_to = employees.id
      |  AND second_manager_review_details.review_from = secondManagerMapping.manager_id
      |  LEFT JOIN review_details AS check_in_with_manager
      |  ON check_in_with_manager.review_cycle_id = ?
      |  AND check_in_with_manager.review_type_id = 3
      |  AND check_in_with_manager.review_to = employees.id
      |  LEFT JOIN review_details AS self_review_details
      |  ON self_review_details.review_cycle_id = ?
      |  AND self_review_details.review_type_id = 1
      |  AND self_review_details.review_to = employees.id
      |  AND self_review_details.review_from = employees.id
      |  LEFT JOIN employees AS check_in_from_details
      |  ON check_in_from_details.id = check_in_with_manager.review_from
      |  AND check_in_from_details.organisation_id = ?
      |WHERE
      |  (firstManagerMapping.manager_id = ? OR secondManagerMapping.manager_id = ?)
      |  AND (firstManagerMapping.emp_id != ? OR secondManagerMapping.emp_id != ?)
      |  AND employees.organisation_id = ?
      |  AND employees.status = true;
      |""".trimMargin()

  override val mapper: RowMapper<GetManagerReviewDataResult> = GetManagerReviewDataRowMapper()

  override val paramSetter: ParamSetter<GetManagerReviewDataParams> =
      GetManagerReviewDataParamSetter()
}

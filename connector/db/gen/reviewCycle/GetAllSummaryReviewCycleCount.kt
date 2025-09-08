package reviewCycle

import java.math.BigDecimal
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

data class GetAllSummaryReviewCycleCountParams(
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

class GetAllSummaryReviewCycleCountParamSetter : ParamSetter<GetAllSummaryReviewCycleCountParams> {
  override fun map(ps: PreparedStatement, params: GetAllSummaryReviewCycleCountParams) {
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

data class GetAllSummaryReviewCycleCountResult(
  val summaryReviewCount: Long?
)

class GetAllSummaryReviewCycleCountRowMapper : RowMapper<GetAllSummaryReviewCycleCountResult> {
  override fun map(rs: ResultSet): GetAllSummaryReviewCycleCountResult =
      GetAllSummaryReviewCycleCountResult(
  summaryReviewCount = rs.getObject("summary_review_count") as kotlin.Long?)
}

class GetAllSummaryReviewCycleCountQuery : Query<GetAllSummaryReviewCycleCountParams,
    GetAllSummaryReviewCycleCountResult> {
  override val sql: String = """
      |SELECT
      |COUNT(review_cycle.id) AS summary_review_count
      |FROM
      |  review_cycle
      |  INNER JOIN employees ON employees.organisation_id = review_cycle.organisation_id
      |  LEFT JOIN employee_manager_mapping AS firstManagerData ON
      |      (firstManagerData.emp_id = employees.id AND firstManagerData.type = 1
      |      AND firstManagerData.created_at::date <= review_cycle.self_review_end_date
      |      AND (firstManagerData.updated_at::date IS NULL OR firstManagerData.updated_at::date >= review_cycle.end_date))
      |  LEFT JOIN employee_manager_mapping AS secondManagerData ON
      |      (secondManagerData.emp_id = employees.id AND secondManagerData.type = 2
      |      AND secondManagerData.created_at::date <= review_cycle.self_review_end_date
      |      AND (secondManagerData.updated_at::date IS NULL OR secondManagerData.updated_at::date >= review_cycle.end_date))
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
      |  OR (COALESCE(check_in_review.draft, false) = ? AND COALESCE(check_in_review.published, false) = ?));
      |
      |""".trimMargin()

  override val mapper: RowMapper<GetAllSummaryReviewCycleCountResult> =
      GetAllSummaryReviewCycleCountRowMapper()

  override val paramSetter: ParamSetter<GetAllSummaryReviewCycleCountParams> =
      GetAllSummaryReviewCycleCountParamSetter()
}

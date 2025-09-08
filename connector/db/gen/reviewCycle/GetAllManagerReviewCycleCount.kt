package reviewCycle

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

data class GetAllManagerReviewCycleCountParams(
  val reviewFromId: Long?,
  val reviewTypeId: Int?,
  val organisationId: Long?,
  val reviewToId: Array<Int>?,
  val reviewCycleId: Array<Int>?,
  val managerReviewDraft: Boolean?,
  val managerReviewPublished: Boolean?
)

class GetAllManagerReviewCycleCountParamSetter : ParamSetter<GetAllManagerReviewCycleCountParams> {
  override fun map(ps: PreparedStatement, params: GetAllManagerReviewCycleCountParams) {
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
  }
}

data class GetAllManagerReviewCycleCountResult(
  val managerReviewCycleCount: Long?
)

class GetAllManagerReviewCycleCountRowMapper : RowMapper<GetAllManagerReviewCycleCountResult> {
  override fun map(rs: ResultSet): GetAllManagerReviewCycleCountResult =
      GetAllManagerReviewCycleCountResult(
  managerReviewCycleCount = rs.getObject("manager_review_cycle_count") as kotlin.Long?)
}

class GetAllManagerReviewCycleCountQuery : Query<GetAllManagerReviewCycleCountParams,
    GetAllManagerReviewCycleCountResult> {
  override val sql: String = """
      |SELECT COUNT(review_cycle.start_date) AS manager_review_cycle_count
      |FROM
      |  review_cycle
      |  LEFT JOIN employee_manager_mapping AS emm
      |      ON ((emm.manager_id = ? AND type = 1)
      |          OR (emm.manager_id = ? AND type = 2))
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
      |  AND COALESCE(review_details.published, false) = ?));
      """.trimMargin()

  override val mapper: RowMapper<GetAllManagerReviewCycleCountResult> =
      GetAllManagerReviewCycleCountRowMapper()

  override val paramSetter: ParamSetter<GetAllManagerReviewCycleCountParams> =
      GetAllManagerReviewCycleCountParamSetter()
}

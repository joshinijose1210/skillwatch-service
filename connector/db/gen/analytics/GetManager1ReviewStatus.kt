package analytics

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetManager1ReviewStatusParams(
  val organisationId: Long?,
  val reviewCycleId: Long?
)

class GetManager1ReviewStatusParamSetter : ParamSetter<GetManager1ReviewStatusParams> {
  override fun map(ps: PreparedStatement, params: GetManager1ReviewStatusParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.reviewCycleId)
  }
}

data class GetManager1ReviewStatusResult(
  val inProgress: Long?,
  val completed: Long?
)

class GetManager1ReviewStatusRowMapper : RowMapper<GetManager1ReviewStatusResult> {
  override fun map(rs: ResultSet): GetManager1ReviewStatusResult = GetManager1ReviewStatusResult(
  inProgress = rs.getObject("in_progress") as kotlin.Long?,
    completed = rs.getObject("completed") as kotlin.Long?)
}

class GetManager1ReviewStatusQuery : Query<GetManager1ReviewStatusParams,
    GetManager1ReviewStatusResult> {
  override val sql: String = """
      |SELECT
      |  SUM(CASE WHEN first_manager_review.draft = true AND first_manager_review.published = false THEN 1 ELSE 0 END) AS in_progress,
      |  SUM(CASE WHEN first_manager_review.published = true AND first_manager_review.draft = false THEN 1 ELSE 0 END) AS completed
      |FROM
      |  review_cycle
      |  INNER JOIN employees ON employees.organisation_id = review_cycle.organisation_id
      |  JOIN employees_history ON employees_history.employee_id = employees.id
      |    AND (DATE(employees_history.activated_at) <= review_cycle.end_date)
      |    AND (DATE(employees_history.deactivated_at) IS NULL OR DATE(employees_history.deactivated_at) >= review_cycle.start_date)
      |  LEFT JOIN employee_manager_mapping_view AS firstManagerData ON firstManagerData.emp_id = employees.id
      |  AND firstManagerData.type = 1
      |  LEFT JOIN employees AS current_first_manager ON current_first_manager.id = firstManagerData.manager_id
      |  LEFT JOIN review_details AS first_manager_review ON first_manager_review.review_cycle_id = review_cycle.id
      |  AND first_manager_review.review_to = employees.id
      |  AND first_manager_review.review_type_id = 2
      |  AND first_manager_review.review_from IN (SELECT manager_id FROM employee_manager_mapping WHERE emp_id = employees.id AND type = 1
      |  AND first_manager_review.updated_at BETWEEN created_at AND COALESCE(updated_at, now()))
      |WHERE
      |  review_cycle.organisation_id = ?
      |  AND (firstManagerData.manager_id IS NULL OR firstManagerData.emp_id != firstManagerData.manager_id)
      |  AND review_cycle.id = ?;
      """.trimMargin()

  override val mapper: RowMapper<GetManager1ReviewStatusResult> = GetManager1ReviewStatusRowMapper()

  override val paramSetter: ParamSetter<GetManager1ReviewStatusParams> =
      GetManager1ReviewStatusParamSetter()
}

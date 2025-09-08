package dashboard

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Array
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetEmployeeFeedbacksCountParams(
  val reviewCycleId: Array<Int>?,
  val organisationId: Long?,
  val id: Array<Int>?,
  val feedbackTypeId: Array<Int>?
)

class GetEmployeeFeedbacksCountParamSetter : ParamSetter<GetEmployeeFeedbacksCountParams> {
  override fun map(ps: PreparedStatement, params: GetEmployeeFeedbacksCountParams) {
    ps.setArray(1, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setObject(2, params.organisationId)
    ps.setArray(3, ps.connection.createArrayOf("int4", params.id))
    ps.setArray(4, ps.connection.createArrayOf("int4", params.id))
    ps.setArray(5, ps.connection.createArrayOf("int4", params.feedbackTypeId))
  }
}

data class GetEmployeeFeedbacksCountResult(
  val employeeFeedbacksCount: Long?
)

class GetEmployeeFeedbacksCountRowMapper : RowMapper<GetEmployeeFeedbacksCountResult> {
  override fun map(rs: ResultSet): GetEmployeeFeedbacksCountResult =
      GetEmployeeFeedbacksCountResult(
  employeeFeedbacksCount = rs.getObject("employee_feedbacks_count") as kotlin.Long?)
}

class GetEmployeeFeedbacksCountQuery : Query<GetEmployeeFeedbacksCountParams,
    GetEmployeeFeedbacksCountResult> {
  override val sql: String = """
      |SELECT COUNT(feedback_types.name) AS employee_feedbacks_count
      |FROM
      |  (
      |    SELECT
      |      employees.id,
      |      employees.emp_id,
      |      employees.first_name,
      |      employees.last_name,
      |      review_cycle.id AS review_cycle_id,
      |      review_cycle.start_date AS start_date,
      |      review_cycle.end_date AS end_date
      |    FROM
      |      employees
      |      INNER JOIN review_cycle ON employees.organisation_id = review_cycle.organisation_id
      |    WHERE
      |      review_cycle.id = ANY(?::INT[])
      |      AND employees.organisation_id = ?
      |  ) AS review
      |  LEFT JOIN feedbacks ON review.id = feedbacks.feedback_to
      |    AND DATE(feedbacks.updated_at) BETWEEN review.start_date AND review.end_date
      |  JOIN feedback_types ON feedbacks.feedback_type_id = feedback_types.id
      |  LEFT JOIN employees_role_mapping AS feedback_to_role_mapping ON feedback_to_role_mapping.emp_id = review.id
      |  LEFT JOIN roles AS feedback_to_role ON feedback_to_role.id = feedback_to_role_mapping.role_id
      |  LEFT JOIN employees AS feedback_from ON feedbacks.feedback_from = feedback_from.id
      |  LEFT JOIN employees_role_mapping AS feedback_from_role_mapping ON feedback_from_role_mapping.emp_id = feedback_from.id
      |  LEFT JOIN roles AS feedback_from_role ON feedback_from_role.id = feedback_from_role_mapping.role_id
      |  LEFT JOIN external_feedback_emails AS external_email ON feedbacks.feedback_from_external_id = external_email.id
      |  WHERE
      |    feedbacks.is_draft = false
      |    AND CASE WHEN ?::INT[] = '{-99}' THEN 1 = 1 ELSE feedbacks.feedback_to = ANY (?::INT[]) END
      |    AND feedbacks.feedback_type_id = ANY (?::INT[])
      |    AND DATE(feedbacks.updated_at) BETWEEN review.start_date AND review.end_date ;
      |""".trimMargin()

  override val mapper: RowMapper<GetEmployeeFeedbacksCountResult> =
      GetEmployeeFeedbacksCountRowMapper()

  override val paramSetter: ParamSetter<GetEmployeeFeedbacksCountParams> =
      GetEmployeeFeedbacksCountParamSetter()
}

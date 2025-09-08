package feedbacks

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Array
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAllFeedbacksCountParams(
  val organisationId: Long?,
  val fromDate: String?,
  val toDate: String?,
  val search: String?,
  val feedbackTypeId: Array<Int>?,
  val reviewCycleId: Array<Int>?
)

class GetAllFeedbacksCountParamSetter : ParamSetter<GetAllFeedbacksCountParams> {
  override fun map(ps: PreparedStatement, params: GetAllFeedbacksCountParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.fromDate)
    ps.setObject(3, params.toDate)
    ps.setObject(4, params.fromDate)
    ps.setObject(5, params.toDate)
    ps.setObject(6, params.search)
    ps.setObject(7, params.search)
    ps.setObject(8, params.search)
    ps.setObject(9, params.search)
    ps.setObject(10, params.search)
    ps.setObject(11, params.search)
    ps.setObject(12, params.search)
    ps.setObject(13, params.search)
    ps.setObject(14, params.search)
    ps.setObject(15, params.search)
    ps.setArray(16, ps.connection.createArrayOf("int4", params.feedbackTypeId))
    ps.setArray(17, ps.connection.createArrayOf("int4", params.feedbackTypeId))
    ps.setObject(18, params.organisationId)
    ps.setArray(19, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setArray(20, ps.connection.createArrayOf("int4", params.reviewCycleId))
  }
}

data class GetAllFeedbacksCountResult(
  val feedbackCount: Long?
)

class GetAllFeedbacksCountRowMapper : RowMapper<GetAllFeedbacksCountResult> {
  override fun map(rs: ResultSet): GetAllFeedbacksCountResult = GetAllFeedbacksCountResult(
  feedbackCount = rs.getObject("feedback_count") as kotlin.Long?)
}

class GetAllFeedbacksCountQuery : Query<GetAllFeedbacksCountParams, GetAllFeedbacksCountResult> {
  override val sql: String = """
      |SELECT COUNT(feedbacks.sr_no) AS feedback_count
      |FROM
      |  feedbacks
      |  JOIN employees AS feedback_to_details ON feedback_to_details.id = feedbacks.feedback_to
      |  JOIN employees_role_mapping_view AS feedback_to_role ON feedback_to_details.id = feedback_to_role.emp_id
      |  LEFT JOIN employees AS feedback_from_details ON feedback_from_details.id = feedbacks.feedback_from
      |  LEFT JOIN employees_role_mapping_view AS feedback_from_role ON feedback_from_details.id = feedback_from_role.emp_id
      |  LEFT JOIN external_feedback_emails AS external_email ON external_email.id = feedbacks.feedback_from_external_id
      |  INNER JOIN feedback_types ON feedbacks.feedback_type_id = feedback_types.id
      |  LEFT JOIN review_cycle ON DATE(feedbacks.updated_at) BETWEEN review_cycle.start_date AND review_cycle.end_date
      |  AND review_cycle.organisation_id = ?
      |WHERE
      |  feedbacks.is_draft = false
      |  AND( (CAST (?  AS text) IS  NULL OR  CAST(?  AS text)IS NULL) OR
      |        feedbacks.updated_at::DATE BETWEEN ?::DATE AND ?::DATE)
      |  AND( (CAST(? as text) IS NULL)
      |            OR feedbacks.feedback_to IN
      |            (SELECT id FROM employees WHERE
      |              (UPPER(employees.emp_id) LIKE  UPPER(?)
      |              OR UPPER(employees.first_name) LIKE  UPPER(?)
      |              OR UPPER(employees.last_name) LIKE  UPPER(?)
      |              OR UPPER(employees.first_name) || ' ' || UPPER(employees.last_name) LIKE  UPPER(?) )
      |            )
      |            OR feedbacks.feedback_from IN
      |            (SELECT id FROM employees WHERE
      |              (UPPER(employees.emp_id) LIKE  UPPER(?)
      |              OR UPPER(employees.first_name) LIKE UPPER(?)
      |              OR UPPER(employees.last_name) LIKE UPPER(?)
      |              OR UPPER(employees.first_name) || ' ' || UPPER(employees.last_name) LIKE UPPER(?) )
      |            )
      |            OR feedbacks.feedback_from_external_id IN
      |            (SELECT id FROM external_feedback_emails WHERE
      |                (UPPER(external_feedback_emails.email_id) LIKE UPPER(?))
      |            )
      |        )
      |  AND (?::INT[] = '{-99}' OR feedbacks.feedback_type_id = ANY(?::INT[]))
      |  AND feedback_to_details.organisation_id = ?
      |  AND (?::INT[] = '{-99}' OR review_cycle.id = ANY(?::INT[]));
      """.trimMargin()

  override val mapper: RowMapper<GetAllFeedbacksCountResult> = GetAllFeedbacksCountRowMapper()

  override val paramSetter: ParamSetter<GetAllFeedbacksCountParams> =
      GetAllFeedbacksCountParamSetter()
}

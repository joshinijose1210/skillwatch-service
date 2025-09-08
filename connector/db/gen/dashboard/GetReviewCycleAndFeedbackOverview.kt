package dashboard

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetReviewCycleAndFeedbackOverviewParams(
  val organisationId: Long?,
  val id: Long?
)

class GetReviewCycleAndFeedbackOverviewParamSetter :
    ParamSetter<GetReviewCycleAndFeedbackOverviewParams> {
  override fun map(ps: PreparedStatement, params: GetReviewCycleAndFeedbackOverviewParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.id)
  }
}

data class GetReviewCycleAndFeedbackOverviewResult(
  val reviewCycleId: Long,
  val firstName: String,
  val startDate: Date,
  val endDate: Date,
  val selfReviewStartDate: Date,
  val selfReviewEndDate: Date,
  val selfreviewdraft: Boolean?,
  val selfreviewpublish: Boolean?,
  val positive: Long?,
  val improvement: Long?,
  val appreciation: Long?
)

class GetReviewCycleAndFeedbackOverviewRowMapper :
    RowMapper<GetReviewCycleAndFeedbackOverviewResult> {
  override fun map(rs: ResultSet): GetReviewCycleAndFeedbackOverviewResult =
      GetReviewCycleAndFeedbackOverviewResult(
  reviewCycleId = rs.getObject("review_cycle_id") as kotlin.Long,
    firstName = rs.getObject("first_name") as kotlin.String,
    startDate = rs.getObject("start_date") as java.sql.Date,
    endDate = rs.getObject("end_date") as java.sql.Date,
    selfReviewStartDate = rs.getObject("self_review_start_date") as java.sql.Date,
    selfReviewEndDate = rs.getObject("self_review_end_date") as java.sql.Date,
    selfreviewdraft = rs.getObject("selfreviewdraft") as kotlin.Boolean?,
    selfreviewpublish = rs.getObject("selfreviewpublish") as kotlin.Boolean?,
    positive = rs.getObject("positive") as kotlin.Long?,
    improvement = rs.getObject("improvement") as kotlin.Long?,
    appreciation = rs.getObject("appreciation") as kotlin.Long?)
}

class GetReviewCycleAndFeedbackOverviewQuery : Query<GetReviewCycleAndFeedbackOverviewParams,
    GetReviewCycleAndFeedbackOverviewResult> {
  override val sql: String = """
      |SELECT
      |  review.review_cycle_id,
      |  review.first_name,
      |  review.start_date,
      |  review.end_date,
      |  review.self_review_start_date,
      |  review.self_review_end_date,
      |  review.selfReviewDraft,
      |  review.selfReviewPublish,
      |  COUNT(CASE WHEN feedback_type_id = 1 THEN feedback_type_id END) AS Positive,
      |  COUNT(CASE WHEN feedback_type_id = 2 THEN feedback_type_id END) AS Improvement,
      |  COUNT(CASE WHEN feedback_type_id = 3 THEN feedback_type_id END) AS Appreciation
      |FROM
      |  (SELECT
      |     employees.id,
      |     employees.emp_id,
      |     employees.first_name,
      |     review_cycle.id AS review_cycle_id,
      |     review_cycle.start_date ,
      |     review_cycle.end_date ,
      |     review_cycle.self_review_start_date,
      |     review_cycle.self_review_end_date,
      |     review_details.draft AS selfReviewDraft,
      |     review_details.published AS selfReviewPublish
      |  FROM
      |    employees
      |    INNER JOIN review_cycle ON employees.organisation_id = review_cycle.organisation_id
      |    LEFT JOIN review_details ON review_details.review_cycle_id = review_cycle.id
      |    AND review_details.review_to = employees.id
      |    AND review_details.review_type_id = 1
      |  WHERE
      |    review_cycle.publish = true
      |    AND employees.organisation_id = ?
      |    AND employees.id = ?
      |  ) AS review
      |  LEFT JOIN feedbacks ON review.id = feedbacks.feedback_to
      |  AND feedbacks.is_draft = false
      |  AND feedbacks.updated_at BETWEEN review.start_date AND review.end_date
      |GROUP BY
      |  review.review_cycle_id,
      |  review.first_name,
      |  review.start_date,
      |  review.end_date,
      |  review.self_review_start_date,
      |  review.self_review_end_date,
      |  review.selfReviewDraft,
      |  review.selfReviewPublish;
      """.trimMargin()

  override val mapper: RowMapper<GetReviewCycleAndFeedbackOverviewResult> =
      GetReviewCycleAndFeedbackOverviewRowMapper()

  override val paramSetter: ParamSetter<GetReviewCycleAndFeedbackOverviewParams> =
      GetReviewCycleAndFeedbackOverviewParamSetter()
}

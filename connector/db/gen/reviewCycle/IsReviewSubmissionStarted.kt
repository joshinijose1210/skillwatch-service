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

data class IsReviewSubmissionStartedParams(
  val currentDate: Date?,
  val organisationId: Long?
)

class IsReviewSubmissionStartedParamSetter : ParamSetter<IsReviewSubmissionStartedParams> {
  override fun map(ps: PreparedStatement, params: IsReviewSubmissionStartedParams) {
    ps.setObject(1, params.currentDate)
    ps.setObject(2, params.organisationId)
  }
}

data class IsReviewSubmissionStartedResult(
  val exists: Boolean?
)

class IsReviewSubmissionStartedRowMapper : RowMapper<IsReviewSubmissionStartedResult> {
  override fun map(rs: ResultSet): IsReviewSubmissionStartedResult =
      IsReviewSubmissionStartedResult(
  exists = rs.getObject("exists") as kotlin.Boolean?)
}

class IsReviewSubmissionStartedQuery : Query<IsReviewSubmissionStartedParams,
    IsReviewSubmissionStartedResult> {
  override val sql: String = """
      |WITH subquery AS (
      |    SELECT id
      |    FROM review_cycle
      |    WHERE ? BETWEEN manager_review_start_date AND check_in_end_date
      |    AND publish = true
      |    AND organisation_id = ?
      |)
      |SELECT EXISTS (SELECT 1 FROM subquery);
      """.trimMargin()

  override val mapper: RowMapper<IsReviewSubmissionStartedResult> =
      IsReviewSubmissionStartedRowMapper()

  override val paramSetter: ParamSetter<IsReviewSubmissionStartedParams> =
      IsReviewSubmissionStartedParamSetter()
}

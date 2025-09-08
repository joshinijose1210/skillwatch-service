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

data class IsSelfReviewStartedParams(
  val todayDate: Date?,
  val organisationId: Long?
)

class IsSelfReviewStartedParamSetter : ParamSetter<IsSelfReviewStartedParams> {
  override fun map(ps: PreparedStatement, params: IsSelfReviewStartedParams) {
    ps.setObject(1, params.todayDate)
    ps.setObject(2, params.organisationId)
  }
}

data class IsSelfReviewStartedResult(
  val exists: Boolean?,
  val id: Long?
)

class IsSelfReviewStartedRowMapper : RowMapper<IsSelfReviewStartedResult> {
  override fun map(rs: ResultSet): IsSelfReviewStartedResult = IsSelfReviewStartedResult(
  exists = rs.getObject("exists") as kotlin.Boolean?,
    id = rs.getObject("id") as kotlin.Long?)
}

class IsSelfReviewStartedQuery : Query<IsSelfReviewStartedParams, IsSelfReviewStartedResult> {
  override val sql: String = """
      |WITH subquery AS (
      |    SELECT id
      |    FROM review_cycle
      |    WHERE self_review_start_date = ?
      |    AND publish = true
      |    AND organisation_id = ?
      |)
      |SELECT EXISTS (SELECT 1 FROM subquery), (SELECT id FROM subquery);
      """.trimMargin()

  override val mapper: RowMapper<IsSelfReviewStartedResult> = IsSelfReviewStartedRowMapper()

  override val paramSetter: ParamSetter<IsSelfReviewStartedParams> =
      IsSelfReviewStartedParamSetter()
}

package review

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetReviewParams(
  val reviewId: Long?
)

class GetReviewParamSetter : ParamSetter<GetReviewParams> {
  override fun map(ps: PreparedStatement, params: GetReviewParams) {
    ps.setObject(1, params.reviewId)
  }
}

data class GetReviewResult(
  val reviewId: Long,
  val kpiId: Long?,
  val title: String,
  val description: String,
  val review: String?,
  val rating: Int?,
  val kraId: Long?,
  val kraName: String?
)

class GetReviewRowMapper : RowMapper<GetReviewResult> {
  override fun map(rs: ResultSet): GetReviewResult = GetReviewResult(
  reviewId = rs.getObject("review_id") as kotlin.Long,
    kpiId = rs.getObject("kpi_id") as kotlin.Long?,
    title = rs.getObject("title") as kotlin.String,
    description = rs.getObject("description") as kotlin.String,
    review = rs.getObject("review") as kotlin.String?,
    rating = rs.getObject("rating") as kotlin.Int?,
    kraId = rs.getObject("kra_id") as kotlin.Long?,
    kraName = rs.getObject("kra_name") as kotlin.String?)
}

class GetReviewQuery : Query<GetReviewParams, GetReviewResult> {
  override val sql: String = """
      |SELECT
      |  reviews.id AS review_id,
      |  reviews.kpi_id,
      |  kpi.title,
      |  kpi.description,
      |  reviews.review,
      |  reviews.rating,
      |  COALESCE(kkm.kra_id, null) AS kra_id,
      |  COALESCE(kra.name, null) As kra_name
      |FROM
      |  reviews
      |  INNER JOIN kpi ON reviews.kpi_id = kpi.id
      |  LEFT JOIN kra_kpi_mapping AS kkm ON kkm.kpi_id = reviews.kpi_id
      |  LEFT JOIN kra ON kra.id = kkm.kra_id
      |WHERE
      |  reviews.id = ?;
      """.trimMargin()

  override val mapper: RowMapper<GetReviewResult> = GetReviewRowMapper()

  override val paramSetter: ParamSetter<GetReviewParams> = GetReviewParamSetter()
}

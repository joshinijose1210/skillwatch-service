package review

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAllSelfReviewParams(
  val reviewDetailsId: Long?
)

class GetAllSelfReviewParamSetter : ParamSetter<GetAllSelfReviewParams> {
  override fun map(ps: PreparedStatement, params: GetAllSelfReviewParams) {
    ps.setObject(1, params.reviewDetailsId)
  }
}

data class GetAllSelfReviewResult(
  val reviewId: Long,
  val kpiId: Long?,
  val title: String,
  val description: String,
  val review: String?,
  val rating: Int?,
  val kraId: Long?,
  val kraName: String?
)

class GetAllSelfReviewRowMapper : RowMapper<GetAllSelfReviewResult> {
  override fun map(rs: ResultSet): GetAllSelfReviewResult = GetAllSelfReviewResult(
  reviewId = rs.getObject("review_id") as kotlin.Long,
    kpiId = rs.getObject("kpi_id") as kotlin.Long?,
    title = rs.getObject("title") as kotlin.String,
    description = rs.getObject("description") as kotlin.String,
    review = rs.getObject("review") as kotlin.String?,
    rating = rs.getObject("rating") as kotlin.Int?,
    kraId = rs.getObject("kra_id") as kotlin.Long?,
    kraName = rs.getObject("kra_name") as kotlin.String?)
}

class GetAllSelfReviewQuery : Query<GetAllSelfReviewParams, GetAllSelfReviewResult> {
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
      |  reviews.review_details_id = ?;
      """.trimMargin()

  override val mapper: RowMapper<GetAllSelfReviewResult> = GetAllSelfReviewRowMapper()

  override val paramSetter: ParamSetter<GetAllSelfReviewParams> = GetAllSelfReviewParamSetter()
}

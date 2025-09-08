package review

import java.math.BigDecimal
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAverageRatingParams(
  val reviewDetailsId: Long?,
  val reviewCycleId: Long?
)

class GetAverageRatingParamSetter : ParamSetter<GetAverageRatingParams> {
  override fun map(ps: PreparedStatement, params: GetAverageRatingParams) {
    ps.setObject(1, params.reviewDetailsId)
    ps.setObject(2, params.reviewCycleId)
  }
}

data class GetAverageRatingResult(
  val averageRating: BigDecimal?
)

class GetAverageRatingRowMapper : RowMapper<GetAverageRatingResult> {
  override fun map(rs: ResultSet): GetAverageRatingResult = GetAverageRatingResult(
  averageRating = rs.getObject("average_rating") as java.math.BigDecimal?)
}

class GetAverageRatingQuery : Query<GetAverageRatingParams, GetAverageRatingResult> {
  override val sql: String = """
      |SELECT
      |    review_details.average_rating
      |FROM
      |    review_details
      |WHERE
      |    review_details.id = ?
      |    AND review_details.review_cycle_id = ? ;
      |""".trimMargin()

  override val mapper: RowMapper<GetAverageRatingResult> = GetAverageRatingRowMapper()

  override val paramSetter: ParamSetter<GetAverageRatingParams> = GetAverageRatingParamSetter()
}

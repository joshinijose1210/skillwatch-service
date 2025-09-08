package kra

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetKraByReviewCycleParams(
  val reviewCycleId: Long?
)

class GetKraByReviewCycleParamSetter : ParamSetter<GetKraByReviewCycleParams> {
  override fun map(ps: PreparedStatement, params: GetKraByReviewCycleParams) {
    ps.setObject(1, params.reviewCycleId)
  }
}

data class GetKraByReviewCycleResult(
  val id: Long,
  val srNo: Long,
  val kraName: String,
  val kraWeightage: Int,
  val organisationId: Long
)

class GetKraByReviewCycleRowMapper : RowMapper<GetKraByReviewCycleResult> {
  override fun map(rs: ResultSet): GetKraByReviewCycleResult = GetKraByReviewCycleResult(
  id = rs.getObject("id") as kotlin.Long,
    srNo = rs.getObject("sr_no") as kotlin.Long,
    kraName = rs.getObject("kra_name") as kotlin.String,
    kraWeightage = rs.getObject("kra_weightage") as kotlin.Int,
    organisationId = rs.getObject("organisation_id") as kotlin.Long)
}

class GetKraByReviewCycleQuery : Query<GetKraByReviewCycleParams, GetKraByReviewCycleResult> {
  override val sql: String = """
      |SELECT
      |    kra.id,
      |    kra.sr_no,
      |    rck.kra_name AS kra_name,
      |    rck.kra_weightage AS kra_weightage,
      |    kra.organisation_id
      |FROM kra
      |JOIN review_cycle_kra AS rck
      |    ON rck.kra_id = kra.id AND rck.review_cycle_id = ? ;
      |""".trimMargin()

  override val mapper: RowMapper<GetKraByReviewCycleResult> = GetKraByReviewCycleRowMapper()

  override val paramSetter: ParamSetter<GetKraByReviewCycleParams> =
      GetKraByReviewCycleParamSetter()
}

package kra

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetMaxSrNoParams(
  val organisationId: Long?
)

class GetMaxSrNoParamSetter : ParamSetter<GetMaxSrNoParams> {
  override fun map(ps: PreparedStatement, params: GetMaxSrNoParams) {
    ps.setObject(1, params.organisationId)
  }
}

data class GetMaxSrNoResult(
  val maxSrNo: Long?
)

class GetMaxSrNoRowMapper : RowMapper<GetMaxSrNoResult> {
  override fun map(rs: ResultSet): GetMaxSrNoResult = GetMaxSrNoResult(
  maxSrNo = rs.getObject("max_sr_no") as kotlin.Long?)
}

class GetMaxSrNoQuery : Query<GetMaxSrNoParams, GetMaxSrNoResult> {
  override val sql: String = """
      |SELECT MAX(sr_no) as max_sr_no FROM kra WHERE organisation_id = ?;
      |""".trimMargin()

  override val mapper: RowMapper<GetMaxSrNoResult> = GetMaxSrNoRowMapper()

  override val paramSetter: ParamSetter<GetMaxSrNoParams> = GetMaxSrNoParamSetter()
}

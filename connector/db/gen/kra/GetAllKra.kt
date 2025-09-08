package kra

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAllKraParams(
  val organisationId: Long?
)

class GetAllKraParamSetter : ParamSetter<GetAllKraParams> {
  override fun map(ps: PreparedStatement, params: GetAllKraParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.organisationId)
  }
}

data class GetAllKraResult(
  val id: Long,
  val srNo: Long,
  val name: String,
  val weightage: Int,
  val versionNumber: Int,
  val organisationId: Long
)

class GetAllKraRowMapper : RowMapper<GetAllKraResult> {
  override fun map(rs: ResultSet): GetAllKraResult = GetAllKraResult(
  id = rs.getObject("id") as kotlin.Long,
    srNo = rs.getObject("sr_no") as kotlin.Long,
    name = rs.getObject("name") as kotlin.String,
    weightage = rs.getObject("weightage") as kotlin.Int,
    versionNumber = rs.getObject("version_number") as kotlin.Int,
    organisationId = rs.getObject("organisation_id") as kotlin.Long)
}

class GetAllKraQuery : Query<GetAllKraParams, GetAllKraResult> {
  override val sql: String = """
      |SELECT k1.id, k1.sr_no, k1.name, k1.weightage, k1.version_number, k1.organisation_id
      |FROM kra k1
      |JOIN (
      |    -- Subquery to get the latest version number for each KRA within the organisation
      |    SELECT sr_no, MAX(version_number) AS latest_version
      |    FROM kra
      |    WHERE organisation_id = ?
      |    GROUP BY sr_no
      |) k2
      |ON k1.sr_no = k2.sr_no
      |AND k1.version_number = k2.latest_version
      |WHERE k1.organisation_id = ?
      |ORDER BY k1.sr_no;
      |""".trimMargin()

  override val mapper: RowMapper<GetAllKraResult> = GetAllKraRowMapper()

  override val paramSetter: ParamSetter<GetAllKraParams> = GetAllKraParamSetter()
}

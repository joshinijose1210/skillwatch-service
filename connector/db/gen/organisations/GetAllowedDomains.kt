package organisations

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAllowedDomainsParams(
  val organisationId: Long?
)

class GetAllowedDomainsParamSetter : ParamSetter<GetAllowedDomainsParams> {
  override fun map(ps: PreparedStatement, params: GetAllowedDomainsParams) {
    ps.setObject(1, params.organisationId)
  }
}

data class GetAllowedDomainsResult(
  val srNo: Long,
  val allowedDomain: String
)

class GetAllowedDomainsRowMapper : RowMapper<GetAllowedDomainsResult> {
  override fun map(rs: ResultSet): GetAllowedDomainsResult = GetAllowedDomainsResult(
  srNo = rs.getObject("sr_no") as kotlin.Long,
    allowedDomain = rs.getObject("allowed_domain") as kotlin.String)
}

class GetAllowedDomainsQuery : Query<GetAllowedDomainsParams, GetAllowedDomainsResult> {
  override val sql: String = """
      |SELECT
      |  odm.sr_no, odm.allowed_domain
      |FROM
      |  organisation_domain_mapping odm
      |WHERE
      |  odm.organisation_id = ?
      |ORDER BY odm.sr_no ;
      |""".trimMargin()

  override val mapper: RowMapper<GetAllowedDomainsResult> = GetAllowedDomainsRowMapper()

  override val paramSetter: ParamSetter<GetAllowedDomainsParams> = GetAllowedDomainsParamSetter()
}

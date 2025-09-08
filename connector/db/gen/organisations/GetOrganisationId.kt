package organisations

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Int
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetOrganisationIdParams(
  val organisationName: String?
)

class GetOrganisationIdParamSetter : ParamSetter<GetOrganisationIdParams> {
  override fun map(ps: PreparedStatement, params: GetOrganisationIdParams) {
    ps.setObject(1, params.organisationName)
  }
}

data class GetOrganisationIdResult(
  val organisationId: Int
)

class GetOrganisationIdRowMapper : RowMapper<GetOrganisationIdResult> {
  override fun map(rs: ResultSet): GetOrganisationIdResult = GetOrganisationIdResult(
  organisationId = rs.getObject("organisation_id") as kotlin.Int)
}

class GetOrganisationIdQuery : Query<GetOrganisationIdParams, GetOrganisationIdResult> {
  override val sql: String = """
      |SELECT sr_no AS organisation_id
      |FROM organisations
      |WHERE name = ? ;
      |""".trimMargin()

  override val mapper: RowMapper<GetOrganisationIdResult> = GetOrganisationIdRowMapper()

  override val paramSetter: ParamSetter<GetOrganisationIdParams> = GetOrganisationIdParamSetter()
}

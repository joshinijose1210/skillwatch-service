package organisations

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetOrganisationIdByEmailIdParams(
  val emailId: String?
)

class GetOrganisationIdByEmailIdParamSetter : ParamSetter<GetOrganisationIdByEmailIdParams> {
  override fun map(ps: PreparedStatement, params: GetOrganisationIdByEmailIdParams) {
    ps.setObject(1, params.emailId)
  }
}

data class GetOrganisationIdByEmailIdResult(
  val organisationId: Long
)

class GetOrganisationIdByEmailIdRowMapper : RowMapper<GetOrganisationIdByEmailIdResult> {
  override fun map(rs: ResultSet): GetOrganisationIdByEmailIdResult =
      GetOrganisationIdByEmailIdResult(
  organisationId = rs.getObject("organisation_id") as kotlin.Long)
}

class GetOrganisationIdByEmailIdQuery : Query<GetOrganisationIdByEmailIdParams,
    GetOrganisationIdByEmailIdResult> {
  override val sql: String = """
      |SELECT
      |  organisation_id
      |FROM
      |  employees
      |WHERE
      |  email_id = ? ;
      """.trimMargin()

  override val mapper: RowMapper<GetOrganisationIdByEmailIdResult> =
      GetOrganisationIdByEmailIdRowMapper()

  override val paramSetter: ParamSetter<GetOrganisationIdByEmailIdParams> =
      GetOrganisationIdByEmailIdParamSetter()
}

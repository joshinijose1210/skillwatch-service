package login

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Int
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

class GetMaxOrganisationIdParams

class GetMaxOrganisationIdParamSetter : ParamSetter<GetMaxOrganisationIdParams> {
  override fun map(ps: PreparedStatement, params: GetMaxOrganisationIdParams) {
  }
}

data class GetMaxOrganisationIdResult(
  val maxId: Int?
)

class GetMaxOrganisationIdRowMapper : RowMapper<GetMaxOrganisationIdResult> {
  override fun map(rs: ResultSet): GetMaxOrganisationIdResult = GetMaxOrganisationIdResult(
  maxId = rs.getObject("max_id") as kotlin.Int?)
}

class GetMaxOrganisationIdQuery : Query<GetMaxOrganisationIdParams, GetMaxOrganisationIdResult> {
  override val sql: String = "SELECT MAX(sr_no) as max_id FROM organisations;"

  override val mapper: RowMapper<GetMaxOrganisationIdResult> = GetMaxOrganisationIdRowMapper()

  override val paramSetter: ParamSetter<GetMaxOrganisationIdParams> =
      GetMaxOrganisationIdParamSetter()
}

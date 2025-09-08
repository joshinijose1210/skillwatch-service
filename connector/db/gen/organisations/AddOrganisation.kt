package organisations

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class AddOrganisationParams(
  val organisationName: String?,
  val isActive: Boolean?,
  val organisationSize: Int?,
  val timeZone: String?
)

class AddOrganisationParamSetter : ParamSetter<AddOrganisationParams> {
  override fun map(ps: PreparedStatement, params: AddOrganisationParams) {
    ps.setObject(1, params.organisationName)
    ps.setObject(2, params.isActive)
    ps.setObject(3, params.organisationSize)
    ps.setObject(4, params.timeZone)
  }
}

data class AddOrganisationResult(
  val srNo: Int
)

class AddOrganisationRowMapper : RowMapper<AddOrganisationResult> {
  override fun map(rs: ResultSet): AddOrganisationResult = AddOrganisationResult(
  srNo = rs.getObject("sr_no") as kotlin.Int)
}

class AddOrganisationQuery : Query<AddOrganisationParams, AddOrganisationResult> {
  override val sql: String = """
      |INSERT INTO organisations(
      |  name, is_active, organisation_size, time_zone
      |)
      |VALUES(
      |  ?, ?, ?, ?
      |) RETURNING sr_no;
      """.trimMargin()

  override val mapper: RowMapper<AddOrganisationResult> = AddOrganisationRowMapper()

  override val paramSetter: ParamSetter<AddOrganisationParams> = AddOrganisationParamSetter()
}

package organisations

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class AddOrganisationAdminParams(
  val empId: String?,
  val firstName: String?,
  val lastName: String?,
  val emailId: String?,
  val contactNo: String?,
  val organisationId: Long?
)

class AddOrganisationAdminParamSetter : ParamSetter<AddOrganisationAdminParams> {
  override fun map(ps: PreparedStatement, params: AddOrganisationAdminParams) {
    ps.setObject(1, params.empId)
    ps.setObject(2, params.firstName)
    ps.setObject(3, params.lastName)
    ps.setObject(4, params.emailId)
    ps.setObject(5, params.contactNo)
    ps.setObject(6, params.organisationId)
  }
}

data class AddOrganisationAdminResult(
  val id: Long
)

class AddOrganisationAdminRowMapper : RowMapper<AddOrganisationAdminResult> {
  override fun map(rs: ResultSet): AddOrganisationAdminResult = AddOrganisationAdminResult(
  id = rs.getObject("id") as kotlin.Long)
}

class AddOrganisationAdminQuery : Query<AddOrganisationAdminParams, AddOrganisationAdminResult> {
  override val sql: String = """
      |INSERT INTO employees (emp_id, first_name, last_name, email_id, contact_no, status, organisation_id)
      |VALUES (?, ?, ?, ?, ?, true, ?) RETURNING id ;
      |
      |""".trimMargin()

  override val mapper: RowMapper<AddOrganisationAdminResult> = AddOrganisationAdminRowMapper()

  override val paramSetter: ParamSetter<AddOrganisationAdminParams> =
      AddOrganisationAdminParamSetter()
}

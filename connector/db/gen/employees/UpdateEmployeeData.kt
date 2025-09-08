package employees

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class UpdateEmployeeDataParams(
  val employeeId: String?,
  val firstName: String?,
  val lastName: String?,
  val emailId: String?,
  val contactNo: String?,
  val status: Boolean?,
  val genderId: Int?,
  val dateOfBirth: Date?,
  val dateOfJoining: Date?,
  val experience: Int?,
  val isConsultant: Boolean?,
  val id: Long?,
  val organisationId: Long?
)

class UpdateEmployeeDataParamSetter : ParamSetter<UpdateEmployeeDataParams> {
  override fun map(ps: PreparedStatement, params: UpdateEmployeeDataParams) {
    ps.setObject(1, params.employeeId)
    ps.setObject(2, params.firstName)
    ps.setObject(3, params.lastName)
    ps.setObject(4, params.emailId)
    ps.setObject(5, params.contactNo)
    ps.setObject(6, params.status)
    ps.setObject(7, params.genderId)
    ps.setObject(8, params.dateOfBirth)
    ps.setObject(9, params.dateOfJoining)
    ps.setObject(10, params.experience)
    ps.setObject(11, params.isConsultant)
    ps.setObject(12, params.id)
    ps.setObject(13, params.organisationId)
  }
}

data class UpdateEmployeeDataResult(
  val id: Long,
  val empId: String,
  val firstName: String,
  val lastName: String,
  val emailId: String,
  val contactNo: String,
  val status: Boolean,
  val password: String?,
  val onboardingFlow: Boolean,
  val organisationId: Long,
  val createdAt: Timestamp,
  val updatedAt: Timestamp?,
  val genderId: Int?,
  val dateOfBirth: Date?,
  val dateOfJoining: Date?,
  val experience: Int?,
  val isConsultant: Boolean
)

class UpdateEmployeeDataRowMapper : RowMapper<UpdateEmployeeDataResult> {
  override fun map(rs: ResultSet): UpdateEmployeeDataResult = UpdateEmployeeDataResult(
  id = rs.getObject("id") as kotlin.Long,
    empId = rs.getObject("emp_id") as kotlin.String,
    firstName = rs.getObject("first_name") as kotlin.String,
    lastName = rs.getObject("last_name") as kotlin.String,
    emailId = rs.getObject("email_id") as kotlin.String,
    contactNo = rs.getObject("contact_no") as kotlin.String,
    status = rs.getObject("status") as kotlin.Boolean,
    password = rs.getObject("password") as kotlin.String?,
    onboardingFlow = rs.getObject("onboarding_flow") as kotlin.Boolean,
    organisationId = rs.getObject("organisation_id") as kotlin.Long,
    createdAt = rs.getObject("created_at") as java.sql.Timestamp,
    updatedAt = rs.getObject("updated_at") as java.sql.Timestamp?,
    genderId = rs.getObject("gender_id") as kotlin.Int?,
    dateOfBirth = rs.getObject("date_of_birth") as java.sql.Date?,
    dateOfJoining = rs.getObject("date_of_joining") as java.sql.Date?,
    experience = rs.getObject("experience") as kotlin.Int?,
    isConsultant = rs.getObject("is_consultant") as kotlin.Boolean)
}

class UpdateEmployeeDataQuery : Query<UpdateEmployeeDataParams, UpdateEmployeeDataResult> {
  override val sql: String = """
      |UPDATE
      |  employees
      |SET
      |  emp_id = ?,
      |  first_name = ?,
      |  last_name = ?,
      |  email_id = ?,
      |  contact_no = ?,
      |  status = ?,
      |  gender_id = ?,
      |  date_of_birth= ?,
      |  date_of_joining = ?,
      |  experience = ?,
      |  is_consultant = ?,
      |  updated_at = CURRENT_TIMESTAMP
      |WHERE
      | id = ?
      | AND organisation_id = ?
      |RETURNING * ;
      |
      |""".trimMargin()

  override val mapper: RowMapper<UpdateEmployeeDataResult> = UpdateEmployeeDataRowMapper()

  override val paramSetter: ParamSetter<UpdateEmployeeDataParams> = UpdateEmployeeDataParamSetter()
}

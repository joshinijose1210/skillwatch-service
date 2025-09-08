package user

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetUserRoleParams(
  val emailId: String?
)

class GetUserRoleParamSetter : ParamSetter<GetUserRoleParams> {
  override fun map(ps: PreparedStatement, params: GetUserRoleParams) {
    ps.setObject(1, params.emailId)
  }
}

data class GetUserRoleResult(
  val roleId: Long,
  val roleName: String
)

class GetUserRoleRowMapper : RowMapper<GetUserRoleResult> {
  override fun map(rs: ResultSet): GetUserRoleResult = GetUserRoleResult(
  roleId = rs.getObject("role_id") as kotlin.Long,
    roleName = rs.getObject("role_name") as kotlin.String)
}

class GetUserRoleQuery : Query<GetUserRoleParams, GetUserRoleResult> {
  override val sql: String = """
      |select employees_role_mapping.role_id,
      | roles.role_name
      | from employees_role_mapping
      |JOIN employees on employees.id = employees_role_mapping.emp_id
      |JOIN roles ON employees_role_mapping.role_id = roles.id
      |where LOWER(employees.email_id) = LOWER(?) ;
      |""".trimMargin()

  override val mapper: RowMapper<GetUserRoleResult> = GetUserRoleRowMapper()

  override val paramSetter: ParamSetter<GetUserRoleParams> = GetUserRoleParamSetter()
}

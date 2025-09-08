package roles

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class HasPermissionsParams(
  val organisationId: Long?,
  val moduleName: String?,
  val roleName: String?
)

class HasPermissionsParamSetter : ParamSetter<HasPermissionsParams> {
  override fun map(ps: PreparedStatement, params: HasPermissionsParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.moduleName)
    ps.setObject(3, params.roleName)
  }
}

data class HasPermissionsResult(
  val view: Boolean?,
  val edit: Boolean?
)

class HasPermissionsRowMapper : RowMapper<HasPermissionsResult> {
  override fun map(rs: ResultSet): HasPermissionsResult = HasPermissionsResult(
  view = rs.getObject("view") as kotlin.Boolean?,
    edit = rs.getObject("edit") as kotlin.Boolean?)
}

class HasPermissionsQuery : Query<HasPermissionsParams, HasPermissionsResult> {
  override val sql: String = """
      |SELECT
      | view,
      | edit
      |FROM
      | module_permissions
      |JOIN modules ON modules.id = module_permissions.module_id
      |JOIN roles ON roles.id = module_permissions.role_id AND roles.organisation_id = ?
      |WHERE
      |UPPER(modules.name) = UPPER(?)
      |AND UPPER(roles.role_name) = UPPER(?) ;
      |""".trimMargin()

  override val mapper: RowMapper<HasPermissionsResult> = HasPermissionsRowMapper()

  override val paramSetter: ParamSetter<HasPermissionsParams> = HasPermissionsParamSetter()
}

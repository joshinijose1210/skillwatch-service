package roles

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetPermissionParams(
  val roleId: Long?
)

class GetPermissionParamSetter : ParamSetter<GetPermissionParams> {
  override fun map(ps: PreparedStatement, params: GetPermissionParams) {
    ps.setObject(1, params.roleId)
  }
}

data class GetPermissionResult(
  val moduleId: Int?,
  val moduleName: String,
  val view: Boolean?,
  val edit: Boolean?
)

class GetPermissionRowMapper : RowMapper<GetPermissionResult> {
  override fun map(rs: ResultSet): GetPermissionResult = GetPermissionResult(
  moduleId = rs.getObject("module_id") as kotlin.Int?,
    moduleName = rs.getObject("module_name") as kotlin.String,
    view = rs.getObject("view") as kotlin.Boolean?,
    edit = rs.getObject("edit") as kotlin.Boolean?)
}

class GetPermissionQuery : Query<GetPermissionParams, GetPermissionResult> {
  override val sql: String = """
      |SELECT
      |  module_id,
      |  modules.name AS module_name,
      |  view,
      |  edit
      |from
      |  module_permissions
      |JOIN modules on modules.id = module_permissions.module_id
      |where
      |  role_id = ?;
      |""".trimMargin()

  override val mapper: RowMapper<GetPermissionResult> = GetPermissionRowMapper()

  override val paramSetter: ParamSetter<GetPermissionParams> = GetPermissionParamSetter()
}

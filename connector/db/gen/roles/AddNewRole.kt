package roles

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class AddNewRoleParams(
  val organisationId: Long?,
  val roleId: Long?,
  val roleName: String?,
  val status: Boolean?
)

class AddNewRoleParamSetter : ParamSetter<AddNewRoleParams> {
  override fun map(ps: PreparedStatement, params: AddNewRoleParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.roleId)
    ps.setObject(3, params.roleName)
    ps.setObject(4, params.status)
  }
}

data class AddNewRoleResult(
  val id: Long
)

class AddNewRoleRowMapper : RowMapper<AddNewRoleResult> {
  override fun map(rs: ResultSet): AddNewRoleResult = AddNewRoleResult(
  id = rs.getObject("id") as kotlin.Long)
}

class AddNewRoleQuery : Query<AddNewRoleParams, AddNewRoleResult> {
  override val sql: String = """
      |INSERT INTO roles(organisation_id, role_id, role_name, status)
      |VALUES
      |(?, ?, ?,?) RETURNING id ;
      """.trimMargin()

  override val mapper: RowMapper<AddNewRoleResult> = AddNewRoleRowMapper()

  override val paramSetter: ParamSetter<AddNewRoleParams> = AddNewRoleParamSetter()
}

package departments

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class AddDepartmentParams(
  val departmentName: String?,
  val departmentStatus: Boolean?,
  val id: Long?,
  val organisationId: Long?
)

class AddDepartmentParamSetter : ParamSetter<AddDepartmentParams> {
  override fun map(ps: PreparedStatement, params: AddDepartmentParams) {
    ps.setObject(1, params.departmentName)
    ps.setObject(2, params.departmentStatus)
    ps.setObject(3, params.id)
    ps.setObject(4, params.organisationId)
  }
}

data class AddDepartmentResult(
  val id: Long
)

class AddDepartmentRowMapper : RowMapper<AddDepartmentResult> {
  override fun map(rs: ResultSet): AddDepartmentResult = AddDepartmentResult(
  id = rs.getObject("id") as kotlin.Long)
}

class AddDepartmentQuery : Query<AddDepartmentParams, AddDepartmentResult> {
  override val sql: String = """
      |INSERT INTO departments(department_name, status, department_id, organisation_id)
      |VALUES
      |  (?, ?, ?, ?)
      |RETURNING id;
      """.trimMargin()

  override val mapper: RowMapper<AddDepartmentResult> = AddDepartmentRowMapper()

  override val paramSetter: ParamSetter<AddDepartmentParams> = AddDepartmentParamSetter()
}

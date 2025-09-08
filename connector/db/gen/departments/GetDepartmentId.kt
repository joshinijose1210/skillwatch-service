package departments

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetDepartmentIdParams(
  val organisationId: Long?,
  val departmentName: String?
)

class GetDepartmentIdParamSetter : ParamSetter<GetDepartmentIdParams> {
  override fun map(ps: PreparedStatement, params: GetDepartmentIdParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.departmentName)
  }
}

data class GetDepartmentIdResult(
  val id: Long
)

class GetDepartmentIdRowMapper : RowMapper<GetDepartmentIdResult> {
  override fun map(rs: ResultSet): GetDepartmentIdResult = GetDepartmentIdResult(
  id = rs.getObject("id") as kotlin.Long)
}

class GetDepartmentIdQuery : Query<GetDepartmentIdParams, GetDepartmentIdResult> {
  override val sql: String = """
      |SELECT id FROM departments WHERE
      |      organisation_id = ?
      |      AND LOWER(department_name) = LOWER(?)
      |      AND departments.status = true;
      """.trimMargin()

  override val mapper: RowMapper<GetDepartmentIdResult> = GetDepartmentIdRowMapper()

  override val paramSetter: ParamSetter<GetDepartmentIdParams> = GetDepartmentIdParamSetter()
}

package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetEmployeesUniqueIdParams(
  val employeeId: String?,
  val organisationId: Long?
)

class GetEmployeesUniqueIdParamSetter : ParamSetter<GetEmployeesUniqueIdParams> {
  override fun map(ps: PreparedStatement, params: GetEmployeesUniqueIdParams) {
    ps.setObject(1, params.employeeId)
    ps.setObject(2, params.organisationId)
  }
}

data class GetEmployeesUniqueIdResult(
  val id: Long
)

class GetEmployeesUniqueIdRowMapper : RowMapper<GetEmployeesUniqueIdResult> {
  override fun map(rs: ResultSet): GetEmployeesUniqueIdResult = GetEmployeesUniqueIdResult(
  id = rs.getObject("id") as kotlin.Long)
}

class GetEmployeesUniqueIdQuery : Query<GetEmployeesUniqueIdParams, GetEmployeesUniqueIdResult> {
  override val sql: String = """
      |SELECT id FROM employees WHERE
      |      LOWER(emp_id) = LOWER(?)
      |      AND organisation_id = ?;
      """.trimMargin()

  override val mapper: RowMapper<GetEmployeesUniqueIdResult> = GetEmployeesUniqueIdRowMapper()

  override val paramSetter: ParamSetter<GetEmployeesUniqueIdParams> =
      GetEmployeesUniqueIdParamSetter()
}

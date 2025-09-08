package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class IsEmployeeIdExistsParams(
  val employeeId: String?,
  val organisationId: Long?
)

class IsEmployeeIdExistsParamSetter : ParamSetter<IsEmployeeIdExistsParams> {
  override fun map(ps: PreparedStatement, params: IsEmployeeIdExistsParams) {
    ps.setObject(1, params.employeeId)
    ps.setObject(2, params.organisationId)
  }
}

data class IsEmployeeIdExistsResult(
  val exists: Boolean?,
  val status: Boolean?
)

class IsEmployeeIdExistsRowMapper : RowMapper<IsEmployeeIdExistsResult> {
  override fun map(rs: ResultSet): IsEmployeeIdExistsResult = IsEmployeeIdExistsResult(
  exists = rs.getObject("exists") as kotlin.Boolean?,
    status = rs.getObject("status") as kotlin.Boolean?)
}

class IsEmployeeIdExistsQuery : Query<IsEmployeeIdExistsParams, IsEmployeeIdExistsResult> {
  override val sql: String = """
      |WITH subquery AS (
      |    SELECT status FROM employees
      |    WHERE
      |    LOWER(emp_id) = LOWER(?)
      |    AND organisation_id = ?
      |)
      |SELECT EXISTS (SELECT 1 FROM subquery), (SELECT status FROM subquery);
      """.trimMargin()

  override val mapper: RowMapper<IsEmployeeIdExistsResult> = IsEmployeeIdExistsRowMapper()

  override val paramSetter: ParamSetter<IsEmployeeIdExistsParams> = IsEmployeeIdExistsParamSetter()
}

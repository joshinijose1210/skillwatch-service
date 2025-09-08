package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class IsEmployeeExistsParams(
  val emailId: String?
)

class IsEmployeeExistsParamSetter : ParamSetter<IsEmployeeExistsParams> {
  override fun map(ps: PreparedStatement, params: IsEmployeeExistsParams) {
    ps.setObject(1, params.emailId)
  }
}

data class IsEmployeeExistsResult(
  val exists: Boolean
)

class IsEmployeeExistsRowMapper : RowMapper<IsEmployeeExistsResult> {
  override fun map(rs: ResultSet): IsEmployeeExistsResult = IsEmployeeExistsResult(
  exists = rs.getObject("exists") as kotlin.Boolean)
}

class IsEmployeeExistsQuery : Query<IsEmployeeExistsParams, IsEmployeeExistsResult> {
  override val sql: String = """
      |SELECT EXISTS (
      |  SELECT 1 FROM employees WHERE
      |      LOWER(email_id) = LOWER(?)
      |      AND status = true
      |);
      |""".trimMargin()

  override val mapper: RowMapper<IsEmployeeExistsResult> = IsEmployeeExistsRowMapper()

  override val paramSetter: ParamSetter<IsEmployeeExistsParams> = IsEmployeeExistsParamSetter()
}

package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetSecretParams(
  val emailId: String?
)

class GetSecretParamSetter : ParamSetter<GetSecretParams> {
  override fun map(ps: PreparedStatement, params: GetSecretParams) {
    ps.setObject(1, params.emailId)
  }
}

data class GetSecretResult(
  val password: String?
)

class GetSecretRowMapper : RowMapper<GetSecretResult> {
  override fun map(rs: ResultSet): GetSecretResult = GetSecretResult(
  password = rs.getObject("password") as kotlin.String?)
}

class GetSecretQuery : Query<GetSecretParams, GetSecretResult> {
  override val sql: String = """
      |SELECT
      |  password
      |FROM
      |  employees
      |WHERE
      |  LOWER(email_id) = LOWER(?);
      """.trimMargin()

  override val mapper: RowMapper<GetSecretResult> = GetSecretRowMapper()

  override val paramSetter: ParamSetter<GetSecretParams> = GetSecretParamSetter()
}

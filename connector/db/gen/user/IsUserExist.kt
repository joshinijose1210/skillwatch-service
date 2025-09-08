package user

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class IsUserExistParams(
  val emailId: String?
)

class IsUserExistParamSetter : ParamSetter<IsUserExistParams> {
  override fun map(ps: PreparedStatement, params: IsUserExistParams) {
    ps.setObject(1, params.emailId)
  }
}

data class IsUserExistResult(
  val exists: Boolean?
)

class IsUserExistRowMapper : RowMapper<IsUserExistResult> {
  override fun map(rs: ResultSet): IsUserExistResult = IsUserExistResult(
  exists = rs.getObject("exists") as kotlin.Boolean?)
}

class IsUserExistQuery : Query<IsUserExistParams, IsUserExistResult> {
  override val sql: String = """
      |SELECT EXISTS (
      |  SELECT 1 FROM users WHERE
      |      LOWER(email_id) = LOWER(?)
      |      AND is_org_admin = TRUE
      |) ;
      """.trimMargin()

  override val mapper: RowMapper<IsUserExistResult> = IsUserExistRowMapper()

  override val paramSetter: ParamSetter<IsUserExistParams> = IsUserExistParamSetter()
}

package user

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetUserParams(
  val emailId: String?
)

class GetUserParamSetter : ParamSetter<GetUserParams> {
  override fun map(ps: PreparedStatement, params: GetUserParams) {
    ps.setObject(1, params.emailId)
  }
}

data class GetUserResult(
  val id: Long,
  val firstName: String,
  val lastName: String,
  val emailId: String
)

class GetUserRowMapper : RowMapper<GetUserResult> {
  override fun map(rs: ResultSet): GetUserResult = GetUserResult(
  id = rs.getObject("id") as kotlin.Long,
    firstName = rs.getObject("first_name") as kotlin.String,
    lastName = rs.getObject("last_name") as kotlin.String,
    emailId = rs.getObject("email_id") as kotlin.String)
}

class GetUserQuery : Query<GetUserParams, GetUserResult> {
  override val sql: String = """
      |SELECT id , first_name, last_name, email_id
      |FROM users
      |WHERE LOWER(email_id) = LOWER(?);
      """.trimMargin()

  override val mapper: RowMapper<GetUserResult> = GetUserRowMapper()

  override val paramSetter: ParamSetter<GetUserParams> = GetUserParamSetter()
}

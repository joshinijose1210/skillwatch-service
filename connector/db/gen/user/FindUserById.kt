package user

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class FindUserByIdParams(
  val userid: Long?
)

class FindUserByIdParamSetter : ParamSetter<FindUserByIdParams> {
  override fun map(ps: PreparedStatement, params: FindUserByIdParams) {
    ps.setObject(1, params.userid)
  }
}

data class FindUserByIdResult(
  val userid: Long,
  val firstName: String,
  val lastName: String,
  val emailId: String
)

class FindUserByIdRowMapper : RowMapper<FindUserByIdResult> {
  override fun map(rs: ResultSet): FindUserByIdResult = FindUserByIdResult(
  userid = rs.getObject("userid") as kotlin.Long,
    firstName = rs.getObject("first_name") as kotlin.String,
    lastName = rs.getObject("last_name") as kotlin.String,
    emailId = rs.getObject("email_id") as kotlin.String)
}

class FindUserByIdQuery : Query<FindUserByIdParams, FindUserByIdResult> {
  override val sql: String = """
      |SELECT id AS userid, first_name, last_name, email_id
      |FROM users
      |WHERE id = ?
      """.trimMargin()

  override val mapper: RowMapper<FindUserByIdResult> = FindUserByIdRowMapper()

  override val paramSetter: ParamSetter<FindUserByIdParams> = FindUserByIdParamSetter()
}

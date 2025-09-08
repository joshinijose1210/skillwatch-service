package user

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class FindUserByNameParams(
  val username: String?
)

class FindUserByNameParamSetter : ParamSetter<FindUserByNameParams> {
  override fun map(ps: PreparedStatement, params: FindUserByNameParams) {
    ps.setObject(1, params.username)
  }
}

data class FindUserByNameResult(
  val userid: Long,
  val firstName: String,
  val lastName: String,
  val username: String
)

class FindUserByNameRowMapper : RowMapper<FindUserByNameResult> {
  override fun map(rs: ResultSet): FindUserByNameResult = FindUserByNameResult(
  userid = rs.getObject("userid") as kotlin.Long,
    firstName = rs.getObject("first_name") as kotlin.String,
    lastName = rs.getObject("last_name") as kotlin.String,
    username = rs.getObject("username") as kotlin.String)
}

class FindUserByNameQuery : Query<FindUserByNameParams, FindUserByNameResult> {
  override val sql: String = """
      |SELECT id AS userid, first_name, last_name, email_id AS username
      |FROM employees
      |WHERE LOWER(email_id) = LOWER(?) ;
      """.trimMargin()

  override val mapper: RowMapper<FindUserByNameResult> = FindUserByNameRowMapper()

  override val paramSetter: ParamSetter<FindUserByNameParams> = FindUserByNameParamSetter()
}

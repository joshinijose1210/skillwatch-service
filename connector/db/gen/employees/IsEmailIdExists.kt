package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class IsEmailIdExistsParams(
  val emailId: String?
)

class IsEmailIdExistsParamSetter : ParamSetter<IsEmailIdExistsParams> {
  override fun map(ps: PreparedStatement, params: IsEmailIdExistsParams) {
    ps.setObject(1, params.emailId)
  }
}

data class IsEmailIdExistsResult(
  val exists: Boolean?
)

class IsEmailIdExistsRowMapper : RowMapper<IsEmailIdExistsResult> {
  override fun map(rs: ResultSet): IsEmailIdExistsResult = IsEmailIdExistsResult(
  exists = rs.getObject("exists") as kotlin.Boolean?)
}

class IsEmailIdExistsQuery : Query<IsEmailIdExistsParams, IsEmailIdExistsResult> {
  override val sql: String = """
      |SELECT EXISTS (
      |  SELECT 1 FROM employees WHERE
      |      LOWER(email_id) = LOWER(?)
      |) ;
      """.trimMargin()

  override val mapper: RowMapper<IsEmailIdExistsResult> = IsEmailIdExistsRowMapper()

  override val paramSetter: ParamSetter<IsEmailIdExistsParams> = IsEmailIdExistsParamSetter()
}

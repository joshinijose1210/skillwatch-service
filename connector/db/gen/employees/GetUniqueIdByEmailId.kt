package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetUniqueIdByEmailIdParams(
  val emailId: String?
)

class GetUniqueIdByEmailIdParamSetter : ParamSetter<GetUniqueIdByEmailIdParams> {
  override fun map(ps: PreparedStatement, params: GetUniqueIdByEmailIdParams) {
    ps.setObject(1, params.emailId)
  }
}

data class GetUniqueIdByEmailIdResult(
  val id: Long
)

class GetUniqueIdByEmailIdRowMapper : RowMapper<GetUniqueIdByEmailIdResult> {
  override fun map(rs: ResultSet): GetUniqueIdByEmailIdResult = GetUniqueIdByEmailIdResult(
  id = rs.getObject("id") as kotlin.Long)
}

class GetUniqueIdByEmailIdQuery : Query<GetUniqueIdByEmailIdParams, GetUniqueIdByEmailIdResult> {
  override val sql: String = """
      |SELECT
      |  id
      |FROM
      |  employees
      |WHERE
      |  email_id = ?;
      """.trimMargin()

  override val mapper: RowMapper<GetUniqueIdByEmailIdResult> = GetUniqueIdByEmailIdRowMapper()

  override val paramSetter: ParamSetter<GetUniqueIdByEmailIdParams> =
      GetUniqueIdByEmailIdParamSetter()
}

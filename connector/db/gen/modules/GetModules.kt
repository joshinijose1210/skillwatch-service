package modules

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Int
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

class GetModulesParams

class GetModulesParamSetter : ParamSetter<GetModulesParams> {
  override fun map(ps: PreparedStatement, params: GetModulesParams) {
  }
}

data class GetModulesResult(
  val id: Int,
  val name: String
)

class GetModulesRowMapper : RowMapper<GetModulesResult> {
  override fun map(rs: ResultSet): GetModulesResult = GetModulesResult(
  id = rs.getObject("id") as kotlin.Int,
    name = rs.getObject("name") as kotlin.String)
}

class GetModulesQuery : Query<GetModulesParams, GetModulesResult> {
  override val sql: String = """
      |SELECT
      |  id,
      |  name
      |FROM
      |  modules
      |ORDER BY
      | id ASC ;
      |""".trimMargin()

  override val mapper: RowMapper<GetModulesResult> = GetModulesRowMapper()

  override val paramSetter: ParamSetter<GetModulesParams> = GetModulesParamSetter()
}

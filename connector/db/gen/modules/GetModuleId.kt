package modules

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Int
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetModuleIdParams(
  val moduleName: String?
)

class GetModuleIdParamSetter : ParamSetter<GetModuleIdParams> {
  override fun map(ps: PreparedStatement, params: GetModuleIdParams) {
    ps.setObject(1, params.moduleName)
  }
}

data class GetModuleIdResult(
  val id: Int
)

class GetModuleIdRowMapper : RowMapper<GetModuleIdResult> {
  override fun map(rs: ResultSet): GetModuleIdResult = GetModuleIdResult(
  id = rs.getObject("id") as kotlin.Int)
}

class GetModuleIdQuery : Query<GetModuleIdParams, GetModuleIdResult> {
  override val sql: String = """
      |SELECT id
      |    FROM modules
      |    WHERE UPPER(name) = UPPER(?) ;
      |""".trimMargin()

  override val mapper: RowMapper<GetModuleIdResult> = GetModuleIdRowMapper()

  override val paramSetter: ParamSetter<GetModuleIdParams> = GetModuleIdParamSetter()
}

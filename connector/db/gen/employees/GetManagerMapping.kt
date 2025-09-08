package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetManagerMappingParams(
  val id: Long?
)

class GetManagerMappingParamSetter : ParamSetter<GetManagerMappingParams> {
  override fun map(ps: PreparedStatement, params: GetManagerMappingParams) {
    ps.setObject(1, params.id)
    ps.setObject(2, params.id)
  }
}

data class GetManagerMappingResult(
  val firstManagerId: Long?,
  val secondManagerId: Long?
)

class GetManagerMappingRowMapper : RowMapper<GetManagerMappingResult> {
  override fun map(rs: ResultSet): GetManagerMappingResult = GetManagerMappingResult(
  firstManagerId = rs.getObject("first_manager_id") as kotlin.Long?,
    secondManagerId = rs.getObject("second_manager_id") as kotlin.Long?)
}

class GetManagerMappingQuery : Query<GetManagerMappingParams, GetManagerMappingResult> {
  override val sql: String = """
      |SELECT
      |    MAX(CASE WHEN emp_id = ? AND type = 1 THEN manager_id END) AS first_manager_id,
      |    MAX(CASE WHEN emp_id = ? AND type = 2 THEN manager_id END) AS second_manager_id
      |FROM employee_manager_mapping_view ;
      |""".trimMargin()

  override val mapper: RowMapper<GetManagerMappingResult> = GetManagerMappingRowMapper()

  override val paramSetter: ParamSetter<GetManagerMappingParams> = GetManagerMappingParamSetter()
}

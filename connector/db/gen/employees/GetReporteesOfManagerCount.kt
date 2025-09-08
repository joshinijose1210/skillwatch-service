package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetReporteesOfManagerCountParams(
  val organisationId: Long?,
  val managerId: Long?
)

class GetReporteesOfManagerCountParamSetter : ParamSetter<GetReporteesOfManagerCountParams> {
  override fun map(ps: PreparedStatement, params: GetReporteesOfManagerCountParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.managerId)
    ps.setObject(3, params.managerId)
    ps.setObject(4, params.managerId)
    ps.setObject(5, params.managerId)
  }
}

data class GetReporteesOfManagerCountResult(
  val reporteesCount: Long?
)

class GetReporteesOfManagerCountRowMapper : RowMapper<GetReporteesOfManagerCountResult> {
  override fun map(rs: ResultSet): GetReporteesOfManagerCountResult =
      GetReporteesOfManagerCountResult(
  reporteesCount = rs.getObject("reportees_count") as kotlin.Long?)
}

class GetReporteesOfManagerCountQuery : Query<GetReporteesOfManagerCountParams,
    GetReporteesOfManagerCountResult> {
  override val sql: String = """
      |SELECT COUNT(employees.id) AS reportees_count
      |FROM
      |  employees
      |  JOIN employee_manager_mapping_view AS firstManagerMapping ON employees.id = firstManagerMapping.emp_id
      |  AND firstManagerMapping.type = 1
      |  LEFT JOIN employee_manager_mapping_view AS secondManagerMapping ON employees.id = secondManagerMapping.emp_id
      |  AND secondManagerMapping.type = 2
      |WHERE
      |  employees.organisation_id = ?
      |  AND employees.status = true
      |  AND ((firstManagerMapping.manager_id = ? AND firstManagerMapping.emp_id != ?)
      |  OR (secondManagerMapping.manager_id = ? AND secondManagerMapping.emp_id != ?));
      """.trimMargin()

  override val mapper: RowMapper<GetReporteesOfManagerCountResult> =
      GetReporteesOfManagerCountRowMapper()

  override val paramSetter: ParamSetter<GetReporteesOfManagerCountParams> =
      GetReporteesOfManagerCountParamSetter()
}

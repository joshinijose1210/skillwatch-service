package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetReporteesOfManagerParams(
  val organisationId: Long?,
  val managerId: Long?,
  val offset: Int?,
  val limit: Int?
)

class GetReporteesOfManagerParamSetter : ParamSetter<GetReporteesOfManagerParams> {
  override fun map(ps: PreparedStatement, params: GetReporteesOfManagerParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.managerId)
    ps.setObject(3, params.managerId)
    ps.setObject(4, params.managerId)
    ps.setObject(5, params.managerId)
    ps.setObject(6, params.offset)
    ps.setObject(7, params.limit)
  }
}

data class GetReporteesOfManagerResult(
  val organisationId: Long,
  val id: Long,
  val firstName: String,
  val lastName: String,
  val empId: String,
  val emailId: String,
  val firstManagerId: Long?,
  val secondManagerId: Long?
)

class GetReporteesOfManagerRowMapper : RowMapper<GetReporteesOfManagerResult> {
  override fun map(rs: ResultSet): GetReporteesOfManagerResult = GetReporteesOfManagerResult(
  organisationId = rs.getObject("organisation_id") as kotlin.Long,
    id = rs.getObject("id") as kotlin.Long,
    firstName = rs.getObject("first_name") as kotlin.String,
    lastName = rs.getObject("last_name") as kotlin.String,
    empId = rs.getObject("emp_id") as kotlin.String,
    emailId = rs.getObject("email_id") as kotlin.String,
    firstManagerId = rs.getObject("first_manager_id") as kotlin.Long?,
    secondManagerId = rs.getObject("second_manager_id") as kotlin.Long?)
}

class GetReporteesOfManagerQuery : Query<GetReporteesOfManagerParams, GetReporteesOfManagerResult> {
  override val sql: String = """
      |SELECT
      |  employees.organisation_id,
      |  employees.id,
      |  employees.first_name,
      |  employees.last_name,
      |  employees.emp_id,
      |  employees.email_id,
      |  COALESCE(firstManagerMapping.manager_id, null) AS first_manager_id,
      |  COALESCE(secondManagerMapping.manager_id, null) AS second_manager_id
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
      |  OR (secondManagerMapping.manager_id = ? AND secondManagerMapping.emp_id != ?))
      |ORDER BY
      |  employees.first_name
      |OFFSET (?::INT)
      |LIMIT (?::INT);
      """.trimMargin()

  override val mapper: RowMapper<GetReporteesOfManagerResult> = GetReporteesOfManagerRowMapper()

  override val paramSetter: ParamSetter<GetReporteesOfManagerParams> =
      GetReporteesOfManagerParamSetter()
}

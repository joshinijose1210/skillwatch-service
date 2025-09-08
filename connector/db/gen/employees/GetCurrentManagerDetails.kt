package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetCurrentManagerDetailsParams(
  val organisationId: Long?,
  val id: Long?
)

class GetCurrentManagerDetailsParamSetter : ParamSetter<GetCurrentManagerDetailsParams> {
  override fun map(ps: PreparedStatement, params: GetCurrentManagerDetailsParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.id)
  }
}

data class GetCurrentManagerDetailsResult(
  val firstManagerId: Long,
  val firstManagerEmployeeId: String,
  val firstManagerFirstName: String,
  val firstManagerLastName: String,
  val secondManagerId: Long?,
  val secondManagerEmployeeId: String?,
  val secondManagerFirstName: String?,
  val secondManagerLastName: String?
)

class GetCurrentManagerDetailsRowMapper : RowMapper<GetCurrentManagerDetailsResult> {
  override fun map(rs: ResultSet): GetCurrentManagerDetailsResult = GetCurrentManagerDetailsResult(
  firstManagerId = rs.getObject("first_manager_id") as kotlin.Long,
    firstManagerEmployeeId = rs.getObject("first_manager_employee_id") as kotlin.String,
    firstManagerFirstName = rs.getObject("first_manager_first_name") as kotlin.String,
    firstManagerLastName = rs.getObject("first_manager_last_name") as kotlin.String,
    secondManagerId = rs.getObject("second_manager_id") as kotlin.Long?,
    secondManagerEmployeeId = rs.getObject("second_manager_employee_id") as kotlin.String?,
    secondManagerFirstName = rs.getObject("second_manager_first_name") as kotlin.String?,
    secondManagerLastName = rs.getObject("second_manager_last_name") as kotlin.String?)
}

class GetCurrentManagerDetailsQuery : Query<GetCurrentManagerDetailsParams,
    GetCurrentManagerDetailsResult> {
  override val sql: String = """
      |SELECT
      |  first_manager.manager_id AS first_manager_id,
      |  first_manager.manager_employee_id AS first_manager_employee_id,
      |  first_manager.first_name AS first_manager_first_name,
      |  first_manager.last_name AS first_manager_last_name,
      |  COALESCE(second_manager.manager_id, null) AS second_manager_id,
      |  COALESCE(second_manager.manager_employee_id, null) AS second_manager_employee_id,
      |  COALESCE(second_manager.first_name, null) AS second_manager_first_name,
      |  COALESCE(second_manager.last_name, null) AS second_manager_last_name
      |FROM
      |  employees
      |  JOIN employee_manager_mapping_view AS first_manager ON employees.id = first_manager.emp_id
      |  AND first_manager.type = 1
      |  LEFT JOIN employee_manager_mapping_view AS second_manager ON employees.id = second_manager.emp_id
      |  AND second_manager.type = 2
      |WHERE
      |  employees.status = true
      |  AND employees.organisation_id = ?
      |  AND employees.id = ?;
      """.trimMargin()

  override val mapper: RowMapper<GetCurrentManagerDetailsResult> =
      GetCurrentManagerDetailsRowMapper()

  override val paramSetter: ParamSetter<GetCurrentManagerDetailsParams> =
      GetCurrentManagerDetailsParamSetter()
}

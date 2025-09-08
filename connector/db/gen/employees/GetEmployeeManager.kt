package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Array
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetEmployeeManagerParams(
  val id: Array<Int>?,
  val firstManagerId: Array<Int>?,
  val secondManagerId: Array<Int>?,
  val organisationId: Long?,
  val offset: Int?,
  val limit: Int?
)

class GetEmployeeManagerParamSetter : ParamSetter<GetEmployeeManagerParams> {
  override fun map(ps: PreparedStatement, params: GetEmployeeManagerParams) {
    ps.setArray(1, ps.connection.createArrayOf("int4", params.id))
    ps.setArray(2, ps.connection.createArrayOf("int4", params.id))
    ps.setArray(3, ps.connection.createArrayOf("int4", params.firstManagerId))
    ps.setArray(4, ps.connection.createArrayOf("int4", params.firstManagerId))
    ps.setArray(5, ps.connection.createArrayOf("int4", params.firstManagerId))
    ps.setArray(6, ps.connection.createArrayOf("int4", params.secondManagerId))
    ps.setArray(7, ps.connection.createArrayOf("int4", params.secondManagerId))
    ps.setArray(8, ps.connection.createArrayOf("int4", params.secondManagerId))
    ps.setObject(9, params.organisationId)
    ps.setObject(10, params.offset)
    ps.setObject(11, params.limit)
  }
}

data class GetEmployeeManagerResult(
  val organisationId: Long,
  val id: Long,
  val employeeId: String,
  val firstName: String,
  val lastName: String,
  val emailId: String,
  val firstManagerId: Long?,
  val firstManagerEmployeeId: String?,
  val firstManagerFirstName: String?,
  val firstManagerLastName: String?,
  val secondManagerId: Long?,
  val secondManagerEmployeeId: String?,
  val secondManagerFirstName: String?,
  val secondManagerLastName: String?
)

class GetEmployeeManagerRowMapper : RowMapper<GetEmployeeManagerResult> {
  override fun map(rs: ResultSet): GetEmployeeManagerResult = GetEmployeeManagerResult(
  organisationId = rs.getObject("organisation_id") as kotlin.Long,
    id = rs.getObject("id") as kotlin.Long,
    employeeId = rs.getObject("employee_id") as kotlin.String,
    firstName = rs.getObject("first_name") as kotlin.String,
    lastName = rs.getObject("last_name") as kotlin.String,
    emailId = rs.getObject("email_id") as kotlin.String,
    firstManagerId = rs.getObject("first_manager_id") as kotlin.Long?,
    firstManagerEmployeeId = rs.getObject("first_manager_employee_id") as kotlin.String?,
    firstManagerFirstName = rs.getObject("first_manager_first_name") as kotlin.String?,
    firstManagerLastName = rs.getObject("first_manager_last_name") as kotlin.String?,
    secondManagerId = rs.getObject("second_manager_id") as kotlin.Long?,
    secondManagerEmployeeId = rs.getObject("second_manager_employee_id") as kotlin.String?,
    secondManagerFirstName = rs.getObject("second_manager_first_name") as kotlin.String?,
    secondManagerLastName = rs.getObject("second_manager_last_name") as kotlin.String?)
}

class GetEmployeeManagerQuery : Query<GetEmployeeManagerParams, GetEmployeeManagerResult> {
  override val sql: String = """
      |SELECT
      |  employees.organisation_id,
      |  employees.id,
      |  employees.emp_id AS employee_id,
      |  employees.first_name,
      |  employees.last_name,
      |  employees.email_id,
      |  COALESCE(firstManagerMappingData.manager_id, null) AS first_manager_id,
      |  COALESCE(firstManagerMappingData.manager_employee_id, null) AS first_manager_employee_id,
      |  COALESCE(firstManagerMappingData.first_name, null) AS first_manager_first_name,
      |  COALESCE(firstManagerMappingData.last_name,null) AS first_manager_last_name,
      |  COALESCE(secondManagerMappingData.manager_id, null) AS second_manager_id,
      |  COALESCE(secondManagerMappingData.manager_employee_id, null) AS second_manager_employee_id,
      |  COALESCE(secondManagerMappingData.first_name, null) AS second_manager_first_name,
      |  COALESCE(secondManagerMappingData.last_name, null) AS second_manager_last_name
      |FROM
      |  employees
      |  LEFT JOIN employee_manager_mapping_view AS firstManagerMappingData ON employees.id = firstManagerMappingData.emp_id
      |  AND firstManagerMappingData.type = 1
      |  LEFT JOIN employee_manager_mapping_view AS secondManagerMappingData ON employees.id = secondManagerMappingData.emp_id
      |  AND secondManagerMappingData.type = 2
      |WHERE
      |  (?::INT[] = '{-99}' OR employees.id = ANY (?::INT[]))
      |  AND (?::INT[] = '{-99}'
      |  OR (firstManagerMappingData.manager_id = ANY (?::INT[]) AND firstManagerMappingData.emp_id != ANY (?::INT[])))
      |  AND (?::INT[] = '{-99}'
      |  OR (secondManagerMappingData.manager_id = ANY (?::INT[]) AND secondManagerMappingData.emp_id != ANY (?::INT[])))
      |  AND employees.status = true
      |  AND employees.organisation_id = ?
      |ORDER BY employees.emp_id
      |OFFSET (?::INT)
      |LIMIT (?::INT);
      """.trimMargin()

  override val mapper: RowMapper<GetEmployeeManagerResult> = GetEmployeeManagerRowMapper()

  override val paramSetter: ParamSetter<GetEmployeeManagerParams> = GetEmployeeManagerParamSetter()
}

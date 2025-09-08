package departments

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAllDepartmentsCountParams(
  val organisationId: Long?,
  val searchText: String?
)

class GetAllDepartmentsCountParamSetter : ParamSetter<GetAllDepartmentsCountParams> {
  override fun map(ps: PreparedStatement, params: GetAllDepartmentsCountParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.searchText)
    ps.setObject(3, params.searchText)
  }
}

data class GetAllDepartmentsCountResult(
  val departmentCount: Long?
)

class GetAllDepartmentsCountRowMapper : RowMapper<GetAllDepartmentsCountResult> {
  override fun map(rs: ResultSet): GetAllDepartmentsCountResult = GetAllDepartmentsCountResult(
  departmentCount = rs.getObject("department_count") as kotlin.Long?)
}

class GetAllDepartmentsCountQuery : Query<GetAllDepartmentsCountParams,
    GetAllDepartmentsCountResult> {
  override val sql: String = """
      |SELECT COUNT(departments.department_id) AS department_count
      |FROM departments
      |WHERE organisation_id = ?
      |    AND (cast(? as text) IS NULL
      |    OR UPPER(departments.department_name) LIKE UPPER(?)) ;
      """.trimMargin()

  override val mapper: RowMapper<GetAllDepartmentsCountResult> = GetAllDepartmentsCountRowMapper()

  override val paramSetter: ParamSetter<GetAllDepartmentsCountParams> =
      GetAllDepartmentsCountParamSetter()
}

package departments

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetDepartmentDataByIdParams(
  val id: Long?,
  val organisationId: Long?
)

class GetDepartmentDataByIdParamSetter : ParamSetter<GetDepartmentDataByIdParams> {
  override fun map(ps: PreparedStatement, params: GetDepartmentDataByIdParams) {
    ps.setObject(1, params.id)
    ps.setObject(2, params.organisationId)
  }
}

data class GetDepartmentDataByIdResult(
  val organisationId: Long,
  val id: Long,
  val displayId: Long,
  val departmentName: String,
  val status: Boolean,
  val createdAt: Timestamp,
  val updatedAt: Timestamp?
)

class GetDepartmentDataByIdRowMapper : RowMapper<GetDepartmentDataByIdResult> {
  override fun map(rs: ResultSet): GetDepartmentDataByIdResult = GetDepartmentDataByIdResult(
  organisationId = rs.getObject("organisation_id") as kotlin.Long,
    id = rs.getObject("id") as kotlin.Long,
    displayId = rs.getObject("display_id") as kotlin.Long,
    departmentName = rs.getObject("department_name") as kotlin.String,
    status = rs.getObject("status") as kotlin.Boolean,
    createdAt = rs.getObject("created_at") as java.sql.Timestamp,
    updatedAt = rs.getObject("updated_at") as java.sql.Timestamp?)
}

class GetDepartmentDataByIdQuery : Query<GetDepartmentDataByIdParams, GetDepartmentDataByIdResult> {
  override val sql: String = """
      |SELECT
      |  organisation_id,
      |  id,
      |  department_id as display_id,
      |  department_name,
      |  status,
      |  created_at,
      |  updated_at
      |FROM
      |  departments
      |WHERE
      |  id = ?
      |  AND organisation_id = ? ;
      """.trimMargin()

  override val mapper: RowMapper<GetDepartmentDataByIdResult> = GetDepartmentDataByIdRowMapper()

  override val paramSetter: ParamSetter<GetDepartmentDataByIdParams> =
      GetDepartmentDataByIdParamSetter()
}

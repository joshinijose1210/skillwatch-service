package departments

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAllDepartmentsParams(
  val organisationId: Long?,
  val searchText: String?,
  val offset: Int?,
  val limit: Int?
)

class GetAllDepartmentsParamSetter : ParamSetter<GetAllDepartmentsParams> {
  override fun map(ps: PreparedStatement, params: GetAllDepartmentsParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.searchText)
    ps.setObject(3, params.searchText)
    ps.setObject(4, params.offset)
    ps.setObject(5, params.limit)
  }
}

data class GetAllDepartmentsResult(
  val id: Long,
  val organisationId: Long,
  val departmentId: Long,
  val departmentName: String,
  val createdAt: Timestamp,
  val updatedAt: Timestamp?,
  val status: Boolean
)

class GetAllDepartmentsRowMapper : RowMapper<GetAllDepartmentsResult> {
  override fun map(rs: ResultSet): GetAllDepartmentsResult = GetAllDepartmentsResult(
  id = rs.getObject("id") as kotlin.Long,
    organisationId = rs.getObject("organisation_id") as kotlin.Long,
    departmentId = rs.getObject("department_id") as kotlin.Long,
    departmentName = rs.getObject("department_name") as kotlin.String,
    createdAt = rs.getObject("created_at") as java.sql.Timestamp,
    updatedAt = rs.getObject("updated_at") as java.sql.Timestamp?,
    status = rs.getObject("status") as kotlin.Boolean)
}

class GetAllDepartmentsQuery : Query<GetAllDepartmentsParams, GetAllDepartmentsResult> {
  override val sql: String = """
      |SELECT
      |  id,
      |  organisation_id,
      |  department_id,
      |  department_name,
      |  created_at,
      |  updated_at,
      |  status
      |FROM
      |  departments
      |WHERE
      |  organisation_id = ?
      |  AND (cast(? as text) IS NULL
      |  OR UPPER(departments.department_name) LIKE UPPER(?))
      |ORDER BY
      |  department_id DESC
      |OFFSET (?::INT)
      |LIMIT (?::INT);
      """.trimMargin()

  override val mapper: RowMapper<GetAllDepartmentsResult> = GetAllDepartmentsRowMapper()

  override val paramSetter: ParamSetter<GetAllDepartmentsParams> = GetAllDepartmentsParamSetter()
}

package departments

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetMaxDepartmentIdParams(
  val organisationId: Long?
)

class GetMaxDepartmentIdParamSetter : ParamSetter<GetMaxDepartmentIdParams> {
  override fun map(ps: PreparedStatement, params: GetMaxDepartmentIdParams) {
    ps.setObject(1, params.organisationId)
  }
}

data class GetMaxDepartmentIdResult(
  val maxId: Long?
)

class GetMaxDepartmentIdRowMapper : RowMapper<GetMaxDepartmentIdResult> {
  override fun map(rs: ResultSet): GetMaxDepartmentIdResult = GetMaxDepartmentIdResult(
  maxId = rs.getObject("max_id") as kotlin.Long?)
}

class GetMaxDepartmentIdQuery : Query<GetMaxDepartmentIdParams, GetMaxDepartmentIdResult> {
  override val sql: String =
      "SELECT MAX(department_id) as max_id FROM departments WHERE organisation_id = ?;"

  override val mapper: RowMapper<GetMaxDepartmentIdResult> = GetMaxDepartmentIdRowMapper()

  override val paramSetter: ParamSetter<GetMaxDepartmentIdParams> = GetMaxDepartmentIdParamSetter()
}

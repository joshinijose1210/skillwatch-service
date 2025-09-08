package departments

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class IsDepartmentExistsParams(
  val organisationId: Long?,
  val departmentName: String?
)

class IsDepartmentExistsParamSetter : ParamSetter<IsDepartmentExistsParams> {
  override fun map(ps: PreparedStatement, params: IsDepartmentExistsParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.departmentName)
  }
}

data class IsDepartmentExistsResult(
  val exists: Boolean?,
  val status: Boolean?
)

class IsDepartmentExistsRowMapper : RowMapper<IsDepartmentExistsResult> {
  override fun map(rs: ResultSet): IsDepartmentExistsResult = IsDepartmentExistsResult(
  exists = rs.getObject("exists") as kotlin.Boolean?,
    status = rs.getObject("status") as kotlin.Boolean?)
}

class IsDepartmentExistsQuery : Query<IsDepartmentExistsParams, IsDepartmentExistsResult> {
  override val sql: String = """
      |WITH subquery AS (
      |    SELECT status
      |    FROM departments
      |    WHERE
      |    organisation_id = ?
      |    AND LOWER(department_name) = LOWER(?)
      |)
      |SELECT EXISTS (SELECT 1 FROM subquery), (SELECT status FROM subquery);
      """.trimMargin()

  override val mapper: RowMapper<IsDepartmentExistsResult> = IsDepartmentExistsRowMapper()

  override val paramSetter: ParamSetter<IsDepartmentExistsParams> = IsDepartmentExistsParamSetter()
}

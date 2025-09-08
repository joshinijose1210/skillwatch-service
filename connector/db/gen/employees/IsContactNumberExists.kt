package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class IsContactNumberExistsParams(
  val contactNo: String?,
  val organisationId: Long?
)

class IsContactNumberExistsParamSetter : ParamSetter<IsContactNumberExistsParams> {
  override fun map(ps: PreparedStatement, params: IsContactNumberExistsParams) {
    ps.setObject(1, params.contactNo)
    ps.setObject(2, params.organisationId)
  }
}

data class IsContactNumberExistsResult(
  val exists: Boolean?
)

class IsContactNumberExistsRowMapper : RowMapper<IsContactNumberExistsResult> {
  override fun map(rs: ResultSet): IsContactNumberExistsResult = IsContactNumberExistsResult(
  exists = rs.getObject("exists") as kotlin.Boolean?)
}

class IsContactNumberExistsQuery : Query<IsContactNumberExistsParams, IsContactNumberExistsResult> {
  override val sql: String = """
      |SELECT EXISTS (
      |  SELECT 1 FROM employees WHERE
      |      contact_no = ?
      |      AND organisation_id = ?
      |) ;
      |""".trimMargin()

  override val mapper: RowMapper<IsContactNumberExistsResult> = IsContactNumberExistsRowMapper()

  override val paramSetter: ParamSetter<IsContactNumberExistsParams> =
      IsContactNumberExistsParamSetter()
}

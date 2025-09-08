package kra

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class IsKraExistsParams(
  val kra: String?,
  val organisationId: Long?
)

class IsKraExistsParamSetter : ParamSetter<IsKraExistsParams> {
  override fun map(ps: PreparedStatement, params: IsKraExistsParams) {
    ps.setObject(1, params.kra)
    ps.setObject(2, params.organisationId)
  }
}

data class IsKraExistsResult(
  val exists: Boolean?
)

class IsKraExistsRowMapper : RowMapper<IsKraExistsResult> {
  override fun map(rs: ResultSet): IsKraExistsResult = IsKraExistsResult(
  exists = rs.getObject("exists") as kotlin.Boolean?)
}

class IsKraExistsQuery : Query<IsKraExistsParams, IsKraExistsResult> {
  override val sql: String = """
      |SELECT EXISTS (
      |  SELECT 1 FROM kra WHERE
      |     LOWER(name) = LOWER(?) AND organisation_id = ?
      |) ;
      """.trimMargin()

  override val mapper: RowMapper<IsKraExistsResult> = IsKraExistsRowMapper()

  override val paramSetter: ParamSetter<IsKraExistsParams> = IsKraExistsParamSetter()
}

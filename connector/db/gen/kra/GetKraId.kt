package kra

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetKraIdParams(
  val organisationId: Long?,
  val kraName: String?
)

class GetKraIdParamSetter : ParamSetter<GetKraIdParams> {
  override fun map(ps: PreparedStatement, params: GetKraIdParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.kraName)
  }
}

data class GetKraIdResult(
  val id: Long
)

class GetKraIdRowMapper : RowMapper<GetKraIdResult> {
  override fun map(rs: ResultSet): GetKraIdResult = GetKraIdResult(
  id = rs.getObject("id") as kotlin.Long)
}

class GetKraIdQuery : Query<GetKraIdParams, GetKraIdResult> {
  override val sql: String = """
      |SELECT id FROM kra WHERE
      |      organisation_id = ?
      |      AND LOWER(name) = LOWER(?);
      |""".trimMargin()

  override val mapper: RowMapper<GetKraIdResult> = GetKraIdRowMapper()

  override val paramSetter: ParamSetter<GetKraIdParams> = GetKraIdParamSetter()
}

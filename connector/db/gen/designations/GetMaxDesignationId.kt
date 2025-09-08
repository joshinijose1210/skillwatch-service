package designations

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetMaxDesignationIdParams(
  val organisationId: Long?
)

class GetMaxDesignationIdParamSetter : ParamSetter<GetMaxDesignationIdParams> {
  override fun map(ps: PreparedStatement, params: GetMaxDesignationIdParams) {
    ps.setObject(1, params.organisationId)
  }
}

data class GetMaxDesignationIdResult(
  val maxId: Long?
)

class GetMaxDesignationIdRowMapper : RowMapper<GetMaxDesignationIdResult> {
  override fun map(rs: ResultSet): GetMaxDesignationIdResult = GetMaxDesignationIdResult(
  maxId = rs.getObject("max_id") as kotlin.Long?)
}

class GetMaxDesignationIdQuery : Query<GetMaxDesignationIdParams, GetMaxDesignationIdResult> {
  override val sql: String = """
      |SELECT MAX(designation_id) as max_id FROM designations WHERE organisation_id = ?;
      |""".trimMargin()

  override val mapper: RowMapper<GetMaxDesignationIdResult> = GetMaxDesignationIdRowMapper()

  override val paramSetter: ParamSetter<GetMaxDesignationIdParams> =
      GetMaxDesignationIdParamSetter()
}

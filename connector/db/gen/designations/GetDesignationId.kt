package designations

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetDesignationIdParams(
  val teamId: Long?,
  val designationName: String?,
  val organisationId: Long?
)

class GetDesignationIdParamSetter : ParamSetter<GetDesignationIdParams> {
  override fun map(ps: PreparedStatement, params: GetDesignationIdParams) {
    ps.setObject(1, params.teamId)
    ps.setObject(2, params.designationName)
    ps.setObject(3, params.organisationId)
  }
}

data class GetDesignationIdResult(
  val id: Long
)

class GetDesignationIdRowMapper : RowMapper<GetDesignationIdResult> {
  override fun map(rs: ResultSet): GetDesignationIdResult = GetDesignationIdResult(
  id = rs.getObject("id") as kotlin.Long)
}

class GetDesignationIdQuery : Query<GetDesignationIdParams, GetDesignationIdResult> {
  override val sql: String = """
      |SELECT id FROM designations
      |      JOIN team_designation_mapping tdm ON designations.id = tdm.designation_id
      |      WHERE tdm.team_id = ?
      |      AND LOWER(designation_name) = LOWER(?)
      |      AND organisation_id = ?
      |      AND designations.status = true;
      |""".trimMargin()

  override val mapper: RowMapper<GetDesignationIdResult> = GetDesignationIdRowMapper()

  override val paramSetter: ParamSetter<GetDesignationIdParams> = GetDesignationIdParamSetter()
}

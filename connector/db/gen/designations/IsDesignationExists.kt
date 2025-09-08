package designations

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class IsDesignationExistsParams(
  val organisationId: Long?,
  val designationName: String?,
  val teamId: Long?
)

class IsDesignationExistsParamSetter : ParamSetter<IsDesignationExistsParams> {
  override fun map(ps: PreparedStatement, params: IsDesignationExistsParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.designationName)
    ps.setObject(3, params.teamId)
  }
}

data class IsDesignationExistsResult(
  val exists: Boolean?,
  val status: Boolean?
)

class IsDesignationExistsRowMapper : RowMapper<IsDesignationExistsResult> {
  override fun map(rs: ResultSet): IsDesignationExistsResult = IsDesignationExistsResult(
  exists = rs.getObject("exists") as kotlin.Boolean?,
    status = rs.getObject("status") as kotlin.Boolean?)
}

class IsDesignationExistsQuery : Query<IsDesignationExistsParams, IsDesignationExistsResult> {
  override val sql: String = """
      |WITH subquery AS (
      |    SELECT d.status
      |    FROM designations AS d
      |    JOIN team_designation_mapping tdm ON d.id = tdm.designation_id
      |    WHERE
      |    d.organisation_id = ?
      |    AND LOWER(d.designation_name) = LOWER(?)
      |    AND tdm.team_id = ?
      |)
      |SELECT EXISTS (SELECT 1 FROM subquery), (SELECT status FROM subquery);
      """.trimMargin()

  override val mapper: RowMapper<IsDesignationExistsResult> = IsDesignationExistsRowMapper()

  override val paramSetter: ParamSetter<IsDesignationExistsParams> =
      IsDesignationExistsParamSetter()
}

package designations

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Array
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetDesignationsCountParams(
  val organisationId: Long?,
  val searchText: String?,
  val teamId: Array<Int>?,
  val departmentId: Array<Int>?
)

class GetDesignationsCountParamSetter : ParamSetter<GetDesignationsCountParams> {
  override fun map(ps: PreparedStatement, params: GetDesignationsCountParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.searchText)
    ps.setObject(3, params.searchText)
    ps.setObject(4, params.searchText)
    ps.setObject(5, params.searchText)
    ps.setArray(6, ps.connection.createArrayOf("int4", params.teamId))
    ps.setArray(7, ps.connection.createArrayOf("int4", params.teamId))
    ps.setArray(8, ps.connection.createArrayOf("int4", params.departmentId))
    ps.setArray(9, ps.connection.createArrayOf("int4", params.departmentId))
  }
}

data class GetDesignationsCountResult(
  val designationCount: Long?
)

class GetDesignationsCountRowMapper : RowMapper<GetDesignationsCountResult> {
  override fun map(rs: ResultSet): GetDesignationsCountResult = GetDesignationsCountResult(
  designationCount = rs.getObject("designation_count") as kotlin.Long?)
}

class GetDesignationsCountQuery : Query<GetDesignationsCountParams, GetDesignationsCountResult> {
  override val sql: String = """
      |SELECT
      |COUNT(designations.designation_id)
      |as designation_count
      |FROM designations
      |  LEFT JOIN team_designation_mapping tdm ON tdm.designation_id = designations.id
      |  LEFT JOIN teams t ON t.id = tdm.team_id
      |  LEFT JOIN department_team_mapping dtm ON dtm.team_id = t.id
      |  LEFT JOIN departments d ON d.id = dtm.department_id
      |WHERE designations.organisation_id = ?
      |  AND (cast(? as text) IS NULL
      |  OR UPPER(designations.designation_name) LIKE UPPER(?)
      |  OR UPPER(t.team_name) LIKE UPPER(?)
      |  OR UPPER(d.department_name) LIKE UPPER(?))
      |  AND (?::INT[] = '{-99}' OR tdm.team_id = ANY (?::INT[]))
      |  AND (?::INT[] = '{-99}' OR dtm.department_id = ANY (?::INT[]));
      """.trimMargin()

  override val mapper: RowMapper<GetDesignationsCountResult> = GetDesignationsCountRowMapper()

  override val paramSetter: ParamSetter<GetDesignationsCountParams> =
      GetDesignationsCountParamSetter()
}

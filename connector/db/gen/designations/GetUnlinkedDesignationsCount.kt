package designations

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetUnlinkedDesignationsCountParams(
  val organisationId: Long?
)

class GetUnlinkedDesignationsCountParamSetter : ParamSetter<GetUnlinkedDesignationsCountParams> {
  override fun map(ps: PreparedStatement, params: GetUnlinkedDesignationsCountParams) {
    ps.setObject(1, params.organisationId)
  }
}

data class GetUnlinkedDesignationsCountResult(
  val unlinkedDesignationCount: Long?
)

class GetUnlinkedDesignationsCountRowMapper : RowMapper<GetUnlinkedDesignationsCountResult> {
  override fun map(rs: ResultSet): GetUnlinkedDesignationsCountResult =
      GetUnlinkedDesignationsCountResult(
  unlinkedDesignationCount = rs.getObject("unlinked_designation_count") as kotlin.Long?)
}

class GetUnlinkedDesignationsCountQuery : Query<GetUnlinkedDesignationsCountParams,
    GetUnlinkedDesignationsCountResult> {
  override val sql: String = """
      |SELECT
      |  COUNT(designations.designation_id)
      |  AS unlinked_designation_count
      |FROM designations
      |  LEFT JOIN team_designation_mapping tdm ON tdm.designation_id = designations.id
      |  LEFT JOIN teams t ON t.id = tdm.team_id
      |WHERE designations.organisation_id = ?
      |  AND tdm.designation_id IS NULL
      |  AND designations.designation_name != 'Org Admin' ;
      """.trimMargin()

  override val mapper: RowMapper<GetUnlinkedDesignationsCountResult> =
      GetUnlinkedDesignationsCountRowMapper()

  override val paramSetter: ParamSetter<GetUnlinkedDesignationsCountParams> =
      GetUnlinkedDesignationsCountParamSetter()
}

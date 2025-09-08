package suggestions

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Array
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAllSuggestionsCountParams(
  val organisationId: Long?,
  val suggestedById: Array<Int>?,
  val reviewCycleId: Array<Int>?,
  val isDraft: Array<String>?,
  val progressIds: Array<Int>?
)

class GetAllSuggestionsCountParamSetter : ParamSetter<GetAllSuggestionsCountParams> {
  override fun map(ps: PreparedStatement, params: GetAllSuggestionsCountParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.organisationId)
    ps.setArray(3, ps.connection.createArrayOf("int4", params.suggestedById))
    ps.setArray(4, ps.connection.createArrayOf("int4", params.suggestedById))
    ps.setArray(5, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setArray(6, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setArray(7, ps.connection.createArrayOf("bool", params.isDraft))
    ps.setArray(8, ps.connection.createArrayOf("bool", params.isDraft))
    ps.setArray(9, ps.connection.createArrayOf("bool", params.isDraft))
    ps.setArray(10, ps.connection.createArrayOf("int4", params.progressIds))
    ps.setArray(11, ps.connection.createArrayOf("int4", params.progressIds))
  }
}

data class GetAllSuggestionsCountResult(
  val suggestionCount: Long?
)

class GetAllSuggestionsCountRowMapper : RowMapper<GetAllSuggestionsCountResult> {
  override fun map(rs: ResultSet): GetAllSuggestionsCountResult = GetAllSuggestionsCountResult(
  suggestionCount = rs.getObject("suggestion_count") as kotlin.Long?)
}

class GetAllSuggestionsCountQuery : Query<GetAllSuggestionsCountParams,
    GetAllSuggestionsCountResult> {
  override val sql: String = """
      |SELECT
      |  COUNT(suggestions.id) as suggestion_count
      |FROM
      |  suggestions
      |  JOIN employees AS suggested_by_details ON suggested_by_details.id = suggestions.suggested_by AND suggested_by_details.organisation_id = ?
      |  LEFT JOIN review_cycle ON DATE(suggestions.updated_at) BETWEEN review_cycle.start_date AND review_cycle.end_date
      |  AND review_cycle.organisation_id = ?
      |WHERE
      |  (?::INT[] = '{-99}' OR suggestions.suggested_by = ANY (?::INT[]))
      |  AND (?::INT[] = '{-99}' OR review_cycle.id = ANY(?::INT[]))
      |  AND (?::BOOL[] = '{true,false}'
      |      OR ?::BOOL[] = '{false,true}'
      |      OR suggestions.is_draft = ANY(?::BOOL[]))
      |  AND (?::INT[] = '{-99}' OR suggestions.progress_id = ANY(?::INT[]));
      |""".trimMargin()

  override val mapper: RowMapper<GetAllSuggestionsCountResult> = GetAllSuggestionsCountRowMapper()

  override val paramSetter: ParamSetter<GetAllSuggestionsCountParams> =
      GetAllSuggestionsCountParamSetter()
}

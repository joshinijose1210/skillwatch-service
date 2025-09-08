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

data class GetAllPendingSuggestionsCountParams(
  val organisationId: Long?,
  val reviewCycleId: Array<Int>?
)

class GetAllPendingSuggestionsCountParamSetter : ParamSetter<GetAllPendingSuggestionsCountParams> {
  override fun map(ps: PreparedStatement, params: GetAllPendingSuggestionsCountParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.organisationId)
    ps.setArray(3, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setArray(4, ps.connection.createArrayOf("int4", params.reviewCycleId))
  }
}

data class GetAllPendingSuggestionsCountResult(
  val suggestionCount: Long?
)

class GetAllPendingSuggestionsCountRowMapper : RowMapper<GetAllPendingSuggestionsCountResult> {
  override fun map(rs: ResultSet): GetAllPendingSuggestionsCountResult =
      GetAllPendingSuggestionsCountResult(
  suggestionCount = rs.getObject("suggestion_count") as kotlin.Long?)
}

class GetAllPendingSuggestionsCountQuery : Query<GetAllPendingSuggestionsCountParams,
    GetAllPendingSuggestionsCountResult> {
  override val sql: String = """
      |SELECT
      |  COUNT(suggestions.id) as suggestion_count
      |FROM
      |  suggestions
      |  JOIN employees AS suggested_by_details ON suggested_by_details.id = suggestions.suggested_by AND suggested_by_details.organisation_id = ?
      |  LEFT JOIN review_cycle ON DATE(suggestions.updated_at) BETWEEN review_cycle.start_date AND review_cycle.end_date
      |  AND review_cycle.organisation_id = ?
      |WHERE
      |  progress_id = 1 AND is_draft = false
      |  AND (?::INT[] = '{-99}' OR review_cycle.id = ANY(?::INT[]));
      """.trimMargin()

  override val mapper: RowMapper<GetAllPendingSuggestionsCountResult> =
      GetAllPendingSuggestionsCountRowMapper()

  override val paramSetter: ParamSetter<GetAllPendingSuggestionsCountParams> =
      GetAllPendingSuggestionsCountParamSetter()
}

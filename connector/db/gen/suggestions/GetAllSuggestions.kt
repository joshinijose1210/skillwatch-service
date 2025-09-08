package suggestions

/*
  This is manually created file, do not rely on NORM to generate it
*/

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Array
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper
import scalereal.core.models.domain.SuggestionComments
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.core.type.TypeReference

data class GetAllSuggestionsParams(
  val organisationId: Long?,
  val suggestedById: Array<Int>?,
  val reviewCycleId: Array<Int>?,
  val isDraft: Array<String>?,
  val progressIds: Array<Int>?,
  val sortBy: String?,
  val offset: Int?,
  val limit: Int?
)

class GetAllSuggestionsParamSetter : ParamSetter<GetAllSuggestionsParams> {
  override fun map(ps: PreparedStatement, params: GetAllSuggestionsParams) {
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
    ps.setObject(12, params.sortBy)
    ps.setObject(13, params.sortBy)
    ps.setObject(14, params.offset)
    ps.setObject(15, params.limit)
  }
}

data class GetAllSuggestionsResult(
  val id: Long,
  val updatedAt: Timestamp,
  val suggestedById: Long,
  val progressId: Int?,
  val empId: String,
  val firstName: String,
  val lastName: String,
  val suggestion: String,
  val isDraft: Boolean,
  val isAnonymous: Boolean,
  val organisationId: Long,
  val comments: List<SuggestionComments>
)

class GetAllSuggestionsRowMapper : RowMapper<GetAllSuggestionsResult> {
  companion object {
    private val objectMapper = jacksonObjectMapper()
  }

  override fun map(rs: ResultSet): GetAllSuggestionsResult {
    val commentsJson = rs.getString("comments")
    val comments: List<SuggestionComments> =
      objectMapper.readValue(commentsJson, object : TypeReference<List<SuggestionComments>>() {})

    return GetAllSuggestionsResult(
      id = rs.getObject("id") as kotlin.Long,
      updatedAt = rs.getObject("updated_at") as java.sql.Timestamp,
      suggestedById = rs.getObject("suggested_by_id") as kotlin.Long,
      progressId = rs.getObject("progress_id") as kotlin.Int?,
      empId = rs.getObject("emp_id") as kotlin.String,
      firstName = rs.getObject("first_name") as kotlin.String,
      lastName = rs.getObject("last_name") as kotlin.String,
      suggestion = rs.getObject("suggestion") as kotlin.String,
      isDraft = rs.getObject("is_draft") as kotlin.Boolean,
      isAnonymous = rs.getObject("is_anonymous") as kotlin.Boolean,
      organisationId = rs.getObject("organisation_id") as kotlin.Long,
      comments = comments
    )
  }
}

class GetAllSuggestionsQuery : Query<GetAllSuggestionsParams, GetAllSuggestionsResult> {
  override val sql: String = """
    SELECT
      s.id,
      s.updated_at,
      s.suggested_by AS suggested_by_id,
      s.progress_id,
      suggested_by_details.emp_id,
      suggested_by_details.first_name,
      suggested_by_details.last_name,
      s.suggestion,
      s.is_draft,
      s.is_anonymous,
      suggested_by_details.organisation_id,
      COALESCE(
          json_agg(
            json_build_object(
              'id', sc.id,
              'comment', sc.comment,
              'date', sc.created_at
            )
            ORDER BY sc.created_at DESC
          ) FILTER (WHERE sc.id IS NOT NULL),
          '[]'
      ) AS comments
    FROM
      suggestions AS s
      JOIN employees AS suggested_by_details
        ON suggested_by_details.id = s.suggested_by
        AND suggested_by_details.organisation_id = ?
      LEFT JOIN review_cycle
        ON DATE(s.updated_at) BETWEEN review_cycle.start_date AND review_cycle.end_date
        AND review_cycle.organisation_id = ?
      LEFT JOIN suggestion_comments sc
        ON sc.suggestion_id = s.id
    WHERE
      (?::INT[] = '{-99}' OR s.suggested_by = ANY (?::INT[]))
      AND (?::INT[] = '{-99}' OR review_cycle.id = ANY(?::INT[]))
      AND (?::BOOL[] = '{true,false}'
        OR ?::BOOL[] = '{false,true}'
        OR s.is_draft = ANY(?::BOOL[]))
      AND (?::INT[] = '{-99}' OR s.progress_id = ANY(?::INT[]))
    GROUP BY
      s.id, s.updated_at, s.suggested_by, s.progress_id,
      suggested_by_details.emp_id, suggested_by_details.first_name, suggested_by_details.last_name,
      s.suggestion, s.is_draft, s.is_anonymous,
      suggested_by_details.organisation_id
    ORDER BY
      CASE WHEN ? = 'dateDesc' THEN s.updated_at END DESC,
      CASE WHEN ? = 'dateAsc' THEN s.updated_at END ASC
    OFFSET (?::INT)
    LIMIT (?::INT);
""".trimMargin()


  override val mapper: RowMapper<GetAllSuggestionsResult> = GetAllSuggestionsRowMapper()

  override val paramSetter: ParamSetter<GetAllSuggestionsParams> = GetAllSuggestionsParamSetter()
}

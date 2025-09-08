package suggestions

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class AddSuggestionParams(
  val suggestion: String?,
  val suggested_by: Long?,
  val is_draft: Boolean?,
  val is_anonymous: Boolean?
)

class AddSuggestionParamSetter : ParamSetter<AddSuggestionParams> {
  override fun map(ps: PreparedStatement, params: AddSuggestionParams) {
    ps.setObject(1, params.suggestion)
    ps.setObject(2, params.suggested_by)
    ps.setObject(3, params.is_draft)
    ps.setObject(4, params.is_anonymous)
  }
}

data class AddSuggestionResult(
  val id: Long,
  val suggestion: String,
  val suggestedBy: Long,
  val isDraft: Boolean,
  val isAnonymous: Boolean
)

class AddSuggestionRowMapper : RowMapper<AddSuggestionResult> {
  override fun map(rs: ResultSet): AddSuggestionResult = AddSuggestionResult(
  id = rs.getObject("id") as kotlin.Long,
    suggestion = rs.getObject("suggestion") as kotlin.String,
    suggestedBy = rs.getObject("suggested_by") as kotlin.Long,
    isDraft = rs.getObject("is_draft") as kotlin.Boolean,
    isAnonymous = rs.getObject("is_anonymous") as kotlin.Boolean)
}

class AddSuggestionQuery : Query<AddSuggestionParams, AddSuggestionResult> {
  override val sql: String = """
      |INSERT INTO suggestions(
      |    suggestion,
      |    suggested_by,
      |    is_draft,
      |    is_anonymous,
      |    created_at,
      |    updated_at
      |) VALUES(
      |    ?,
      |    ?,
      |    ?,
      |    ?,
      |    CURRENT_TIMESTAMP,
      |    CURRENT_TIMESTAMP
      |) RETURNING id, suggestion, suggested_by, is_draft, is_anonymous ;
      |""".trimMargin()

  override val mapper: RowMapper<AddSuggestionResult> = AddSuggestionRowMapper()

  override val paramSetter: ParamSetter<AddSuggestionParams> = AddSuggestionParamSetter()
}

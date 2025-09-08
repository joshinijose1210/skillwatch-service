package suggestions

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class EditSuggestionParams(
  val suggestion: String?,
  val is_draft: Boolean?,
  val is_anonymous: Boolean?,
  val id: Long?
)

class EditSuggestionParamSetter : ParamSetter<EditSuggestionParams> {
  override fun map(ps: PreparedStatement, params: EditSuggestionParams) {
    ps.setObject(1, params.suggestion)
    ps.setObject(2, params.is_draft)
    ps.setObject(3, params.is_anonymous)
    ps.setObject(4, params.id)
  }
}

data class EditSuggestionResult(
  val id: Long,
  val suggestion: String,
  val suggestedBy: Long,
  val isDraft: Boolean,
  val isAnonymous: Boolean
)

class EditSuggestionRowMapper : RowMapper<EditSuggestionResult> {
  override fun map(rs: ResultSet): EditSuggestionResult = EditSuggestionResult(
  id = rs.getObject("id") as kotlin.Long,
    suggestion = rs.getObject("suggestion") as kotlin.String,
    suggestedBy = rs.getObject("suggested_by") as kotlin.Long,
    isDraft = rs.getObject("is_draft") as kotlin.Boolean,
    isAnonymous = rs.getObject("is_anonymous") as kotlin.Boolean)
}

class EditSuggestionQuery : Query<EditSuggestionParams, EditSuggestionResult> {
  override val sql: String = """
      |UPDATE suggestions SET
      |    suggestion = ?,
      |    is_draft = ?,
      |    is_anonymous = ?,
      |    updated_at = CURRENT_TIMESTAMP
      |WHERE id = ? AND is_draft = TRUE
      |RETURNING id, suggestion, suggested_by, is_draft, is_anonymous ;
      |""".trimMargin()

  override val mapper: RowMapper<EditSuggestionResult> = EditSuggestionRowMapper()

  override val paramSetter: ParamSetter<EditSuggestionParams> = EditSuggestionParamSetter()
}

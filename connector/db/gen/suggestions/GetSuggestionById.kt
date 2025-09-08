package suggestions

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetSuggestionByIdParams(
  val id: Long?
)

class GetSuggestionByIdParamSetter : ParamSetter<GetSuggestionByIdParams> {
  override fun map(ps: PreparedStatement, params: GetSuggestionByIdParams) {
    ps.setObject(1, params.id)
  }
}

data class GetSuggestionByIdResult(
  val id: Long,
  val suggestion: String,
  val suggestedBy: Long,
  val isDraft: Boolean,
  val isAnonymous: Boolean
)

class GetSuggestionByIdRowMapper : RowMapper<GetSuggestionByIdResult> {
  override fun map(rs: ResultSet): GetSuggestionByIdResult = GetSuggestionByIdResult(
  id = rs.getObject("id") as kotlin.Long,
    suggestion = rs.getObject("suggestion") as kotlin.String,
    suggestedBy = rs.getObject("suggested_by") as kotlin.Long,
    isDraft = rs.getObject("is_draft") as kotlin.Boolean,
    isAnonymous = rs.getObject("is_anonymous") as kotlin.Boolean)
}

class GetSuggestionByIdQuery : Query<GetSuggestionByIdParams, GetSuggestionByIdResult> {
  override val sql: String = """
      |SELECT
      |    suggestions.id,
      |    suggestions.suggestion,
      |    suggestions.suggested_by,
      |    suggestions.is_draft,
      |    suggestions.is_anonymous
      |FROM suggestions
      |WHERE suggestions.id = ? ;
      |""".trimMargin()

  override val mapper: RowMapper<GetSuggestionByIdResult> = GetSuggestionByIdRowMapper()

  override val paramSetter: ParamSetter<GetSuggestionByIdParams> = GetSuggestionByIdParamSetter()
}

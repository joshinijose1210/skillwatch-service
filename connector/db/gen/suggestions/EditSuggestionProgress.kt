package suggestions

import java.sql.PreparedStatement
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class EditSuggestionProgressParams(
  val progressId: Int?,
  val suggestionId: Long?
)

class EditSuggestionProgressParamSetter : ParamSetter<EditSuggestionProgressParams> {
  override fun map(ps: PreparedStatement, params: EditSuggestionProgressParams) {
    ps.setObject(1, params.progressId)
    ps.setObject(2, params.suggestionId)
  }
}

class EditSuggestionProgressCommand : Command<EditSuggestionProgressParams> {
  override val sql: String = """
      |UPDATE suggestions SET
      |    progress_id = ?
      |WHERE id = ? ;
      |""".trimMargin()

  override val paramSetter: ParamSetter<EditSuggestionProgressParams> =
      EditSuggestionProgressParamSetter()
}

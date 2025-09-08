package suggestions

import java.sql.PreparedStatement
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class AddCommentParams(
  val comment: String?,
  val commented_by: Long?,
  val suggestion_id: Long?,
  val progress_id: Int?
)

class AddCommentParamSetter : ParamSetter<AddCommentParams> {
  override fun map(ps: PreparedStatement, params: AddCommentParams) {
    ps.setObject(1, params.comment)
    ps.setObject(2, params.commented_by)
    ps.setObject(3, params.suggestion_id)
    ps.setObject(4, params.progress_id)
  }
}

class AddCommentCommand : Command<AddCommentParams> {
  override val sql: String = """
      |INSERT INTO suggestion_comments(
      |    comment,
      |    commented_by,
      |    suggestion_id,
      |    progress_id
      |) VALUES(
      |    ?,
      |    ?,
      |    ?,
      |    ?
      |) ;
      |""".trimMargin()

  override val paramSetter: ParamSetter<AddCommentParams> = AddCommentParamSetter()
}

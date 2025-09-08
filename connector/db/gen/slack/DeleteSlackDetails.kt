package slack

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class DeleteSlackDetailsParams(
  val organisationId: Long?
)

class DeleteSlackDetailsParamSetter : ParamSetter<DeleteSlackDetailsParams> {
  override fun map(ps: PreparedStatement, params: DeleteSlackDetailsParams) {
    ps.setObject(1, params.organisationId)
  }
}

class DeleteSlackDetailsCommand : Command<DeleteSlackDetailsParams> {
  override val sql: String = """
      |DELETE FROM
      |    slack_details
      |WHERE
      |    organisation_id = ? ;
      |""".trimMargin()

  override val paramSetter: ParamSetter<DeleteSlackDetailsParams> = DeleteSlackDetailsParamSetter()
}

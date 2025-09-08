package organisations

import java.sql.PreparedStatement
import kotlin.Int
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateOrganisationParams(
  val organisationName: String?,
  val timeZone: String?,
  val id: Int?
)

class UpdateOrganisationParamSetter : ParamSetter<UpdateOrganisationParams> {
  override fun map(ps: PreparedStatement, params: UpdateOrganisationParams) {
    ps.setObject(1, params.organisationName)
    ps.setObject(2, params.timeZone)
    ps.setObject(3, params.id)
  }
}

class UpdateOrganisationCommand : Command<UpdateOrganisationParams> {
  override val sql: String = """
      |UPDATE organisations
      |SET
      |  name = ?,
      |  time_zone = ?
      |WHERE sr_no = ?;
      |""".trimMargin()

  override val paramSetter: ParamSetter<UpdateOrganisationParams> = UpdateOrganisationParamSetter()
}

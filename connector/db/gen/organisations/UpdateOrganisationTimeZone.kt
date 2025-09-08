package organisations

import java.sql.PreparedStatement
import kotlin.Int
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateOrganisationTimeZoneParams(
  val timeZone: String?,
  val organisationId: Int?
)

class UpdateOrganisationTimeZoneParamSetter : ParamSetter<UpdateOrganisationTimeZoneParams> {
  override fun map(ps: PreparedStatement, params: UpdateOrganisationTimeZoneParams) {
    ps.setObject(1, params.timeZone)
    ps.setObject(2, params.organisationId)
  }
}

class UpdateOrganisationTimeZoneCommand : Command<UpdateOrganisationTimeZoneParams> {
  override val sql: String = """
      |UPDATE organisations
      |SET
      |  time_zone = ?
      |WHERE sr_no = ?;
      |""".trimMargin()

  override val paramSetter: ParamSetter<UpdateOrganisationTimeZoneParams> =
      UpdateOrganisationTimeZoneParamSetter()
}

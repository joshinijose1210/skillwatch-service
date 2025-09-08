package employees

import java.sql.PreparedStatement
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateEmployeesParams(
  val onboarding_flow: Boolean?,
  val id: Long?,
  val organisationId: Long?
)

class UpdateEmployeesParamSetter : ParamSetter<UpdateEmployeesParams> {
  override fun map(ps: PreparedStatement, params: UpdateEmployeesParams) {
    ps.setObject(1, params.onboarding_flow)
    ps.setObject(2, params.id)
    ps.setObject(3, params.organisationId)
  }
}

class UpdateEmployeesCommand : Command<UpdateEmployeesParams> {
  override val sql: String = """
      |UPDATE
      |  employees
      |SET
      |  onboarding_flow = ?
      |where
      |  id = ?
      |  AND organisation_id = ? ;
      |""".trimMargin()

  override val paramSetter: ParamSetter<UpdateEmployeesParams> = UpdateEmployeesParamSetter()
}

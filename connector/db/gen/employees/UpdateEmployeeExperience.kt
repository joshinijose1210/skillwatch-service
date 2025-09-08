package employees

import java.sql.PreparedStatement
import kotlin.String
import norm.Command
import norm.ParamSetter

class UpdateEmployeeExperienceParams

class UpdateEmployeeExperienceParamSetter : ParamSetter<UpdateEmployeeExperienceParams> {
  override fun map(ps: PreparedStatement, params: UpdateEmployeeExperienceParams) {
  }
}

class UpdateEmployeeExperienceCommand : Command<UpdateEmployeeExperienceParams> {
  override val sql: String = """
      |UPDATE employees
      |SET experience = experience + 1
      |WHERE status = true;
      |""".trimMargin()

  override val paramSetter: ParamSetter<UpdateEmployeeExperienceParams> =
      UpdateEmployeeExperienceParamSetter()
}

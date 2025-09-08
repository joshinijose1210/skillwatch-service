package organisations

import java.sql.PreparedStatement
import kotlin.Int
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateAdminContactNoParams(
  val contactNo: String?,
  val id: Int?
)

class UpdateAdminContactNoParamSetter : ParamSetter<UpdateAdminContactNoParams> {
  override fun map(ps: PreparedStatement, params: UpdateAdminContactNoParams) {
    ps.setObject(1, params.contactNo)
    ps.setObject(2, params.id)
  }
}

class UpdateAdminContactNoCommand : Command<UpdateAdminContactNoParams> {
  override val sql: String = """
      |UPDATE employees
      |SET contact_no = ?
      |FROM organisations
      |WHERE employees.id = organisations.admin_id
      |AND organisations.sr_no = ?;
      |""".trimMargin()

  override val paramSetter: ParamSetter<UpdateAdminContactNoParams> =
      UpdateAdminContactNoParamSetter()
}

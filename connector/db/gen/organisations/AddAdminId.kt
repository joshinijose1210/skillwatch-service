package organisations

import java.sql.PreparedStatement
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class AddAdminIdParams(
  val adminId: Long?,
  val organisationId: Int?
)

class AddAdminIdParamSetter : ParamSetter<AddAdminIdParams> {
  override fun map(ps: PreparedStatement, params: AddAdminIdParams) {
    ps.setObject(1, params.adminId)
    ps.setObject(2, params.organisationId)
  }
}

class AddAdminIdCommand : Command<AddAdminIdParams> {
  override val sql: String = """
      |UPDATE
      |  organisations
      |SET
      |  admin_id = ?
      |WHERE
      |  sr_no = ?;
      """.trimMargin()

  override val paramSetter: ParamSetter<AddAdminIdParams> = AddAdminIdParamSetter()
}

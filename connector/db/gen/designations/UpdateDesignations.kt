package designations

import java.sql.PreparedStatement
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateDesignationsParams(
  val designationName: String?,
  val status: Boolean?,
  val id: Long?,
  val organisationId: Long?
)

class UpdateDesignationsParamSetter : ParamSetter<UpdateDesignationsParams> {
  override fun map(ps: PreparedStatement, params: UpdateDesignationsParams) {
    ps.setObject(1, params.designationName)
    ps.setObject(2, params.status)
    ps.setObject(3, params.id)
    ps.setObject(4, params.organisationId)
  }
}

class UpdateDesignationsCommand : Command<UpdateDesignationsParams> {
  override val sql: String = """
      |UPDATE
      |  designations
      |SET
      |  designation_name = ?,
      |  status = ?,
      |  updated_at = CURRENT_TIMESTAMP
      |WHERE
      |  id = ?
      |  AND organisation_id = ? ;
      |""".trimMargin()

  override val paramSetter: ParamSetter<UpdateDesignationsParams> = UpdateDesignationsParamSetter()
}

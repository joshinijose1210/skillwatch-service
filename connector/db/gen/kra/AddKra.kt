package kra

import java.sql.PreparedStatement
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class AddKraParams(
  val srNo: Long?,
  val name: String?,
  val weightage: Int?,
  val versionNumber: Int?,
  val organisationId: Long?
)

class AddKraParamSetter : ParamSetter<AddKraParams> {
  override fun map(ps: PreparedStatement, params: AddKraParams) {
    ps.setObject(1, params.srNo)
    ps.setObject(2, params.name)
    ps.setObject(3, params.weightage)
    ps.setObject(4, params.versionNumber)
    ps.setObject(5, params.organisationId)
  }
}

class AddKraCommand : Command<AddKraParams> {
  override val sql: String = """
      |INSERT INTO kra (sr_no, name, weightage, version_number, organisation_id)
      | VALUES (?, ?, ?, ?, ?) ;
      """.trimMargin()

  override val paramSetter: ParamSetter<AddKraParams> = AddKraParamSetter()
}

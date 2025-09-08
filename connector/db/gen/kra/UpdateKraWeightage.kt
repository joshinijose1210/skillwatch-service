package kra

import java.sql.PreparedStatement
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateKraWeightageParams(
  val weightage: Int?,
  val id: Long?
)

class UpdateKraWeightageParamSetter : ParamSetter<UpdateKraWeightageParams> {
  override fun map(ps: PreparedStatement, params: UpdateKraWeightageParams) {
    ps.setObject(1, params.weightage)
    ps.setObject(2, params.id)
  }
}

class UpdateKraWeightageCommand : Command<UpdateKraWeightageParams> {
  override val sql: String = """
      |UPDATE kra SET weightage = ? WHERE id = ? ;
      |""".trimMargin()

  override val paramSetter: ParamSetter<UpdateKraWeightageParams> = UpdateKraWeightageParamSetter()
}

package review

import java.sql.Date
import java.sql.PreparedStatement
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class AddGoalsParams(
  val reviewDetailsId: Long?,
  val description: String?,
  val targetDate: Date?,
  val goalId: Int?,
  val typeId: Int?,
  val assignedTo: Long?,
  val createdBy: Long?,
  val organisationId: Long?
)

class AddGoalsParamSetter : ParamSetter<AddGoalsParams> {
  override fun map(ps: PreparedStatement, params: AddGoalsParams) {
    ps.setObject(1, params.reviewDetailsId)
    ps.setObject(2, params.description)
    ps.setObject(3, params.targetDate)
    ps.setObject(4, params.goalId)
    ps.setObject(5, params.typeId)
    ps.setObject(6, params.assignedTo)
    ps.setObject(7, params.createdBy)
    ps.setObject(8, params.organisationId)
  }
}

class AddGoalsCommand : Command<AddGoalsParams> {
  override val sql: String = """
      |INSERT INTO goals(
      |  review_details_id, description, target_date, goal_id, type_id, assigned_to, created_by, organisation_id
      |)
      |VALUES(
      |   ?, ?, ?, ?, ?, ?, ?, ?
      |);
      |""".trimMargin()

  override val paramSetter: ParamSetter<AddGoalsParams> = AddGoalsParamSetter()
}

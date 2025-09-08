package goals

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class AddGoalParams(
  val description: String?,
  val targetDate: Date?,
  val goalId: Int?,
  val typeId: Int?,
  val assignedTo: Long?,
  val createdBy: Long?,
  val organisationId: Long?
)

class AddGoalParamSetter : ParamSetter<AddGoalParams> {
  override fun map(ps: PreparedStatement, params: AddGoalParams) {
    ps.setObject(1, params.description)
    ps.setObject(2, params.targetDate)
    ps.setObject(3, params.goalId)
    ps.setObject(4, params.typeId)
    ps.setObject(5, params.assignedTo)
    ps.setObject(6, params.createdBy)
    ps.setObject(7, params.organisationId)
  }
}

data class AddGoalResult(
  val id: Long
)

class AddGoalRowMapper : RowMapper<AddGoalResult> {
  override fun map(rs: ResultSet): AddGoalResult = AddGoalResult(
  id = rs.getObject("id") as kotlin.Long)
}

class AddGoalQuery : Query<AddGoalParams, AddGoalResult> {
  override val sql: String = """
      |INSERT INTO goals(
      |  description, target_date, goal_id, type_id, assigned_to, created_by, organisation_id
      |)
      |VALUES(
      |   ?, ?, ?, ?, ?, ?, ?
      |) RETURNING id;
      |""".trimMargin()

  override val mapper: RowMapper<AddGoalResult> = AddGoalRowMapper()

  override val paramSetter: ParamSetter<AddGoalParams> = AddGoalParamSetter()
}

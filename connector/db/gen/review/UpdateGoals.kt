package review

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class UpdateGoalsParams(
  val description: String?,
  val id: Long?
)

class UpdateGoalsParamSetter : ParamSetter<UpdateGoalsParams> {
  override fun map(ps: PreparedStatement, params: UpdateGoalsParams) {
    ps.setObject(1, params.description)
    ps.setObject(2, params.id)
  }
}

data class UpdateGoalsResult(
  val id: Long,
  val createdAt: Timestamp,
  val targetDate: Date,
  val progressId: Int,
  val description: String?,
  val reviewDetailsId: Long?,
  val assignedTo: Long,
  val createdBy: Long,
  val updatedAt: Timestamp?,
  val updatedBy: Long?,
  val typeId: Int,
  val goalId: Int,
  val organisationId: Long
)

class UpdateGoalsRowMapper : RowMapper<UpdateGoalsResult> {
  override fun map(rs: ResultSet): UpdateGoalsResult = UpdateGoalsResult(
  id = rs.getObject("id") as kotlin.Long,
    createdAt = rs.getObject("created_at") as java.sql.Timestamp,
    targetDate = rs.getObject("target_date") as java.sql.Date,
    progressId = rs.getObject("progress_id") as kotlin.Int,
    description = rs.getObject("description") as kotlin.String?,
    reviewDetailsId = rs.getObject("review_details_id") as kotlin.Long?,
    assignedTo = rs.getObject("assigned_to") as kotlin.Long,
    createdBy = rs.getObject("created_by") as kotlin.Long,
    updatedAt = rs.getObject("updated_at") as java.sql.Timestamp?,
    updatedBy = rs.getObject("updated_by") as kotlin.Long?,
    typeId = rs.getObject("type_id") as kotlin.Int,
    goalId = rs.getObject("goal_id") as kotlin.Int,
    organisationId = rs.getObject("organisation_id") as kotlin.Long)
}

class UpdateGoalsQuery : Query<UpdateGoalsParams, UpdateGoalsResult> {
  override val sql: String = """
      |UPDATE
      |goals
      |SET
      |description =?
      |WHERE
      | goals.id = ?
      |RETURNING *;
      |""".trimMargin()

  override val mapper: RowMapper<UpdateGoalsResult> = UpdateGoalsRowMapper()

  override val paramSetter: ParamSetter<UpdateGoalsParams> = UpdateGoalsParamSetter()
}

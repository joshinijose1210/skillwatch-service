package dashboard

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

data class GetGoalsParams(
  val reviewCycleId: Long?,
  val organisationId: Long?,
  val reviewToId: Long?,
  val offset: Int?,
  val limit: Int?
)

class GetGoalsParamSetter : ParamSetter<GetGoalsParams> {
  override fun map(ps: PreparedStatement, params: GetGoalsParams) {
    ps.setObject(1, params.reviewCycleId)
    ps.setObject(2, params.organisationId)
    ps.setObject(3, params.organisationId)
    ps.setObject(4, params.reviewToId)
    ps.setObject(5, params.offset)
    ps.setObject(6, params.limit)
  }
}

data class GetGoalsResult(
  val id: Long,
  val goalId: String?,
  val typeId: Int,
  val description: String?,
  val createdAt: Timestamp,
  val targetDate: Date,
  val progressId: Int,
  val createdBy: Long,
  val assignedTo: Long
)

class GetGoalsRowMapper : RowMapper<GetGoalsResult> {
  override fun map(rs: ResultSet): GetGoalsResult = GetGoalsResult(
  id = rs.getObject("id") as kotlin.Long,
    goalId = rs.getObject("goal_id") as kotlin.String?,
    typeId = rs.getObject("type_id") as kotlin.Int,
    description = rs.getObject("description") as kotlin.String?,
    createdAt = rs.getObject("created_at") as java.sql.Timestamp,
    targetDate = rs.getObject("target_date") as java.sql.Date,
    progressId = rs.getObject("progress_id") as kotlin.Int,
    createdBy = rs.getObject("created_by") as kotlin.Long,
    assignedTo = rs.getObject("assigned_to") as kotlin.Long)
}

class GetGoalsQuery : Query<GetGoalsParams, GetGoalsResult> {
  override val sql: String = """
      |SELECT
      |  g.id,
      |  'G' || g.goal_id AS goal_id,
      |  g.type_id,
      |  g.description,
      |  g.created_at,
      |  g.target_date,
      |  g.progress_id,
      |  g.created_by,
      |  g.assigned_to
      |FROM
      |  goals g
      |  JOIN review_details ON review_details.id = g.review_details_id
      |  JOIN (
      |    SELECT start_date, end_date
      |    FROM review_cycle
      |    WHERE review_cycle.id = ? AND review_cycle.organisation_id = ?
      |  ) AS data ON data.start_date BETWEEN g.created_at AND g.target_date
      |  JOIN review_cycle ON review_cycle.id = review_details.review_cycle_id AND review_cycle.organisation_id = ?
      |WHERE
      |  g.assigned_to = ?
      |OFFSET (?::INT)
      |LIMIT (?::INT) ;
      |""".trimMargin()

  override val mapper: RowMapper<GetGoalsResult> = GetGoalsRowMapper()

  override val paramSetter: ParamSetter<GetGoalsParams> = GetGoalsParamSetter()
}

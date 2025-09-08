package goals

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Array
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAllGoalsParams(
  val organisationId: Long?,
  val assignedTo: Array<Int>?,
  val reviewCycleId: Array<Int>?,
  val progressId: Array<Int>?,
  val typeId: Array<Int>?,
  val offset: Int?,
  val limit: Int?
)

class GetAllGoalsParamSetter : ParamSetter<GetAllGoalsParams> {
  override fun map(ps: PreparedStatement, params: GetAllGoalsParams) {
    ps.setObject(1, params.organisationId)
    ps.setArray(2, ps.connection.createArrayOf("int4", params.assignedTo))
    ps.setArray(3, ps.connection.createArrayOf("int4", params.assignedTo))
    ps.setArray(4, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setArray(5, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setArray(6, ps.connection.createArrayOf("int4", params.progressId))
    ps.setArray(7, ps.connection.createArrayOf("int4", params.progressId))
    ps.setArray(8, ps.connection.createArrayOf("int4", params.typeId))
    ps.setArray(9, ps.connection.createArrayOf("int4", params.typeId))
    ps.setObject(10, params.offset)
    ps.setObject(11, params.limit)
  }
}

data class GetAllGoalsResult(
  val id: Long,
  val description: String?,
  val targetDate: Date,
  val progressId: Int,
  val createdBy: Long,
  val createdByName: String?,
  val assignedTo: Long,
  val assignedToName: String?,
  val typeId: Int,
  val goalId: String?
)

class GetAllGoalsRowMapper : RowMapper<GetAllGoalsResult> {
  override fun map(rs: ResultSet): GetAllGoalsResult = GetAllGoalsResult(
  id = rs.getObject("id") as kotlin.Long,
    description = rs.getObject("description") as kotlin.String?,
    targetDate = rs.getObject("target_date") as java.sql.Date,
    progressId = rs.getObject("progress_id") as kotlin.Int,
    createdBy = rs.getObject("created_by") as kotlin.Long,
    createdByName = rs.getObject("created_by_name") as kotlin.String?,
    assignedTo = rs.getObject("assigned_to") as kotlin.Long,
    assignedToName = rs.getObject("assigned_to_name") as kotlin.String?,
    typeId = rs.getObject("type_id") as kotlin.Int,
    goalId = rs.getObject("goal_id") as kotlin.String?)
}

class GetAllGoalsQuery : Query<GetAllGoalsParams, GetAllGoalsResult> {
  override val sql: String = """
      |SELECT
      |  gl.id,
      |  gl.description,
      |  gl.target_date,
      |  gl.progress_id,
      |  gl.created_by,
      |  CONCAT(cb.first_name, ' ', cb.last_name) AS created_by_name,
      |  gl.assigned_to,
      |  CONCAT(ab.first_name, ' ', ab.last_name) AS assigned_to_name,
      |  gl.type_id,
      |  CONCAT('G', gl.goal_id) AS goal_id
      |FROM
      |  goals gl
      |  LEFT JOIN review_cycle
      |    ON DATE(gl.target_date) BETWEEN review_cycle.start_date AND review_cycle.end_date
      |  LEFT JOIN employees cb ON gl.created_by = cb.id
      |  LEFT JOIN employees ab ON gl.assigned_to = ab.id
      |WHERE
      |  gl.organisation_id = ?
      |  AND (?::INT[] = '{-99}' OR gl.assigned_to = ANY(?::INT[]))
      |  AND (?::INT[] = '{-99}' OR review_cycle.id = ANY(?::INT[]))
      |  AND (?::INT[] = '{-99}' OR gl.progress_id = ANY(?::INT[]))
      |  AND (?::INT[] = '{-99}' OR gl.type_id = ANY(?::INT[]))
      |ORDER BY
      |  gl.goal_id DESC
      |OFFSET (?::INT)
      |LIMIT (?::INT);
      """.trimMargin()

  override val mapper: RowMapper<GetAllGoalsResult> = GetAllGoalsRowMapper()

  override val paramSetter: ParamSetter<GetAllGoalsParams> = GetAllGoalsParamSetter()
}

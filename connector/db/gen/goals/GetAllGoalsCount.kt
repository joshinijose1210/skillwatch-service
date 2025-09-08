package goals

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Array
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAllGoalsCountParams(
  val organisationId: Long?,
  val assignedTo: Array<Int>?,
  val reviewCycleId: Array<Int>?,
  val goalStatus: Array<Int>?,
  val goalType: Array<Int>?
)

class GetAllGoalsCountParamSetter : ParamSetter<GetAllGoalsCountParams> {
  override fun map(ps: PreparedStatement, params: GetAllGoalsCountParams) {
    ps.setObject(1, params.organisationId)
    ps.setArray(2, ps.connection.createArrayOf("int4", params.assignedTo))
    ps.setArray(3, ps.connection.createArrayOf("int4", params.assignedTo))
    ps.setArray(4, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setArray(5, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setArray(6, ps.connection.createArrayOf("int4", params.goalStatus))
    ps.setArray(7, ps.connection.createArrayOf("int4", params.goalStatus))
    ps.setArray(8, ps.connection.createArrayOf("int4", params.goalType))
    ps.setArray(9, ps.connection.createArrayOf("int4", params.goalType))
  }
}

data class GetAllGoalsCountResult(
  val goalCount: Long?
)

class GetAllGoalsCountRowMapper : RowMapper<GetAllGoalsCountResult> {
  override fun map(rs: ResultSet): GetAllGoalsCountResult = GetAllGoalsCountResult(
  goalCount = rs.getObject("goal_count") as kotlin.Long?)
}

class GetAllGoalsCountQuery : Query<GetAllGoalsCountParams, GetAllGoalsCountResult> {
  override val sql: String = """
      |SELECT
      |  COUNT(*) AS goal_count
      |FROM
      |  goals gl
      |  LEFT JOIN review_cycle
      |    ON DATE(gl.target_date) BETWEEN review_cycle.start_date AND review_cycle.end_date
      |WHERE
      |  gl.organisation_id = ?
      |  AND (?::INT[] = '{-99}' OR gl.assigned_to = ANY(?::INT[]))
      |  AND (?::INT[] = '{-99}' OR review_cycle.id = ANY(?::INT[]))
      |  AND (?::INT[] = '{-99}' OR gl.progress_id = ANY(?::INT[]))
      |  AND (?::INT[] = '{-99}' OR gl.type_id = ANY(?::INT[]));
      """.trimMargin()

  override val mapper: RowMapper<GetAllGoalsCountResult> = GetAllGoalsCountRowMapper()

  override val paramSetter: ParamSetter<GetAllGoalsCountParams> = GetAllGoalsCountParamSetter()
}

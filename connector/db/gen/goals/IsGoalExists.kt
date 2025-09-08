package goals

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class IsGoalExistsParams(
  val id: Long?
)

class IsGoalExistsParamSetter : ParamSetter<IsGoalExistsParams> {
  override fun map(ps: PreparedStatement, params: IsGoalExistsParams) {
    ps.setObject(1, params.id)
  }
}

data class IsGoalExistsResult(
  val exists: Boolean?
)

class IsGoalExistsRowMapper : RowMapper<IsGoalExistsResult> {
  override fun map(rs: ResultSet): IsGoalExistsResult = IsGoalExistsResult(
  exists = rs.getObject("exists") as kotlin.Boolean?)
}

class IsGoalExistsQuery : Query<IsGoalExistsParams, IsGoalExistsResult> {
  override val sql: String = """
      |SELECT EXISTS (
      |  SELECT 1 FROM goals WHERE
      |      id = ?
      |);
      |""".trimMargin()

  override val mapper: RowMapper<IsGoalExistsResult> = IsGoalExistsRowMapper()

  override val paramSetter: ParamSetter<IsGoalExistsParams> = IsGoalExistsParamSetter()
}

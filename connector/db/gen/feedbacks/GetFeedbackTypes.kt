package feedbacks

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Int
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

class GetFeedbackTypesParams

class GetFeedbackTypesParamSetter : ParamSetter<GetFeedbackTypesParams> {
  override fun map(ps: PreparedStatement, params: GetFeedbackTypesParams) {
  }
}

data class GetFeedbackTypesResult(
  val id: Int,
  val name: String
)

class GetFeedbackTypesRowMapper : RowMapper<GetFeedbackTypesResult> {
  override fun map(rs: ResultSet): GetFeedbackTypesResult = GetFeedbackTypesResult(
  id = rs.getObject("id") as kotlin.Int,
    name = rs.getObject("name") as kotlin.String)
}

class GetFeedbackTypesQuery : Query<GetFeedbackTypesParams, GetFeedbackTypesResult> {
  override val sql: String = """
      |SELECT
      |  id,
      |  name
      |FROM
      |  feedback_types
      |ORDER BY id ;
      |""".trimMargin()

  override val mapper: RowMapper<GetFeedbackTypesResult> = GetFeedbackTypesRowMapper()

  override val paramSetter: ParamSetter<GetFeedbackTypesParams> = GetFeedbackTypesParamSetter()
}

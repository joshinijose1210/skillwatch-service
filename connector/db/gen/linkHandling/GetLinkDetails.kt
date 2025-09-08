package linkHandling

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Int
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetLinkDetailsParams(
  val linkId: String?
)

class GetLinkDetailsParamSetter : ParamSetter<GetLinkDetailsParams> {
  override fun map(ps: PreparedStatement, params: GetLinkDetailsParams) {
    ps.setObject(1, params.linkId)
  }
}

data class GetLinkDetailsResult(
  val id: String,
  val generationTime: Timestamp,
  val noOfHit: Int,
  val purpose: String
)

class GetLinkDetailsRowMapper : RowMapper<GetLinkDetailsResult> {
  override fun map(rs: ResultSet): GetLinkDetailsResult = GetLinkDetailsResult(
  id = rs.getObject("id") as kotlin.String,
    generationTime = rs.getObject("generation_time") as java.sql.Timestamp,
    noOfHit = rs.getObject("no_of_hit") as kotlin.Int,
    purpose = rs.getObject("purpose") as kotlin.String)
}

class GetLinkDetailsQuery : Query<GetLinkDetailsParams, GetLinkDetailsResult> {
  override val sql: String = """
      |SELECT
      |  la.id,
      |  la.generation_time,
      |  la.no_of_hit,
      |  la.purpose
      |FROM
      |  link_details la
      |WHERE
      |  la.id = ?
      |""".trimMargin()

  override val mapper: RowMapper<GetLinkDetailsResult> = GetLinkDetailsRowMapper()

  override val paramSetter: ParamSetter<GetLinkDetailsParams> = GetLinkDetailsParamSetter()
}

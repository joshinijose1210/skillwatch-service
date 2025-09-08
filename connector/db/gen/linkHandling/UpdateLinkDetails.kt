package linkHandling

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Int
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class UpdateLinkDetailsParams(
  val noOfHit: Int?,
  val linkId: String?
)

class UpdateLinkDetailsParamSetter : ParamSetter<UpdateLinkDetailsParams> {
  override fun map(ps: PreparedStatement, params: UpdateLinkDetailsParams) {
    ps.setObject(1, params.noOfHit)
    ps.setObject(2, params.linkId)
  }
}

data class UpdateLinkDetailsResult(
  val id: String,
  val generationTime: Timestamp,
  val noOfHit: Int,
  val purpose: String
)

class UpdateLinkDetailsRowMapper : RowMapper<UpdateLinkDetailsResult> {
  override fun map(rs: ResultSet): UpdateLinkDetailsResult = UpdateLinkDetailsResult(
  id = rs.getObject("id") as kotlin.String,
    generationTime = rs.getObject("generation_time") as java.sql.Timestamp,
    noOfHit = rs.getObject("no_of_hit") as kotlin.Int,
    purpose = rs.getObject("purpose") as kotlin.String)
}

class UpdateLinkDetailsQuery : Query<UpdateLinkDetailsParams, UpdateLinkDetailsResult> {
  override val sql: String = """
      |UPDATE
      |  link_details
      |SET
      |  no_of_hit = ?
      |WHERE
      |  link_details.id = ?
      |  RETURNING * ;
      |""".trimMargin()

  override val mapper: RowMapper<UpdateLinkDetailsResult> = UpdateLinkDetailsRowMapper()

  override val paramSetter: ParamSetter<UpdateLinkDetailsParams> = UpdateLinkDetailsParamSetter()
}

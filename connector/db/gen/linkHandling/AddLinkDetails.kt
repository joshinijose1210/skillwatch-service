package linkHandling

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Int
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class AddLinkDetailsParams(
  val linkId: String?,
  val generationTime: Timestamp?,
  val noOfHit: Int?,
  val purpose: String?
)

class AddLinkDetailsParamSetter : ParamSetter<AddLinkDetailsParams> {
  override fun map(ps: PreparedStatement, params: AddLinkDetailsParams) {
    ps.setObject(1, params.linkId)
    ps.setObject(2, params.generationTime)
    ps.setObject(3, params.noOfHit)
    ps.setObject(4, params.purpose)
  }
}

data class AddLinkDetailsResult(
  val id: String,
  val generationTime: Timestamp,
  val noOfHit: Int,
  val purpose: String
)

class AddLinkDetailsRowMapper : RowMapper<AddLinkDetailsResult> {
  override fun map(rs: ResultSet): AddLinkDetailsResult = AddLinkDetailsResult(
  id = rs.getObject("id") as kotlin.String,
    generationTime = rs.getObject("generation_time") as java.sql.Timestamp,
    noOfHit = rs.getObject("no_of_hit") as kotlin.Int,
    purpose = rs.getObject("purpose") as kotlin.String)
}

class AddLinkDetailsQuery : Query<AddLinkDetailsParams, AddLinkDetailsResult> {
  override val sql: String = """
      |INSERT INTO link_details(id, generation_time, no_of_hit, purpose)
      | VALUES (?, ?, ?, ?) RETURNING *;
      |""".trimMargin()

  override val mapper: RowMapper<AddLinkDetailsResult> = AddLinkDetailsRowMapper()

  override val paramSetter: ParamSetter<AddLinkDetailsParams> = AddLinkDetailsParamSetter()
}

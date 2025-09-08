package kra

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetWeightageByIdParams(
  val kraId: Long?,
  val organisationId: Long?
)

class GetWeightageByIdParamSetter : ParamSetter<GetWeightageByIdParams> {
  override fun map(ps: PreparedStatement, params: GetWeightageByIdParams) {
    ps.setObject(1, params.kraId)
    ps.setObject(2, params.organisationId)
  }
}

data class GetWeightageByIdResult(
  val id: Long,
  val weightage: Int
)

class GetWeightageByIdRowMapper : RowMapper<GetWeightageByIdResult> {
  override fun map(rs: ResultSet): GetWeightageByIdResult = GetWeightageByIdResult(
  id = rs.getObject("id") as kotlin.Long,
    weightage = rs.getObject("weightage") as kotlin.Int)
}

class GetWeightageByIdQuery : Query<GetWeightageByIdParams, GetWeightageByIdResult> {
  override val sql: String = """
      |SELECT kra.id, kra.weightage FROM kra
      |    WHERE id = ? AND
      |    organisation_id = ? ;
      |""".trimMargin()

  override val mapper: RowMapper<GetWeightageByIdResult> = GetWeightageByIdRowMapper()

  override val paramSetter: ParamSetter<GetWeightageByIdParams> = GetWeightageByIdParamSetter()
}

package kpi

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class AddNewKpiParams(
  val organisationId: Long?,
  val kpiId: Long?,
  val kpiTitle: String?,
  val kpiDescription: String?,
  val kpiStatus: Boolean?
)

class AddNewKpiParamSetter : ParamSetter<AddNewKpiParams> {
  override fun map(ps: PreparedStatement, params: AddNewKpiParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.kpiId)
    ps.setObject(3, params.kpiTitle)
    ps.setObject(4, params.kpiDescription)
    ps.setObject(5, params.kpiStatus)
  }
}

data class AddNewKpiResult(
  val id: Long,
  val title: String,
  val description: String,
  val status: Boolean,
  val kpiId: Long,
  val organisationId: Long
)

class AddNewKpiRowMapper : RowMapper<AddNewKpiResult> {
  override fun map(rs: ResultSet): AddNewKpiResult = AddNewKpiResult(
  id = rs.getObject("id") as kotlin.Long,
    title = rs.getObject("title") as kotlin.String,
    description = rs.getObject("description") as kotlin.String,
    status = rs.getObject("status") as kotlin.Boolean,
    kpiId = rs.getObject("kpi_id") as kotlin.Long,
    organisationId = rs.getObject("organisation_id") as kotlin.Long)
}

class AddNewKpiQuery : Query<AddNewKpiParams, AddNewKpiResult> {
  override val sql: String = """
      |INSERT INTO kpi(organisation_id, kpi_id, title, description, status)
      | VALUES (?, ?, ?, ?, ?) RETURNING *;
      |""".trimMargin()

  override val mapper: RowMapper<AddNewKpiResult> = AddNewKpiRowMapper()

  override val paramSetter: ParamSetter<AddNewKpiParams> = AddNewKpiParamSetter()
}

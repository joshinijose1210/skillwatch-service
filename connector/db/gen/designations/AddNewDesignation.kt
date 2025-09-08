package designations

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class AddNewDesignationParams(
  val designationId: Long?,
  val organisationId: Long?,
  val designationName: String?,
  val status: Boolean?
)

class AddNewDesignationParamSetter : ParamSetter<AddNewDesignationParams> {
  override fun map(ps: PreparedStatement, params: AddNewDesignationParams) {
    ps.setObject(1, params.designationId)
    ps.setObject(2, params.organisationId)
    ps.setObject(3, params.designationName)
    ps.setObject(4, params.status)
  }
}

data class AddNewDesignationResult(
  val id: Long
)

class AddNewDesignationRowMapper : RowMapper<AddNewDesignationResult> {
  override fun map(rs: ResultSet): AddNewDesignationResult = AddNewDesignationResult(
  id = rs.getObject("id") as kotlin.Long)
}

class AddNewDesignationQuery : Query<AddNewDesignationParams, AddNewDesignationResult> {
  override val sql: String = """
      |INSERT INTO designations(designation_id, organisation_id, designation_name, status)
      |VALUES
      |(?, ?, ?,?) RETURNING id;
      """.trimMargin()

  override val mapper: RowMapper<AddNewDesignationResult> = AddNewDesignationRowMapper()

  override val paramSetter: ParamSetter<AddNewDesignationParams> = AddNewDesignationParamSetter()
}

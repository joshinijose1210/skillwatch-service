package reviewCycle

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class UpdateReviewCycleParams(
  val start_date: Date?,
  val end_date: Date?,
  val publish: Boolean?,
  val self_review_start_date: Date?,
  val self_review_end_date: Date?,
  val manager_review_start_date: Date?,
  val manager_review_end_date: Date?,
  val check_in_start_date: Date?,
  val check_in_end_date: Date?,
  val organisation_id: Long?,
  val id: Long?
)

class UpdateReviewCycleParamSetter : ParamSetter<UpdateReviewCycleParams> {
  override fun map(ps: PreparedStatement, params: UpdateReviewCycleParams) {
    ps.setObject(1, params.start_date)
    ps.setObject(2, params.end_date)
    ps.setObject(3, params.publish)
    ps.setObject(4, params.self_review_start_date)
    ps.setObject(5, params.self_review_end_date)
    ps.setObject(6, params.manager_review_start_date)
    ps.setObject(7, params.manager_review_end_date)
    ps.setObject(8, params.check_in_start_date)
    ps.setObject(9, params.check_in_end_date)
    ps.setObject(10, params.organisation_id)
    ps.setObject(11, params.id)
  }
}

data class UpdateReviewCycleResult(
  val id: Long,
  val startDate: Date,
  val endDate: Date,
  val publish: Boolean,
  val lastModified: Timestamp,
  val selfReviewStartDate: Date,
  val selfReviewEndDate: Date,
  val managerReviewStartDate: Date,
  val managerReviewEndDate: Date,
  val checkInStartDate: Date,
  val checkInEndDate: Date,
  val organisationId: Long
)

class UpdateReviewCycleRowMapper : RowMapper<UpdateReviewCycleResult> {
  override fun map(rs: ResultSet): UpdateReviewCycleResult = UpdateReviewCycleResult(
  id = rs.getObject("id") as kotlin.Long,
    startDate = rs.getObject("start_date") as java.sql.Date,
    endDate = rs.getObject("end_date") as java.sql.Date,
    publish = rs.getObject("publish") as kotlin.Boolean,
    lastModified = rs.getObject("last_modified") as java.sql.Timestamp,
    selfReviewStartDate = rs.getObject("self_review_start_date") as java.sql.Date,
    selfReviewEndDate = rs.getObject("self_review_end_date") as java.sql.Date,
    managerReviewStartDate = rs.getObject("manager_review_start_date") as java.sql.Date,
    managerReviewEndDate = rs.getObject("manager_review_end_date") as java.sql.Date,
    checkInStartDate = rs.getObject("check_in_start_date") as java.sql.Date,
    checkInEndDate = rs.getObject("check_in_end_date") as java.sql.Date,
    organisationId = rs.getObject("organisation_id") as kotlin.Long)
}

class UpdateReviewCycleQuery : Query<UpdateReviewCycleParams, UpdateReviewCycleResult> {
  override val sql: String = """
      |UPDATE
      |review_cycle
      |SET
      |start_date = ?,
      |end_date = ?,
      |publish = ?,
      |last_modified = CURRENT_TIMESTAMP,
      |self_review_start_date = ?,
      |self_review_end_date = ?,
      |manager_review_start_date = ?,
      |manager_review_end_date = ?,
      |check_in_start_date = ?,
      |check_in_end_date = ?
      |WHERE
      |organisation_id = ?
      |AND id = ? RETURNING *;
      """.trimMargin()

  override val mapper: RowMapper<UpdateReviewCycleResult> = UpdateReviewCycleRowMapper()

  override val paramSetter: ParamSetter<UpdateReviewCycleParams> = UpdateReviewCycleParamSetter()
}

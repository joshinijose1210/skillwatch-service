package reviewCycle

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAllReviewCyclesParams(
  val organisationId: Long?,
  val offset: Int?,
  val limit: Int?
)

class GetAllReviewCyclesParamSetter : ParamSetter<GetAllReviewCyclesParams> {
  override fun map(ps: PreparedStatement, params: GetAllReviewCyclesParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.offset)
    ps.setObject(3, params.limit)
  }
}

data class GetAllReviewCyclesResult(
  val organisationId: Long,
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
  val checkInEndDate: Date
)

class GetAllReviewCyclesRowMapper : RowMapper<GetAllReviewCyclesResult> {
  override fun map(rs: ResultSet): GetAllReviewCyclesResult = GetAllReviewCyclesResult(
  organisationId = rs.getObject("organisation_id") as kotlin.Long,
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
    checkInEndDate = rs.getObject("check_in_end_date") as java.sql.Date)
}

class GetAllReviewCyclesQuery : Query<GetAllReviewCyclesParams, GetAllReviewCyclesResult> {
  override val sql: String = """
      |SELECT
      |review_cycle.organisation_id,
      |review_cycle.id,
      |review_cycle.start_date,
      |review_cycle.end_date,
      |review_cycle.publish,
      |review_cycle.last_modified,
      |review_cycle.self_review_start_date,
      |review_cycle.self_review_end_date,
      |review_cycle.manager_review_start_date,
      |review_cycle.manager_review_end_date,
      |review_cycle.check_in_start_date,
      |review_cycle.check_in_end_date
      |FROM
      |review_cycle
      |WHERE
      |review_cycle.organisation_id = ?
      |ORDER BY
      |  review_cycle.publish DESC,
      |  (daterange(review_cycle.start_date, review_cycle.end_date)) DESC
      |OFFSET (?::INT)
      |LIMIT (?::INT);
      """.trimMargin()

  override val mapper: RowMapper<GetAllReviewCyclesResult> = GetAllReviewCyclesRowMapper()

  override val paramSetter: ParamSetter<GetAllReviewCyclesParams> = GetAllReviewCyclesParamSetter()
}

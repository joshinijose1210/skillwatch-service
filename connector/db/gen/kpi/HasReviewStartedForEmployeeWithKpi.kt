package kpi

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class HasReviewStartedForEmployeeWithKpiParams(
  val reviewCycleId: Long?,
  val kpiId: Long?
)

class HasReviewStartedForEmployeeWithKpiParamSetter :
    ParamSetter<HasReviewStartedForEmployeeWithKpiParams> {
  override fun map(ps: PreparedStatement, params: HasReviewStartedForEmployeeWithKpiParams) {
    ps.setObject(1, params.reviewCycleId)
    ps.setObject(2, params.kpiId)
  }
}

data class HasReviewStartedForEmployeeWithKpiResult(
  val hasStartedReview: Boolean?
)

class HasReviewStartedForEmployeeWithKpiRowMapper :
    RowMapper<HasReviewStartedForEmployeeWithKpiResult> {
  override fun map(rs: ResultSet): HasReviewStartedForEmployeeWithKpiResult =
      HasReviewStartedForEmployeeWithKpiResult(
  hasStartedReview = rs.getObject("has_started_review") as kotlin.Boolean?)
}

class HasReviewStartedForEmployeeWithKpiQuery : Query<HasReviewStartedForEmployeeWithKpiParams,
    HasReviewStartedForEmployeeWithKpiResult> {
  override val sql: String = """
      |-- This query checks whether any employee assigned a given KPI
      |-- has started their review for the specified review cycle.
      |SELECT EXISTS (
      |    SELECT 1
      |    FROM kpi_department_team_designation_mapping kdtdm
      |    JOIN employees_designation_mapping edm ON kdtdm.designation_id = edm.designation_id
      |    JOIN review_details rd ON edm.emp_id = rd.review_to
      |    WHERE rd.review_cycle_id = ?
      |    AND kdtdm.kpi_id = ?
      |) AS has_started_review;
      |""".trimMargin()

  override val mapper: RowMapper<HasReviewStartedForEmployeeWithKpiResult> =
      HasReviewStartedForEmployeeWithKpiRowMapper()

  override val paramSetter: ParamSetter<HasReviewStartedForEmployeeWithKpiParams> =
      HasReviewStartedForEmployeeWithKpiParamSetter()
}

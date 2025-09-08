package kra

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class AddKraByReviewCycleParams(
  val reviewCycleId: Long?,
  val organisationId: Long?
)

class AddKraByReviewCycleParamSetter : ParamSetter<AddKraByReviewCycleParams> {
  override fun map(ps: PreparedStatement, params: AddKraByReviewCycleParams) {
    ps.setObject(1, params.reviewCycleId)
    ps.setObject(2, params.organisationId)
    ps.setObject(3, params.reviewCycleId)
  }
}

class AddKraByReviewCycleCommand : Command<AddKraByReviewCycleParams> {
  override val sql: String = """
      |INSERT INTO review_cycle_kra (review_cycle_id, kra_id, kra_name, kra_weightage)
      |SELECT
      |    ? AS review_cycle_id,
      |    k.id AS kra_id,
      |    k.name AS kra_name,
      |    k.weightage AS kra_weightage
      |FROM
      |    kra k
      |WHERE
      |    k.organisation_id = ? AND
      |    NOT EXISTS (
      |        SELECT 1
      |        FROM review_cycle_kra rck
      |        WHERE rck.review_cycle_id = ?
      |    );
      |""".trimMargin()

  override val paramSetter: ParamSetter<AddKraByReviewCycleParams> =
      AddKraByReviewCycleParamSetter()
}

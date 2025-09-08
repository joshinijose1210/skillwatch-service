package scalereal.core.kra

import scalereal.core.models.domain.GetAllKRAResponse
import scalereal.core.models.domain.KRAData
import scalereal.core.models.domain.KRAWeightage
import scalereal.core.models.domain.UpdateKRAWeightageRequest

interface KRARepository {
    fun isKRAExists(
        organisationId: Long,
        kra: String,
    ): Boolean

    fun getMaxSrNo(organisationId: Long): Long

    fun createKRA(kraData: KRAData)

    fun getAllKRAs(organisationId: Long): List<GetAllKRAResponse>

    fun updateKRAWeightage(updateKRAWeightageRequest: List<UpdateKRAWeightageRequest>)

    fun getKRAId(
        organisationId: Long,
        kra: String,
    ): Long?

    fun getWeightageByIds(
        kraIds: List<Long?>,
        organisationId: Long,
    ): List<KRAWeightage>

    fun addKraByReviewCycle(
        reviewCycleId: Long,
        organisationId: Long,
    )

    fun getKraByReviewCycle(reviewCycleId: Long): List<GetAllKRAResponse>

    fun doAllKRAsHaveActiveKPIs(organisationId: Long): Boolean
}

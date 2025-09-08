package scalereal.core.kra

import jakarta.inject.Singleton
import scalereal.core.exception.InvalidDataException
import scalereal.core.models.domain.GetAllKRAResponse
import scalereal.core.models.domain.KRAData
import scalereal.core.models.domain.UpdateKRAWeightageRequest
import scalereal.core.models.domain.UserActivityData
import scalereal.core.modules.ModuleService
import scalereal.core.modules.Modules
import scalereal.core.organisations.OrganisationRepository
import scalereal.core.reviewCycle.ReviewCycleRepository
import scalereal.core.userActivity.UserActivityRepository

@Singleton
class KRAService(
    private val kraRepository: KRARepository,
    private val reviewCycleRepository: ReviewCycleRepository,
    private val userActivityRepository: UserActivityRepository,
    moduleService: ModuleService,
    private val organisationRepository: OrganisationRepository,
) {
    private val kpiModuleId = moduleService.fetchModuleId(Modules.KRAs.moduleName)

    fun getAllKRAs(organisationId: Long): List<GetAllKRAResponse> = kraRepository.getAllKRAs(organisationId)

    fun createDefaultKRAs(organisationId: Long) {
        val kras: List<String> = listOf(KRAs.SKILL_NAME, KRAs.RESULTS_NAME, KRAs.ATTITUDE_FITMENT_NAME)
        kras.map { kra ->
            if (!kraRepository.isKRAExists(organisationId, kra)) {
                val maxSrNo = kraRepository.getMaxSrNo(organisationId)
                when (kra) {
                    KRAs.SKILL_NAME -> {
                        kraRepository.createKRA(
                            KRAData(
                                srNo = maxSrNo + 1,
                                name = kra,
                                weightage = KRAs.SKILL_WEIGHTAGE,
                                versionNumber = 1,
                                organisationId = organisationId,
                            ),
                        )
                    }
                    KRAs.RESULTS_NAME -> {
                        kraRepository.createKRA(
                            KRAData(
                                srNo = maxSrNo + 1,
                                name = kra,
                                weightage = KRAs.RESULTS_WEIGHTAGE,
                                versionNumber = 1,
                                organisationId = organisationId,
                            ),
                        )
                    }
                    KRAs.ATTITUDE_FITMENT_NAME -> {
                        kraRepository.createKRA(
                            KRAData(
                                srNo = maxSrNo + 1,
                                name = kra,
                                weightage = KRAs.ATTITUDE_FITMENT_WEIGHTAGE,
                                versionNumber = 1,
                                organisationId = organisationId,
                            ),
                        )
                    }
                }
            }
        }
    }

    fun updateKRAWeightage(
        organisationId: Long,
        updateKRAWeightageRequest: List<UpdateKRAWeightageRequest>,
        userActivityData: UserActivityData,
    ) {
        val organisationTimeZone = organisationRepository.getOrganisationDetails(organisationId).timeZone
        val activeReviewCycle = reviewCycleRepository.fetchActiveReviewCycle(organisationId)?.withActiveFlags(organisationTimeZone)

        if (activeReviewCycle != null && activeReviewCycle.isSelfReviewActive) {
            throw Exception("You cannot edit KRA weightage after Self Review Timeline starts")
        } else {
            val existingKRAs = kraRepository.getAllKRAs(organisationId)
            val existingKRAIds = existingKRAs.map { it.id }.toSet()
            val updateKRAIds = updateKRAWeightageRequest.map { it.id }

            if (!existingKRAIds.containsAll(updateKRAIds)) {
                throw InvalidDataException("One or more KRAs not found")
            }
            if (updateKRAWeightageRequest.any { it.weightage < 1 }) {
                throw InvalidDataException("Each KRA should have at least 1% weightage.")
            }

            if (updateKRAWeightageRequest.sumOf { it.weightage } != 100) {
                throw InvalidDataException("The total weightage of all KRAs must be 100")
            }

            val updatedKRAs =
                existingKRAs.filter { existingKRA ->
                    existingKRA.weightage != updateKRAWeightageRequest.find { it.id == existingKRA.id }?.weightage
                }

            if (updatedKRAs.isNotEmpty()) {
                kraRepository.updateKRAWeightage(updateKRAWeightageRequest)
                updateActivityLog(updatedKRAs, userActivityData)
            }
        }
    }

    private fun updateActivityLog(
        updatedKRAs: List<GetAllKRAResponse>,
        userActivityData: UserActivityData,
    ) {
        updatedKRAs.map { kra ->
            val activity = "${kra.kraId} Edited"
            userActivityRepository.addActivity(
                actionBy = userActivityData.actionBy,
                moduleId = kpiModuleId,
                activity = activity,
                description = activity,
                ipAddress = userActivityData.ipAddress,
            )
        }
    }
}

package scalereal.db.kra

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kra.AddKraByReviewCycleCommand
import kra.AddKraByReviewCycleParams
import kra.AddKraCommand
import kra.AddKraParams
import kra.GetAllKraParams
import kra.GetAllKraQuery
import kra.GetKraByReviewCycleParams
import kra.GetKraByReviewCycleQuery
import kra.GetKraIdParams
import kra.GetKraIdQuery
import kra.GetMaxSrNoParams
import kra.GetMaxSrNoQuery
import kra.GetWeightageByIdParams
import kra.GetWeightageByIdQuery
import kra.HasAllKrasActiveKpisParams
import kra.HasAllKrasActiveKpisQuery
import kra.IsKraExistsParams
import kra.IsKraExistsQuery
import kra.UpdateKraWeightageCommand
import kra.UpdateKraWeightageParams
import norm.command
import norm.query
import scalereal.core.kra.KRARepository
import scalereal.core.models.domain.GetAllKRAResponse
import scalereal.core.models.domain.KRAData
import scalereal.core.models.domain.KRAWeightage
import scalereal.core.models.domain.UpdateKRAWeightageRequest
import javax.sql.DataSource

@Singleton
class KRARepositoryImpl(
    @Inject private val dataSource: DataSource,
) : KRARepository {
    override fun isKRAExists(
        organisationId: Long,
        kra: String,
    ): Boolean =
        dataSource.connection.use { connection ->
            IsKraExistsQuery()
                .query(connection, IsKraExistsParams(organisationId = organisationId, kra = kra))
                .map { requireNotNull(it.exists) }
                .first()
        }

    override fun getMaxSrNo(organisationId: Long): Long {
        dataSource.connection.use { connection ->
            val maxSrNo =
                GetMaxSrNoQuery()
                    .query(
                        connection,
                        GetMaxSrNoParams(organisationId = organisationId),
                    ).map { it.maxSrNo }
            return maxSrNo.firstOrNull() ?: 0
        }
    }

    override fun createKRA(kraData: KRAData) {
        dataSource.connection.use { connection ->
            AddKraCommand()
                .command(
                    connection,
                    AddKraParams(
                        srNo = kraData.srNo,
                        name = kraData.name,
                        weightage = kraData.weightage,
                        versionNumber = kraData.versionNumber,
                        organisationId = kraData.organisationId,
                    ),
                )
        }
    }

    override fun getAllKRAs(organisationId: Long): List<GetAllKRAResponse> =
        dataSource.connection.use { connection ->
            GetAllKraQuery()
                .query(
                    connection,
                    GetAllKraParams(
                        organisationId = organisationId,
                    ),
                ).map {
                    GetAllKRAResponse(
                        id = it.id,
                        kraId = GetAllKRAResponse.getKRADisplayId(it.srNo),
                        name = it.name,
                        weightage = it.weightage,
                        organisationId = it.organisationId,
                    )
                }
        }

    override fun updateKRAWeightage(updateKRAWeightageRequest: List<UpdateKRAWeightageRequest>) {
        updateKRAWeightageRequest.map { kraWeightage ->
            dataSource.connection.use { connection ->
                UpdateKraWeightageCommand()
                    .command(
                        connection,
                        UpdateKraWeightageParams(
                            id = kraWeightage.id,
                            weightage = kraWeightage.weightage,
                        ),
                    )
            }
        }
    }

    override fun getKRAId(
        organisationId: Long,
        kra: String,
    ): Long? =
        dataSource.connection.use { connection ->
            GetKraIdQuery()
                .query(connection, GetKraIdParams(organisationId = organisationId, kraName = kra))
                .map { it.id }
                .firstOrNull()
        }

    override fun getWeightageByIds(
        kraIds: List<Long?>,
        organisationId: Long,
    ): List<KRAWeightage> =
        dataSource.connection.use { connection ->
            kraIds.mapNotNull { kraId ->
                GetWeightageByIdQuery()
                    .query(connection, GetWeightageByIdParams(organisationId = organisationId, kraId = kraId))
                    .map { KRAWeightage(id = it.id, weightage = it.weightage) }
                    .firstOrNull()
            }
        }

    override fun addKraByReviewCycle(
        reviewCycleId: Long,
        organisationId: Long,
    ) {
        dataSource.connection.use { connection ->
            AddKraByReviewCycleCommand()
                .command(
                    connection,
                    AddKraByReviewCycleParams(
                        reviewCycleId = reviewCycleId,
                        organisationId = organisationId,
                    ),
                )
        }
    }

    override fun getKraByReviewCycle(reviewCycleId: Long): List<GetAllKRAResponse> =
        dataSource.connection.use { connection ->
            GetKraByReviewCycleQuery()
                .query(connection, GetKraByReviewCycleParams(reviewCycleId = reviewCycleId))
                .map {
                    GetAllKRAResponse(
                        id = it.id,
                        kraId = GetAllKRAResponse.getKRADisplayId(it.srNo),
                        name = it.kraName,
                        weightage = it.kraWeightage,
                        organisationId = it.organisationId,
                    )
                }
        }

    override fun doAllKRAsHaveActiveKPIs(organisationId: Long): Boolean =
        dataSource.connection.use { connection ->
            HasAllKrasActiveKpisQuery()
                .query(connection, HasAllKrasActiveKpisParams(organisationId))
                .firstOrNull()
                ?.allKrasHasActiveKpis ?: false
        }
}

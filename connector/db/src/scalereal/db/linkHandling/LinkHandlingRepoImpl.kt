package scalereal.db.linkHandling

import jakarta.inject.Inject
import jakarta.inject.Singleton
import linkHandling.AddLinkDetailsParams
import linkHandling.AddLinkDetailsQuery
import linkHandling.GetLinkDetailsParams
import linkHandling.GetLinkDetailsQuery
import linkHandling.UpdateLinkDetailsParams
import linkHandling.UpdateLinkDetailsQuery
import norm.query
import scalereal.core.linkHandling.LinkDetails
import scalereal.core.linkHandling.LinkHandlingRepo
import javax.sql.DataSource

@Singleton
class LinkHandlingRepoImpl(
    @Inject private val dataSource: DataSource,
) : LinkHandlingRepo {
    override fun insertLinkDetails(linkDetails: LinkDetails): LinkDetails =
        dataSource.connection.use { connection ->
            AddLinkDetailsQuery()
                .query(
                    connection,
                    AddLinkDetailsParams(
                        linkId = linkDetails.linkId,
                        generationTime = linkDetails.generationTime,
                        noOfHit = linkDetails.noOfHit,
                        purpose = linkDetails.purposeOfLink,
                    ),
                ).map { LinkDetails(it.id, it.generationTime, it.noOfHit, it.purpose) }
                .first()
        }

    override fun fetchLinkDetails(linkId: String): LinkDetails =
        dataSource.connection.use { connection ->
            GetLinkDetailsQuery()
                .query(
                    connection,
                    GetLinkDetailsParams(
                        linkId = linkId,
                    ),
                ).map {
                    LinkDetails(it.id, it.generationTime, it.noOfHit, it.purpose)
                }.first()
        }

    override fun updateLinkDetails(
        noOfHit: Int,
        linkId: String,
    ): LinkDetails =
        dataSource.connection.use { connection ->
            UpdateLinkDetailsQuery()
                .query(
                    connection,
                    UpdateLinkDetailsParams(noOfHit = noOfHit, linkId = linkId),
                ).map {
                    LinkDetails(it.id, it.generationTime, it.noOfHit, it.purpose)
                }.first()
        }
}

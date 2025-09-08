package scalereal.core.linkHandling

import jakarta.inject.Singleton
import scalereal.core.exception.InvalidLinkException
import java.sql.Timestamp
import java.util.UUID

@Singleton
interface LinkHandlingService {
    fun insertLinkDetails(purpose: String): LinkDetails

    fun updateLinkDetails(linkId: String): LinkDetails

    fun checkLinkValidity(linkId: String)

    fun fetchLinkDetails(linkId: String): LinkDetails
}

@Singleton
class LinkHandlingServiceImpl(
    private val linkHandlingRepo: LinkHandlingRepo,
) : LinkHandlingService {
    private fun getUniqueId() = UUID.randomUUID().toString()

    override fun insertLinkDetails(purpose: String) =
        linkHandlingRepo.insertLinkDetails(
            LinkDetails(
                linkId = getUniqueId(),
                generationTime = Timestamp(System.currentTimeMillis()),
                noOfHit = 0,
                purposeOfLink = purpose,
            ),
        )

    override fun updateLinkDetails(linkId: String): LinkDetails {
        val linkDetails = linkHandlingRepo.fetchLinkDetails(linkId)

        return linkHandlingRepo.updateLinkDetails(
            linkId = linkId,
            noOfHit = linkDetails.noOfHit + 1,
        )
    }

    override fun checkLinkValidity(linkId: String) {
        val linkDetails = linkHandlingRepo.fetchLinkDetails(linkId)
        when {
            (linkDetails.noOfHit > 0) ->
                throw InvalidLinkException("${linkDetails.purposeOfLink} has been done once.")
            (getTimeDifferenceInMinutes(linkDetails.generationTime) > 1440) ->
                throw InvalidLinkException("Link has been expired.")
        }
    }

    override fun fetchLinkDetails(linkId: String): LinkDetails = linkHandlingRepo.fetchLinkDetails(linkId)

    private fun getTimeDifferenceInMinutes(linkGenerationTime: Timestamp) =
        (
            (
                Timestamp(System.currentTimeMillis()).time - linkGenerationTime.time
            ) / 1000
        ) / 60
}

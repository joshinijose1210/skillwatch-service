package scalereal.core.linkHandling

interface LinkHandlingRepo {
    fun insertLinkDetails(linkDetails: LinkDetails): LinkDetails

    fun fetchLinkDetails(linkId: String): LinkDetails

    fun updateLinkDetails(
        noOfHit: Int,
        linkId: String,
    ): LinkDetails
}

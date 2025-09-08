package scalereal.core.linkHandling

import java.sql.Timestamp

data class LinkDetails(
    val linkId: String,
    val generationTime: Timestamp,
    val noOfHit: Int,
    val purposeOfLink: String?,
)

package scalereal.core.models.domain

import java.math.BigDecimal

data class RatingsData(
    val reviewCycleId: Long,
    val id: Long,
    val employeeId: String,
    val firstName: String,
    val lastName: String,
    val checkInRating: Double,
)

data class RatingListingResponse(
    val ratingListingCount: Int,
    val ratingListing: List<RatingsData>,
)

data class RatingTypeRange(
    val minRange: BigDecimal?,
    val maxRange: BigDecimal?,
)

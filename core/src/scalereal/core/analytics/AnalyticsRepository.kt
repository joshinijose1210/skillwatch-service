package scalereal.core.analytics

import scalereal.core.models.domain.RatingsData
import scalereal.core.models.domain.ReviewCount
import java.math.BigDecimal

interface AnalyticsRepository {
    fun getRatings(
        organisationId: Long,
        reviewCycleId: Long,
        minRange: BigDecimal? = null,
        maxRange: BigDecimal? = null,
        employeeId: List<Int> = listOf(-99),
        offset: Int = 1,
        limit: Int = Int.MAX_VALUE,
    ): List<RatingsData>

    fun getRatingListingCount(
        organisationId: Long,
        reviewCycleId: Long,
        minRange: BigDecimal?,
        maxRange: BigDecimal?,
        employeeId: List<Int>,
    ): Int

    fun getSelfReviewStatus(
        organisationId: Long,
        reviewCycleId: Long,
    ): ReviewCount

    fun getManager1ReviewStatus(
        organisationId: Long,
        reviewCycleId: Long,
    ): ReviewCount

    fun getManager2ReviewStatus(
        organisationId: Long,
        reviewCycleId: Long,
    ): ReviewCount

    fun getCheckInReviewStatus(
        organisationId: Long,
        reviewCycleId: Long,
    ): ReviewCount
}

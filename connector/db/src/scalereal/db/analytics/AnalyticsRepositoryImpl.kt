package scalereal.db.analytics

import analytics.GetCheckInReviewStatusParams
import analytics.GetCheckInReviewStatusQuery
import analytics.GetManager1ReviewStatusParams
import analytics.GetManager1ReviewStatusQuery
import analytics.GetManager2ReviewStatusParams
import analytics.GetManager2ReviewStatusQuery
import analytics.GetRatingListingCountParams
import analytics.GetRatingListingCountQuery
import analytics.GetRatingsParams
import analytics.GetRatingsQuery
import analytics.GetRatingsResult
import analytics.GetSelfReviewStatusParams
import analytics.GetSelfReviewStatusQuery
import jakarta.inject.Inject
import jakarta.inject.Singleton
import norm.query
import scalereal.core.analytics.AnalyticsRepository
import scalereal.core.models.domain.RatingsData
import scalereal.core.models.domain.ReviewCount
import java.math.BigDecimal
import javax.sql.DataSource

@Singleton
class AnalyticsRepositoryImpl(
    @Inject private val dataSource: DataSource,
) : AnalyticsRepository {
    override fun getRatings(
        organisationId: Long,
        reviewCycleId: Long,
        minRange: BigDecimal?,
        maxRange: BigDecimal?,
        employeeId: List<Int>,
        offset: Int,
        limit: Int,
    ): List<RatingsData> =
        dataSource.connection.use { connection ->
            GetRatingsQuery()
                .query(
                    connection,
                    GetRatingsParams(organisationId, reviewCycleId, minRange, maxRange, employeeId.toTypedArray(), offset, limit),
                ).map { it.toRatings() }
        }

    override fun getRatingListingCount(
        organisationId: Long,
        reviewCycleId: Long,
        minRange: BigDecimal?,
        maxRange: BigDecimal?,
        employeeId: List<Int>,
    ): Int =
        dataSource.connection.use { connection ->
            GetRatingListingCountQuery()
                .query(
                    connection,
                    GetRatingListingCountParams(organisationId, reviewCycleId, minRange, maxRange, employeeId.toTypedArray()),
                )[0]
                .ratingListingCount
                ?.toInt() ?: 0
        }

    override fun getSelfReviewStatus(
        organisationId: Long,
        reviewCycleId: Long,
    ): ReviewCount =
        dataSource.connection.use { connection ->
            GetSelfReviewStatusQuery()
                .query(connection, GetSelfReviewStatusParams(organisationId = organisationId, reviewCycleId = reviewCycleId))
                .map {
                    ReviewCount(completed = it.completed ?: 0, inProgress = it.inProgress ?: 0, pending = 0)
                }.first()
        }

    override fun getManager1ReviewStatus(
        organisationId: Long,
        reviewCycleId: Long,
    ): ReviewCount =
        dataSource.connection.use { connection ->
            GetManager1ReviewStatusQuery()
                .query(connection, GetManager1ReviewStatusParams(organisationId, reviewCycleId))
                .map {
                    ReviewCount(completed = it.completed ?: 0, inProgress = it.inProgress ?: 0, pending = 0)
                }.first()
        }

    override fun getManager2ReviewStatus(
        organisationId: Long,
        reviewCycleId: Long,
    ): ReviewCount =
        dataSource.connection.use { connection ->
            GetManager2ReviewStatusQuery()
                .query(connection, GetManager2ReviewStatusParams(organisationId, reviewCycleId))
                .map {
                    ReviewCount(completed = it.completed ?: 0, inProgress = it.inProgress ?: 0, pending = 0)
                }.first()
        }

    override fun getCheckInReviewStatus(
        organisationId: Long,
        reviewCycleId: Long,
    ): ReviewCount =
        dataSource.connection.use { connection ->
            GetCheckInReviewStatusQuery()
                .query(connection, GetCheckInReviewStatusParams(organisationId, reviewCycleId))
                .map {
                    ReviewCount(completed = it.completed ?: 0, inProgress = it.inProgress ?: 0, pending = 0)
                }.first()
        }

    private fun GetRatingsResult.toRatings() =
        RatingsData(
            reviewCycleId = reviewCycleId,
            id = id,
            employeeId = employeeId,
            firstName = firstName,
            lastName = lastName,
            checkInRating = checkInAverageRating?.toDouble() ?: -1.00,
        )
}

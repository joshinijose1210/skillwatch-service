package analytics

import io.kotest.core.spec.Spec
import io.kotest.matchers.shouldBe
import norm.executeCommand
import scalereal.core.models.domain.RatingsData
import scalereal.core.models.domain.ReviewCount
import scalereal.db.analytics.AnalyticsRepositoryImpl
import util.StringSpecWithDataSource
import java.io.File
import java.math.BigDecimal

class AnalyticsRepositoryImplTest : StringSpecWithDataSource() {
    private lateinit var analyticsRepositoryImpl: AnalyticsRepositoryImpl

    init {
        "should get ratings" {
            val organisationId = 1L
            val reviewCycleId = 1L
            val employeeId = listOf(-99)
            val expectedRatings =
                listOf(
                    RatingsData(
                        reviewCycleId = 1,
                        id = 5,
                        employeeId = "SR0039",
                        firstName = "Amir",
                        lastName = "Islam",
                        checkInRating = -1.0,
                    ),
                    RatingsData(
                        reviewCycleId = 1,
                        id = 1,
                        employeeId = "SR0006",
                        firstName = "Yogesh",
                        lastName = "Jadhav",
                        checkInRating = 4.8,
                    ),
                    RatingsData(
                        reviewCycleId = 1,
                        id = 3,
                        employeeId = "SR0003",
                        firstName = "Rushad",
                        lastName = "Shaikh",
                        checkInRating = 3.9,
                    ),
                    RatingsData(
                        reviewCycleId = 1,
                        id = 2,
                        employeeId = "SR0002",
                        firstName = "Moly",
                        lastName = "Agarwal",
                        checkInRating = 3.5,
                    ),
                )
            val actualRatings =
                analyticsRepositoryImpl.getRatings(
                    organisationId = organisationId,
                    reviewCycleId = reviewCycleId,
                    minRange = null,
                    maxRange = null,
                    employeeId = employeeId,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                )

            actualRatings shouldBe expectedRatings
        }

        "should get rating listings for exceedExpectations employees" {
            val expectedRatings =
                listOf(
                    RatingsData(
                        reviewCycleId = 1,
                        id = 1,
                        employeeId = "SR0006",
                        firstName = "Yogesh",
                        lastName = "Jadhav",
                        checkInRating = 4.8,
                    ),
                )
            val actualRatings =
                analyticsRepositoryImpl.getRatings(
                    organisationId = 1,
                    reviewCycleId = 1,
                    minRange = BigDecimal("4.00"),
                    maxRange = BigDecimal("4.99"),
                    offset = 0,
                    limit = Int.MAX_VALUE,
                )

            actualRatings shouldBe expectedRatings
        }

        "should get count of rating for given organisation id and review cycle id" {
            val ratingCount =
                analyticsRepositoryImpl.getRatingListingCount(
                    organisationId = 1,
                    reviewCycleId = 1,
                    minRange = null,
                    maxRange = null,
                    employeeId = listOf(-99),
                )
            ratingCount shouldBe 3
        }

        "should return rating listing count 0 for missing ratings" {
            val ratingCount =
                analyticsRepositoryImpl.getRatingListingCount(
                    organisationId = 1,
                    reviewCycleId = 2,
                    minRange = null,
                    maxRange = null,
                    employeeId = listOf(-99),
                )
            ratingCount shouldBe 0
        }

        "should get rating listings for meetsExpectations check-in ratings" {
            val expectedRatings =
                listOf(
                    RatingsData(
                        reviewCycleId = 1,
                        id = 3,
                        employeeId = "SR0003",
                        firstName = "Rushad",
                        lastName = "Shaikh",
                        checkInRating = 3.9,
                    ),
                    RatingsData(
                        reviewCycleId = 1,
                        id = 2,
                        employeeId = "SR0002",
                        firstName = "Moly",
                        lastName = "Agarwal",
                        checkInRating = 3.5,
                    ),
                )
            val actualRatings =
                analyticsRepositoryImpl.getRatings(
                    organisationId = 1,
                    reviewCycleId = 1,
                    minRange = BigDecimal("3.00"),
                    maxRange = BigDecimal("3.99"),
                    offset = 0,
                    limit = Int.MAX_VALUE,
                )
            actualRatings shouldBe expectedRatings
        }

        "should get self review status" {
            val expectedStatus = ReviewCount(completed = 3, inProgress = 1, pending = 0)
            val actualStatus = analyticsRepositoryImpl.getSelfReviewStatus(organisationId = 1, reviewCycleId = 1)
            actualStatus shouldBe expectedStatus
        }

        "should return review count 0 for missing self review status" {
            val expectedStatus = ReviewCount(completed = 0, inProgress = 0, pending = 0)
            val actualStatus = analyticsRepositoryImpl.getSelfReviewStatus(organisationId = 1, reviewCycleId = 2)
            actualStatus shouldBe expectedStatus
        }

        "should get Manager 1 review status" {
            val expectedStatus = ReviewCount(completed = 3, inProgress = 0, pending = 0)
            val manager1ReviewStatus = analyticsRepositoryImpl.getManager1ReviewStatus(organisationId = 1, reviewCycleId = 1)
            manager1ReviewStatus shouldBe expectedStatus
        }

        "should return review count 0 for missing Manager 1 review status" {
            val expectedStatus = ReviewCount(completed = 0, inProgress = 0, pending = 0)
            val manager1ReviewStatus = analyticsRepositoryImpl.getManager1ReviewStatus(organisationId = 1, reviewCycleId = 2)
            manager1ReviewStatus shouldBe expectedStatus
        }

        "should get Manager 2 Review Status" {
            val expectedStatus = ReviewCount(completed = 1, inProgress = 0, pending = 0)
            val manager2ReviewStatus = analyticsRepositoryImpl.getManager2ReviewStatus(organisationId = 1, reviewCycleId = 1)
            manager2ReviewStatus shouldBe expectedStatus
        }

        "should return review count 0 for missing Manager 2 review status" {
            val expectedStatus = ReviewCount(completed = 0, inProgress = 0, pending = 0)
            val manager2ReviewStatus = analyticsRepositoryImpl.getManager2ReviewStatus(organisationId = 1, reviewCycleId = 2)
            manager2ReviewStatus shouldBe expectedStatus
        }

        "should get check-in review status" {
            val expectedStatus = ReviewCount(completed = 3, inProgress = 0, pending = 0)
            val checkInReviewStatus = analyticsRepositoryImpl.getCheckInReviewStatus(organisationId = 1, reviewCycleId = 1)
            checkInReviewStatus shouldBe expectedStatus
        }

        "should return review count 0 for missing check-in review status" {
            val expectedStatus = ReviewCount(completed = 0, inProgress = 0, pending = 0)
            val checkInReviewStatus = analyticsRepositoryImpl.getCheckInReviewStatus(organisationId = 1, reviewCycleId = 2)
            checkInReviewStatus shouldBe expectedStatus
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        val testDataFile = File("./test-res/analytics/analytics-data.sql").readText().trim()
        dataSource.connection.use {
            it.executeCommand(
                testDataFile,
            )
        }
        analyticsRepositoryImpl = AnalyticsRepositoryImpl(dataSource)
    }
}

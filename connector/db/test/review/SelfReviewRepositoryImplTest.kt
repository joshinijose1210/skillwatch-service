package review

import io.kotest.core.spec.Spec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import norm.executeCommand
import scalereal.core.models.domain.Review
import scalereal.core.models.domain.ReviewData
import scalereal.db.review.SelfReviewRepositoryImpl
import util.StringSpecWithDataSource
import java.io.File

class SelfReviewRepositoryImplTest : StringSpecWithDataSource() {
    private lateinit var selfReviewRepositoryImpl: SelfReviewRepositoryImpl

    init {

        "should fetch average rating for selected review cycles" {
            val avgRatings =
                selfReviewRepositoryImpl.getReviewRatingGraphData(
                    organisationId = 1,
                    reviewToId = 2,
                    reviewCycleId = listOf(2, 3),
                )
            avgRatings.avgSelfReviewRating shouldBe "3.50".toBigDecimal()
            avgRatings.avgFirstManagerRating shouldBe "3.50".toBigDecimal()
            avgRatings.avgSecondManagerReviewRating shouldBe "3.50".toBigDecimal()
            avgRatings.avgCheckInReviewRating shouldBe "3.50".toBigDecimal()
        }

        "should add self review" {
            selfReviewRepositoryImpl.create(
                reviewData =
                    ReviewData(
                        organisationId = 1,
                        reviewTypeId = 1,
                        reviewDetailsId = 1,
                        reviewCycleId = 1,
                        reviewToId = 1,
                        reviewFromId = 1,
                        draft = false,
                        published = true,
                        firstManagerId = null,
                        secondManagerId = null,
                        reviewData =
                            listOf(
                                Review(
                                    reviewId = 1,
                                    id = 1,
                                    kraId = null,
                                    kraName = null,
                                    kpiTitle = null,
                                    kpiDescription = null,
                                    review = "It is a test review",
                                    rating = 5,
                                ),
                                Review(
                                    reviewId = 2,
                                    id = 2,
                                    kraId = null,
                                    kraName = null,
                                    kpiTitle = null,
                                    kpiDescription = null,
                                    review = "It is second test review",
                                    rating = 4,
                                ),
                            ),
                        averageRating = 4.5.toBigDecimal(),
                    ),
            )
        }

        "should add self review in draft" {
            selfReviewRepositoryImpl.create(
                reviewData =
                    ReviewData(
                        organisationId = 1,
                        reviewTypeId = 1,
                        reviewDetailsId = 1,
                        reviewCycleId = 1,
                        reviewToId = 1,
                        reviewFromId = 1,
                        draft = true,
                        published = false,
                        firstManagerId = null,
                        secondManagerId = null,
                        reviewData =
                            listOf(
                                Review(
                                    reviewId = 1,
                                    id = 1,
                                    kraId = null,
                                    kraName = null,
                                    kpiTitle = null,
                                    kpiDescription = null,
                                    review = "It is a draft review",
                                    rating = 5,
                                ),
                                Review(
                                    reviewId = 2,
                                    id = 2,
                                    kraId = null,
                                    kraName = null,
                                    kpiTitle = null,
                                    kpiDescription = null,
                                    review = "It is second draft review",
                                    rating = 4,
                                ),
                            ),
                        averageRating = (-1.0).toBigDecimal(),
                    ),
            )
        }

        "should fetch self review" {
            val reviewDetails =
                selfReviewRepositoryImpl.fetch(
                    reviewTypeId = listOf(1),
                    reviewCycleId = 1,
                    reviewToId = 1,
                    reviewFromId = listOf(1),
                )
            val review = selfReviewRepositoryImpl.getReviews(reviewDetailsId = 1)

            reviewDetails[0].reviewDetailsId shouldBe 1
            reviewDetails[0].reviewCycleId shouldBe 1
            reviewDetails[0].reviewToEmployeeId shouldBe "SR0043"
            reviewDetails[0].reviewFromEmployeeId shouldBe "SR0043"
            reviewDetails[0].draft shouldBe false
            reviewDetails[0].published shouldBe true
            review shouldContainAll
                listOf(
                    Review(
                        reviewId = 1,
                        id = 1,
                        kraId = null,
                        kraName = null,
                        kpiTitle = "1st KPI Title",
                        kpiDescription = "Its a detailed description of 1st kpi",
                        review = "It is a test review",
                        rating = 5,
                    ),
                    Review(
                        reviewId = 2,
                        id = 2,
                        kraId = null,
                        kraName = null,
                        kpiTitle = "2nd KPI Title",
                        kpiDescription = "Its a detailed description of 2nd kpi",
                        review = "It is second test review",
                        rating = 4,
                    ),
                )
        }

        "should update draft review details to publish" {
            selfReviewRepositoryImpl.update(
                reviewData =
                    ReviewData(
                        organisationId = 1,
                        reviewTypeId = 1,
                        reviewDetailsId = 2,
                        reviewCycleId = 1,
                        reviewToId = 1,
                        reviewFromId = 1,
                        draft = false,
                        published = true,
                        firstManagerId = null,
                        secondManagerId = null,
                        reviewData =
                            listOf(
                                Review(
                                    reviewId = 1,
                                    id = 1,
                                    kraId = null,
                                    kraName = null,
                                    kpiTitle = null,
                                    kpiDescription = null,
                                    review = "It is a updated self review",
                                    rating = 5,
                                ),
                                Review(
                                    reviewId = 2,
                                    id = 2,
                                    kraId = null,
                                    kraName = null,
                                    kpiTitle = null,
                                    kpiDescription = null,
                                    review = "It is second updated self review",
                                    rating = 4,
                                ),
                            ),
                        averageRating = 4.5.toBigDecimal(),
                    ),
            )
        }

        "should fetch updated review details" {
            val reviewDetails =
                selfReviewRepositoryImpl.fetch(
                    reviewTypeId = listOf(1),
                    reviewCycleId = 1,
                    reviewToId = 1,
                    reviewFromId = listOf(1),
                )
            val review = selfReviewRepositoryImpl.getReviews(reviewDetailsId = 2)

            reviewDetails[0].reviewDetailsId shouldBe 1
            reviewDetails[0].reviewCycleId shouldBe 1
            reviewDetails[0].reviewToEmployeeId shouldBe "SR0043"
            reviewDetails[0].reviewFromEmployeeId shouldBe "SR0043"
            reviewDetails[0].draft shouldBe false
            reviewDetails[0].published shouldBe true

            review shouldContainAll
                listOf(
                    Review(
                        reviewId = 3,
                        id = 1,
                        kraId = null,
                        kraName = null,
                        kpiTitle = "1st KPI Title",
                        kpiDescription = "Its a detailed description of 1st kpi",
                        review = "It is a updated self review",
                        rating = 5,
                    ),
                    Review(
                        reviewId = 4,
                        id = 2,
                        kraId = null,
                        kraName = null,
                        kpiTitle = "2nd KPI Title",
                        kpiDescription = "Its a detailed description of 2nd kpi",
                        review = "It is second updated self review",
                        rating = 4,
                    ),
                )
        }

        "should add Summary review" {
            selfReviewRepositoryImpl.create(
                reviewData =
                    ReviewData(
                        organisationId = 1,
                        reviewTypeId = 3,
                        reviewDetailsId = null,
                        reviewCycleId = 1,
                        reviewToId = 2,
                        reviewFromId = 1,
                        draft = false,
                        published = true,
                        firstManagerId = 1,
                        secondManagerId = null,
                        reviewData =
                            listOf(
                                Review(
                                    reviewId = 1,
                                    id = 1,
                                    kraId = null,
                                    kraName = null,
                                    kpiTitle = null,
                                    kpiDescription = null,
                                    review = "It is a Summary review",
                                    rating = 5,
                                ),
                                Review(
                                    reviewId = 2,
                                    id = 2,
                                    kraId = null,
                                    kraName = null,
                                    kpiTitle = null,
                                    kpiDescription = null,
                                    review = "It is second Summary review",
                                    rating = 4,
                                ),
                            ),
                        averageRating = 4.5.toBigDecimal(),
                    ),
            )
        }

        "should add Summary review in draft" {
            selfReviewRepositoryImpl.create(
                reviewData =
                    ReviewData(
                        organisationId = 1,
                        reviewTypeId = 3,
                        reviewDetailsId = null,
                        reviewCycleId = 1,
                        reviewToId = 2,
                        reviewFromId = 1,
                        draft = true,
                        published = false,
                        firstManagerId = 1,
                        secondManagerId = null,
                        reviewData =
                            listOf(
                                Review(
                                    reviewId = 1,
                                    id = 1,
                                    kraId = null,
                                    kraName = null,
                                    kpiTitle = null,
                                    kpiDescription = null,
                                    review = "It is a Summary review",
                                    rating = 5,
                                ),
                                Review(
                                    reviewId = 2,
                                    id = 2,
                                    kraId = null,
                                    kraName = null,
                                    kpiTitle = null,
                                    kpiDescription = null,
                                    review = "It is second Summary review",
                                    rating = 4,
                                ),
                            ),
                        averageRating = (-1.0).toBigDecimal(),
                    ),
            )
        }

        "should fetch Summary review" {
            val reviewDetails =
                selfReviewRepositoryImpl.fetch(
                    reviewTypeId = listOf(3),
                    reviewCycleId = 1,
                    reviewToId = 2,
                    reviewFromId = listOf(1),
                )
            val review = selfReviewRepositoryImpl.getReviews(reviewDetailsId = 3)

            reviewDetails[0].reviewDetailsId shouldBe 3
            reviewDetails[0].reviewCycleId shouldBe 1
            reviewDetails[0].reviewToEmployeeId shouldBe "SR0051"
            reviewDetails[0].reviewFromEmployeeId shouldBe "SR0043"
            reviewDetails[0].draft shouldBe false
            reviewDetails[0].published shouldBe true

            review shouldContainAll
                listOf(
                    Review(
                        reviewId = 5,
                        id = 1,
                        kraId = null,
                        kraName = null,
                        kpiTitle = "1st KPI Title",
                        kpiDescription = "Its a detailed description of 1st kpi",
                        review = "It is a Summary review",
                        rating = 5,
                    ),
                    Review(
                        reviewId = 6,
                        id = 2,
                        kraId = null,
                        kraName = null,
                        kpiTitle = "2nd KPI Title",
                        kpiDescription = "Its a detailed description of 2nd kpi",
                        review = "It is second Summary review",
                        rating = 4,
                    ),
                )
        }
        "should update Summary review details" {
            selfReviewRepositoryImpl.update(
                reviewData =
                    ReviewData(
                        organisationId = 1,
                        reviewTypeId = 3,
                        reviewDetailsId = 4,
                        reviewCycleId = 1,
                        reviewToId = 2,
                        reviewFromId = 1,
                        draft = false,
                        published = true,
                        firstManagerId = 1,
                        secondManagerId = null,
                        reviewData =
                            listOf(
                                Review(
                                    reviewId = 1,
                                    id = 1,
                                    kraId = null,
                                    kraName = null,
                                    kpiTitle = null,
                                    kpiDescription = null,
                                    review = "It is a updated Summary review",
                                    rating = 5,
                                ),
                                Review(
                                    reviewId = 2,
                                    id = 2,
                                    kraId = null,
                                    kraName = null,
                                    kpiTitle = null,
                                    kpiDescription = null,
                                    review = "It is second updated Summary review",
                                    rating = 4,
                                ),
                            ),
                        averageRating = 4.5.toBigDecimal(),
                    ),
            )
        }

        "should fetch Updated Summary review" {
            val reviewDetails =
                selfReviewRepositoryImpl.fetch(
                    reviewTypeId = listOf(3),
                    reviewCycleId = 1,
                    reviewToId = 2,
                    reviewFromId = listOf(1),
                )
            val review = selfReviewRepositoryImpl.getReviews(reviewDetailsId = 4)

            reviewDetails[0].reviewDetailsId shouldBe 3
            reviewDetails[0].reviewCycleId shouldBe 1
            reviewDetails[0].reviewToEmployeeId shouldBe "SR0051"
            reviewDetails[0].reviewFromEmployeeId shouldBe "SR0043"
            reviewDetails[0].draft shouldBe false
            reviewDetails[0].published shouldBe true

            review shouldContainAll
                listOf(
                    Review(
                        reviewId = 7,
                        id = 1,
                        kraId = null,
                        kraName = null,
                        kpiTitle = "1st KPI Title",
                        kpiDescription = "Its a detailed description of 1st kpi",
                        review = "It is a updated Summary review",
                        rating = 5,
                    ),
                    Review(
                        reviewId = 8,
                        id = 2,
                        kraId = null,
                        kraName = null,
                        kpiTitle = "2nd KPI Title",
                        kpiDescription = "Its a detailed description of 2nd kpi",
                        review = "It is second updated Summary review",
                        rating = 4,
                    ),
                )
        }

        "should fetch average rating for given review details and review cycle id" {
            val averageRating = selfReviewRepositoryImpl.getAverageRating(reviewCycleId = 2, reviewDetailsId = 10)

            averageRating shouldBe "3.00".toBigDecimal()
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        val testDataFile = File("./test-res/review/self-review.sql").readText().trim()
        dataSource.connection.use {
            it.executeCommand(testDataFile)
        }
        selfReviewRepositoryImpl = SelfReviewRepositoryImpl(dataSource)
    }
}

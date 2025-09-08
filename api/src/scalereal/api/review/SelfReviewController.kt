package scalereal.api.review

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.http.annotation.Put
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Singleton
import scalereal.api.common.ErrorMessage
import scalereal.api.common.Response
import scalereal.api.common.ResponseType
import scalereal.core.dto.UpdateReviewRequest
import scalereal.core.exception.SelfReviewNotFoundException
import scalereal.core.models.domain.Review
import scalereal.core.models.domain.ReviewData
import scalereal.core.models.domain.ReviewResponse
import scalereal.core.review.SelfReviewService

@Tag(name = "Review")
@Controller(value = "api/self-review")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Singleton
class SelfReviewController(
    private val selfReviewService: SelfReviewService,
) {
    @Operation(summary = "Add review", description = "Add self review, manager review and check-in with manager")
    @Produces(MediaType.APPLICATION_JSON)
    @Post("/")
    fun create(
        organisationId: Long,
        reviewTypeId: Int,
        reviewCycleId: Long,
        reviewToId: Long,
        reviewFromId: Long,
        firstManagerId: Long?,
        secondManagerId: Long?,
        draft: Boolean,
        published: Boolean,
        reviewData: List<Review>,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    selfReviewService.create(
                        ReviewData(
                            organisationId = organisationId,
                            reviewTypeId = reviewTypeId,
                            reviewDetailsId = null,
                            reviewCycleId = reviewCycleId,
                            reviewToId = reviewToId,
                            reviewFromId = reviewFromId,
                            firstManagerId = firstManagerId,
                            secondManagerId = secondManagerId,
                            draft = draft,
                            published = published,
                            reviewData = reviewData,
                            averageRating = (-1.0).toBigDecimal(),
                        ),
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Get review", description = "Get self review, manager review and check-in with manager data")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(type = "array", implementation = ReviewResponse::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/")
    fun fetch(
        reviewTypeId: List<Int>,
        reviewCycleId: Long,
        reviewToId: Long,
        reviewFromId: List<Int>,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    selfReviewService.fetch(
                        reviewTypeId = reviewTypeId,
                        reviewCycleId = reviewCycleId,
                        reviewToId = reviewToId,
                        reviewFromId = reviewFromId,
                    ),
            )
        } catch (e: SelfReviewNotFoundException) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Edit review", description = "Edit self review, manager review and check-in with manager")
    @Put("/")
    fun update(
        organisationId: Long,
        reviewTypeId: Int,
        reviewDetailsId: Long,
        reviewCycleId: Long,
        reviewToId: Long,
        reviewFromId: Long,
        firstManagerId: Long?,
        secondManagerId: Long?,
        draft: Boolean,
        published: Boolean,
        reviewData: List<Review>,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    selfReviewService.update(
                        ReviewData(
                            organisationId = organisationId,
                            reviewTypeId = reviewTypeId,
                            reviewDetailsId = reviewDetailsId,
                            reviewCycleId = reviewCycleId,
                            reviewToId = reviewToId,
                            reviewFromId = reviewFromId,
                            firstManagerId = firstManagerId,
                            secondManagerId = secondManagerId,
                            draft = draft,
                            published = published,
                            reviewData = reviewData,
                            averageRating = (-1.0).toBigDecimal(),
                        ),
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Put("/auto-save")
    fun updateReview(
        @Body updateReviewRequest: UpdateReviewRequest,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = selfReviewService.updateReview(updateReviewRequest),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Get("/by-id")
    fun getReviewById(reviewId: Long): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    selfReviewService.getReviewById(
                        reviewId = reviewId,
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Get("/weighted-score")
    fun getReviewWeightedScore(
        reviewCycleId: Long,
        reviewDetailsId: Long,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    selfReviewService.getWeightedReviewScore(
                        reviewCycleId = reviewCycleId,
                        reviewDetailsId = reviewDetailsId,
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
}

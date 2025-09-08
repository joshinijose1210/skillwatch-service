package scalereal.api.review

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
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
import scalereal.core.models.domain.CheckInResponse
import scalereal.core.models.domain.CheckInWithManagerRequest
import scalereal.core.models.domain.Goal
import scalereal.core.models.domain.GoalParams
import scalereal.core.models.domain.Review
import scalereal.core.review.CheckInWithManagerService

@Tag(name = "Review")
@Controller(value = "api/check-in-with-manager")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Singleton
class CheckInWithManagerController(
    private val checkInWithManagerService: CheckInWithManagerService,
) {
    @Operation(summary = "Edit check-in with manager details", description = "Edit check-in data and add,edit goals")
    @Produces(MediaType.APPLICATION_JSON)
    @Put("/")
    fun createCheckInWithManager(
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
        goals: List<GoalParams>,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    checkInWithManagerService.createCheckInWithManager(
                        CheckInWithManagerRequest(
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
                            goals = goals,
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

    @Operation(summary = "Get check-in with manager data")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            content = [Content(schema = Schema(type = "array", implementation = CheckInResponse::class))],
        ),
        ApiResponse(
            responseCode = "404",
            content = [Content(schema = Schema(implementation = ErrorMessage::class))],
        ),
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
                    checkInWithManagerService.fetch(
                        reviewTypeId = reviewTypeId,
                        reviewCycleId = reviewCycleId,
                        reviewToId = reviewToId,
                        reviewFromId = reviewFromId,
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Put("/goals")
    fun update(goal: List<Goal>): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = checkInWithManagerService.updateGoals(goal),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
}

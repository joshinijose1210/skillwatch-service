package scalereal.api.feedbacks

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import jakarta.inject.Singleton
import scalereal.api.common.ErrorMessage
import scalereal.api.common.Response
import scalereal.api.common.ResponseType
import scalereal.core.feedbacks.FeedbackRequestService
import scalereal.core.models.domain.AddFeedbackData
import scalereal.core.models.domain.ExternalFeedbackRequestData
import scalereal.core.models.domain.FeedbackRequestParams
import scalereal.core.models.domain.FeedbackRequestResponse
import scalereal.core.roles.RoleService
import java.lang.Exception

@Tag(name = "Feedback Request")
@Controller(value = "/api/feedback-request")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Singleton
class FeedbackRequestController(
    private val feedbackRequestService: FeedbackRequestService,
) {
    @Inject
    lateinit var roleService: RoleService

    @Operation(summary = "Get sent feedback request data")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = FeedbackRequestResponse::class))]),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Produces(MediaType.APPLICATION_JSON)
    @Get("/sent")
    fun fetchSentFeedbackRequestData(
        organisationId: Long,
        id: Int,
        feedbackToId: List<Int>,
        feedbackFromId: List<Int>,
        isSubmitted: List<String>,
        reviewCycleId: List<Int>,
        page: Int?,
        limit: Int?,
        sortBy: String?,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                message = "",
                body =
                    FeedbackRequestResponse(
                        pendingFeedbackRequestCount =
                            if (isSubmitted.contains("false")) {
                                feedbackRequestService.countFeedbackRequestData(
                                    FeedbackRequestParams(
                                        organisationId = organisationId,
                                        requestedById = listOf(id),
                                        feedbackToId = feedbackToId,
                                        feedbackFromId = feedbackFromId,
                                        isSubmitted = listOf("false"),
                                        reviewCycleId = reviewCycleId,
                                        sortBy = null,
                                    ),
                                )
                            } else {
                                0
                            },
                        totalFeedbackRequestDataCount =
                            feedbackRequestService.countFeedbackRequestData(
                                FeedbackRequestParams(
                                    organisationId = organisationId,
                                    requestedById = listOf(id),
                                    feedbackToId = feedbackToId,
                                    feedbackFromId = feedbackFromId,
                                    isSubmitted = isSubmitted,
                                    reviewCycleId = reviewCycleId,
                                    sortBy = null,
                                ),
                            ),
                        feedbackRequestData =
                            feedbackRequestService.fetchFeedbackRequestData(
                                FeedbackRequestParams(
                                    organisationId = organisationId,
                                    requestedById = listOf(id),
                                    feedbackToId = feedbackToId,
                                    feedbackFromId = feedbackFromId,
                                    isSubmitted = isSubmitted,
                                    reviewCycleId = reviewCycleId,
                                    sortBy = sortBy ?: "dateDesc",
                                ),
                                page = page ?: 1,
                                limit = limit ?: Int.MAX_VALUE,
                            ),
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Get received feedback request")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = FeedbackRequestResponse::class))]),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Produces(MediaType.APPLICATION_JSON)
    @Get("/received")
    fun fetchReceivedFeedbackRequestData(
        organisationId: Long,
        id: Int,
        requestedById: List<Int>,
        feedbackToId: List<Int>,
        isSubmitted: List<String>,
        reviewCycleId: List<Int>,
        page: Int?,
        limit: Int?,
        sortBy: String?,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                message = "",
                body =
                    FeedbackRequestResponse(
                        pendingFeedbackRequestCount =
                            if (isSubmitted.contains("false")) {
                                feedbackRequestService.countFeedbackRequestData(
                                    FeedbackRequestParams(
                                        organisationId = organisationId,
                                        requestedById = requestedById,
                                        feedbackToId = feedbackToId,
                                        feedbackFromId = listOf(id),
                                        isSubmitted = listOf("false"),
                                        reviewCycleId = reviewCycleId,
                                        sortBy = null,
                                    ),
                                )
                            } else {
                                0
                            },
                        totalFeedbackRequestDataCount =
                            feedbackRequestService.countFeedbackRequestData(
                                FeedbackRequestParams(
                                    organisationId = organisationId,
                                    requestedById = requestedById,
                                    feedbackToId = feedbackToId,
                                    feedbackFromId = listOf(id),
                                    isSubmitted = isSubmitted,
                                    reviewCycleId = reviewCycleId,
                                    sortBy = null,
                                ),
                            ),
                        feedbackRequestData =
                            feedbackRequestService.fetchFeedbackRequestData(
                                FeedbackRequestParams(
                                    organisationId = organisationId,
                                    requestedById = requestedById,
                                    feedbackToId = feedbackToId,
                                    feedbackFromId = listOf(id),
                                    isSubmitted = isSubmitted,
                                    reviewCycleId = reviewCycleId,
                                    sortBy = sortBy ?: "dateDesc",
                                ),
                                page = page ?: 1,
                                limit = limit ?: Int.MAX_VALUE,
                            ),
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Add feedback request")
    @Post("/")
    fun addFeedbackRequest(
        requestedBy: Long,
        feedbackToId: List<Long>,
        feedbackFromId: List<Long>?,
        feedbackFromEmail: List<String>?,
        isExternalRequest: Boolean,
        goalId: Long?,
        request: String,
        organisationId: Long,
        markdownRequest: String,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    feedbackRequestService.addFeedbackRequest(
                        requestedBy = requestedBy,
                        feedbackToId = feedbackToId,
                        feedbackFromId = feedbackFromId,
                        feedbackFromEmail = feedbackFromEmail,
                        isExternalRequest = isExternalRequest,
                        goalId = goalId,
                        request = request,
                        organisationId = organisationId,
                        markdownRequest = markdownRequest,
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Get("/goals")
    fun getGoals(
        organisationId: Long,
        feedbackToId: Long,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    feedbackRequestService.getGoals(
                        organisationId = organisationId,
                        feedbackToId = feedbackToId,
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Get external feedback request data")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = ExternalFeedbackRequestData::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Secured(SecurityRule.IS_ANONYMOUS)
    @Produces(MediaType.APPLICATION_JSON)
    @Get("/external")
    fun getExternalFeedbackRequestData(
        linkId: String,
        requestId: Long,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = feedbackRequestService.getExternalFeedbackRequestData(linkId, requestId),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Add external feedback")
    @Secured(SecurityRule.IS_ANONYMOUS)
    @Post("/external/")
    fun addExternalFeedback(
        linkId: String,
        feedbackToId: Long,
        feedbackFromId: Long,
        feedback: List<AddFeedbackData>,
        requestId: Long,
    ): HttpResponse<Any> =
        try {
            Response(
                status = ResponseType.CREATED,
                message = "",
                body =
                    feedbackRequestService.addExternalFeedback(
                        linkId = linkId,
                        feedbackToId = feedbackToId,
                        feedbackFromId = feedbackFromId,
                        feedback = feedback,
                        requestId = requestId,
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Get("/{id}")
    fun getFeedbackRequestDetailsById(id: Long): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = feedbackRequestService.getFeedbackRequestDetailsById(id),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
}

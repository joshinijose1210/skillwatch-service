package scalereal.api.feedbacks

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Patch
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.http.annotation.Put
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
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
import scalereal.core.feedbacks.FeedbackService
import scalereal.core.models.domain.AddFeedbackData
import scalereal.core.models.domain.CreateFeedbackParams
import scalereal.core.models.domain.FeedbackResponse
import scalereal.core.models.domain.FeedbacksResponse
import scalereal.core.models.domain.UpdateFeedbackData
import scalereal.core.models.domain.UpdateFeedbackParams
import scalereal.core.modules.Modules
import scalereal.core.roles.RoleService
import java.lang.Exception

@Tag(name = "Feedback")
@Controller(value = "/api/feedback")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Singleton
class FeedbackController(
    private val feedbackService: FeedbackService,
) {
    @Inject
    lateinit var roleService: RoleService

    @Operation(summary = "Add feedback")
    @Produces(MediaType.APPLICATION_JSON)
    @Post("/")
    fun create(
        feedbackToId: Long,
        feedbackFromId: Long,
        requestId: Long?,
        isDraft: Boolean,
        feedbackData: List<AddFeedbackData>,
    ): HttpResponse<Any> =
        try {
            Response(
                status = ResponseType.SUCCESS,
                message = "",
                body =
                    feedbackService.create(
                        CreateFeedbackParams(
                            feedback = feedbackData,
                            feedbackToId = feedbackToId,
                            feedbackFromId = feedbackFromId,
                            requestId = requestId,
                            isDraft = isDraft,
                        ),
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Edit feedback")
    @Put("/")
    fun update(
        feedbackToId: Long,
        feedbackFromId: Long,
        feedbackData: List<UpdateFeedbackData>,
        requestId: Long?,
        isDraft: Boolean,
    ): HttpResponse<Any> =
        try {
            Response(
                status = ResponseType.SUCCESS,
                message = "",
                body =
                    feedbackService.update(
                        UpdateFeedbackParams(
                            feedbackToId = feedbackToId,
                            feedbackFromId = feedbackFromId,
                            requestId = requestId,
                            isDraft = isDraft,
                            feedbackData = feedbackData,
                        ),
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Get submitted feedback list")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            content = [Content(schema = Schema(implementation = FeedbackResponse::class))],
        ),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/submitted/")
    fun fetchAllSubmittedFeedbacks(
        organisationId: Long,
        feedbackFromId: Long,
        feedbackToId: List<Int>,
        feedbackTypeId: List<Int>,
        reviewCycleId: List<Int>,
        page: Int?,
        limit: Int?,
        sortBy: String?,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    FeedbackResponse(
                        totalFeedbacks =
                            feedbackService.countSubmittedFeedbacks(
                                organisationId = organisationId,
                                feedbackFromId = feedbackFromId,
                                feedbackToId = feedbackToId,
                                feedbackTypeId = feedbackTypeId,
                                reviewCycleId = reviewCycleId,
                            ),
                        feedbacks =
                            feedbackService.fetchAllSubmittedFeedbacks(
                                organisationId = organisationId,
                                feedbackFromId = feedbackFromId,
                                feedbackToId = feedbackToId,
                                feedbackTypeId = feedbackTypeId,
                                reviewCycleId = reviewCycleId,
                                page = page ?: 1,
                                limit = limit ?: Int.MAX_VALUE,
                                sortBy = sortBy ?: "dateDesc",
                            ),
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Get received feedback list")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            content = [Content(schema = Schema(implementation = FeedbackResponse::class))],
        ),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/received/")
    fun fetchAllFeedbackReceived(
        organisationId: Long,
        feedbackToId: Long,
        feedbackFromId: List<Int>,
        feedbackTypeId: List<Int>,
        reviewCycleId: List<Int>,
        page: Int?,
        limit: Int?,
        sortBy: String?,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    FeedbackResponse(
                        unReadFeedbackCount =
                            feedbackService.getUnreadFeedbackCount(
                                organisationId = organisationId,
                                feedbackToId = feedbackToId,
                                feedbackFromId = feedbackFromId,
                                feedbackTypeId = feedbackTypeId,
                                reviewCycleId = reviewCycleId,
                            ),
                        totalFeedbacks =
                            feedbackService.countFeedbacksReceived(
                                organisationId = organisationId,
                                feedbackToId = feedbackToId,
                                feedbackFromId = feedbackFromId,
                                feedbackTypeId = feedbackTypeId,
                                reviewCycleId = reviewCycleId,
                            ),
                        feedbacks =
                            feedbackService.fetchAllFeedbacksReceived(
                                organisationId = organisationId,
                                feedbackToId = feedbackToId,
                                feedbackFromId = feedbackFromId,
                                feedbackTypeId = feedbackTypeId,
                                reviewCycleId = reviewCycleId,
                                page = page ?: 1,
                                limit = limit ?: Int.MAX_VALUE,
                                sortBy = sortBy ?: "dateDesc",
                            ),
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Get all feedback list")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            content = [Content(schema = Schema(implementation = FeedbackResponse::class))],
        ),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "400", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/")
    fun fetchAllFeedbacks(
        organisationId: Long,
        searchText: String?,
        feedbackTypeId: List<Int>,
        fromDate: String?,
        toDate: String?,
        reviewCycleId: List<Int>,
        page: Int?,
        limit: Int?,
        sortBy: String?,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions =
            roleService.hasPermission(
                organisationId = organisationId,
                roleName = authentication.roles,
                moduleName = Modules.EMPLOYEE_FEEDBACK.moduleName,
            )
        if (!permissions.view) {
            return Response(
                ResponseType.FORBIDDEN,
                body = ErrorMessage("Access Denied! You do not have permission to access this route."),
            ).getHttpResponse()
        }
        return try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    FeedbacksResponse(
                        totalFeedbacks =
                            feedbackService.countAllFeedbacks(
                                organisationId = organisationId,
                                searchText = searchText,
                                feedbackTypeId = feedbackTypeId,
                                fromDate = fromDate,
                                toDate = toDate,
                                reviewCycleId = reviewCycleId,
                            ),
                        feedbacks =
                            feedbackService.fetchAllFeedbacks(
                                organisationId = organisationId,
                                searchText = searchText,
                                feedbackTypeId = feedbackTypeId,
                                fromDate = fromDate,
                                toDate = toDate,
                                reviewCycleId = reviewCycleId,
                                page = page ?: 1,
                                limit = limit ?: Int.MAX_VALUE,
                                sortBy = sortBy ?: "dateDesc",
                            ),
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Patch("/{id}/mark-read/")
    fun markFeedbackAsReadOrUnread(id: Long): HttpResponse<Any> =
        try {
            Response(ResponseType.SUCCESS, "", body = feedbackService.markFeedbackAsReadOrUnread(id, isRead = true))
        } catch (e: Exception) {
            Response(ResponseType.BAD_REQUEST, body = ErrorMessage(e.message.toString()))
        }.getHttpResponse()
}

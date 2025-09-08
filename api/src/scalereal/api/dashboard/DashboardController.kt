package scalereal.api.dashboard

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
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
import scalereal.core.dashboard.DashboardService
import scalereal.core.feedbacks.FeedbackService
import scalereal.core.models.domain.AvgReviewRatings
import scalereal.core.models.domain.EmployeeFeedbackResponse
import scalereal.core.models.domain.FeedbackGraph
import scalereal.core.models.domain.GoalsResponse
import scalereal.core.models.domain.OverviewData
import scalereal.core.review.SelfReviewService
import java.lang.Exception

@Tag(name = "Dashboard")
@Controller(value = "/api/dashboard")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Singleton
class DashboardController(
    private val dashboardService: DashboardService,
) {
    @Inject
    lateinit var feedbackService: FeedbackService

    @Inject
    lateinit var selfReviewService: SelfReviewService

    @Operation(summary = "Get feedback overview")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(type = "array", implementation = OverviewData::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Produces(MediaType.APPLICATION_JSON)
    @Get("/feedback-overview")
    fun fetchFeedbackOverview(
        organisationId: Long,
        id: Long,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    dashboardService.fetchFeedbackOverview(
                        organisationId = organisationId,
                        id = id,
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Get feedback")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = EmployeeFeedbackResponse::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/employee-feedback")
    fun fetchEmployeeFeedback(
        organisationId: Long,
        id: List<Int>,
        feedbackTypeId: List<Int>,
        reviewCycleId: List<Int>,
        page: Int?,
        limit: Int?,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    EmployeeFeedbackResponse(
                        positiveFeedbackCount =
                            dashboardService.countEmployeeFeedback(
                                organisationId = organisationId,
                                id = id,
                                reviewCycleId = reviewCycleId,
                                feedbackTypeId = listOf(1),
                            ),
                        improvementFeedbackCount =
                            dashboardService.countEmployeeFeedback(
                                organisationId = organisationId,
                                id = id,
                                reviewCycleId = reviewCycleId,
                                feedbackTypeId = listOf(2),
                            ),
                        appreciationFeedbackCount =
                            dashboardService.countEmployeeFeedback(
                                organisationId = organisationId,
                                id = id,
                                reviewCycleId = reviewCycleId,
                                feedbackTypeId = listOf(3),
                            ),
                        totalFeedbacks =
                            dashboardService.countEmployeeFeedback(
                                organisationId = organisationId,
                                id = id,
                                reviewCycleId = reviewCycleId,
                                feedbackTypeId = feedbackTypeId,
                            ),
                        feedbacks =
                            dashboardService.fetchEmployeeFeedback(
                                organisationId = organisationId,
                                id = id,
                                feedbackTypeId = feedbackTypeId,
                                reviewCycleId = reviewCycleId,
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

    @Operation(summary = "Get Goals")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = GoalsResponse::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/goal")
    fun fetchGoal(
        organisationId: Long,
        id: Long,
        reviewCycleId: List<Int>,
        page: Int?,
        limit: Int?,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    GoalsResponse(
                        totalGoals =
                            dashboardService.countGoal(
                                organisationId = organisationId,
                                id = id,
                                reviewCycleId = reviewCycleId,
                            ),
                        goals =
                            dashboardService.fetchGoal(
                                organisationId = organisationId,
                                id = id,
                                reviewCycleId = reviewCycleId,
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

    @Operation(summary = "Get feedback graph data")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = FeedbackGraph::class))]),
        ApiResponse(responseCode = "400", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/feedback-graph")
    fun getFeedbackGraphData(
        id: Long,
        reviewCycleId: List<Long>,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                message = "",
                body = feedbackService.getFeedbackGraphData(id, reviewCycleId),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.BAD_REQUEST,
                message = "",
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Get average rating")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = AvgReviewRatings::class))]),
        ApiResponse(responseCode = "400", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/average-ratings")
    fun getReviewRatingGraphData(
        organisationId: Long,
        reviewToId: Long,
        reviewCycleId: List<Int>,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                message = "",
                body = selfReviewService.getReviewRatingGraphData(organisationId, reviewToId, reviewCycleId),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.BAD_REQUEST,
                message = "",
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
}

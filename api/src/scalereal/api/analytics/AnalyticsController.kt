package scalereal.api.analytics

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
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
import scalereal.core.analytics.AnalyticsService
import scalereal.core.models.domain.AnalyticsEmployeesData
import scalereal.core.models.domain.AnalyticsFeedbackResponse
import scalereal.core.models.domain.RatingListingResponse
import scalereal.core.models.domain.Ratings
import scalereal.core.models.domain.RatingsData
import scalereal.core.models.domain.ReviewStatus
import scalereal.core.modules.Modules
import scalereal.core.roles.RoleService

@Tag(name = "Analytics")
@Controller("/api/analytics")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Singleton
class AnalyticsController(
    private val analyticsService: AnalyticsService,
) {
    @Inject
    lateinit var roleService: RoleService

    private val moduleName = Modules.ANALYTICS.moduleName

    @Operation(summary = "Get Ratings")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = Ratings::class))]),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Produces(MediaType.APPLICATION_JSON)
    @Get("/ratings")
    fun getRatings(
        organisationId: Long,
        reviewCycleId: Long,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions = roleService.hasPermission(organisationId, authentication.roles, moduleName)
        if (!permissions.view) {
            return Response(
                ResponseType.FORBIDDEN,
                body = ErrorMessage("Access Denied! You do not have permission to access this route."),
            ).getHttpResponse()
        }
        return try {
            Response(
                ResponseType.SUCCESS,
                message = "",
                body = analyticsService.getRatings(organisationId, reviewCycleId),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Operation(summary = "Get Rankings")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(type = "array", implementation = RatingsData::class))]),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/rankings")
    fun getRankings(
        organisationId: Long,
        reviewCycleId: Long,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions = roleService.hasPermission(organisationId, authentication.roles, moduleName)
        if (!permissions.view) {
            return Response(
                ResponseType.FORBIDDEN,
                body = ErrorMessage("Access Denied! You do not have permission to access this route."),
            ).getHttpResponse()
        }
        return try {
            Response(
                ResponseType.SUCCESS,
                message = "",
                body = analyticsService.getRankings(organisationId, reviewCycleId),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Operation(summary = "Get Review Status")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = ReviewStatus::class))]),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/review-status")
    fun getReviewStatus(
        organisationId: Long,
        reviewCycleId: Long,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions = roleService.hasPermission(organisationId, authentication.roles, moduleName)
        if (!permissions.view) {
            return Response(
                ResponseType.FORBIDDEN,
                body = ErrorMessage("Access Denied! You do not have permission to access this route."),
            ).getHttpResponse()
        }
        return try {
            Response(
                ResponseType.SUCCESS,
                message = "",
                body = analyticsService.getReviewStatus(organisationId, reviewCycleId),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Operation(summary = "Get Feedback Graph")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = AnalyticsFeedbackResponse::class))]),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/feedback-graph")
    fun getFeedbackGraphData(
        organisationId: Long,
        reviewCycleId: Long,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions = roleService.hasPermission(organisationId, authentication.roles, moduleName)
        if (!permissions.view) {
            return Response(
                ResponseType.FORBIDDEN,
                body = ErrorMessage("Access Denied! You do not have permission to access this route."),
            ).getHttpResponse()
        }
        return try {
            Response(
                ResponseType.SUCCESS,
                message = "",
                body = analyticsService.getFeedbackGraphData(organisationId, reviewCycleId),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                message = "Data not found",
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Operation(summary = "Get Rating Listing")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = RatingListingResponse::class))]),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/rating-listing")
    fun getRatingListing(
        organisationId: Long,
        reviewCycleId: Long,
        ratingType: String,
        employeeId: List<Int>,
        page: Int?,
        limit: Int?,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions = roleService.hasPermission(organisationId, authentication.roles, moduleName)
        if (!permissions.view) {
            return Response(
                ResponseType.FORBIDDEN,
                body = ErrorMessage("Access Denied! You do not have permission to access this route."),
            ).getHttpResponse()
        }
        return try {
            Response(
                ResponseType.SUCCESS,
                message = "",
                body =
                    RatingListingResponse(
                        ratingListingCount =
                            analyticsService.getRatingListingCount(
                                organisationId = organisationId,
                                reviewCycleId = reviewCycleId,
                                ratingType = ratingType,
                                employeeId = employeeId,
                            ),
                        ratingListing =
                            analyticsService.getRatingListing(
                                organisationId = organisationId,
                                reviewCycleId = reviewCycleId,
                                ratingType = ratingType,
                                employeeId = employeeId,
                                page = page ?: 1,
                                limit = limit ?: Int.MAX_VALUE,
                            ),
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                message = "Data not found",
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Operation(summary = "Get Employees Data")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = AnalyticsEmployeesData::class))]),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/employees-data")
    fun getEmployeesData(
        organisationId: Long,
        reviewCycleId: Long,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions = roleService.hasPermission(organisationId, authentication.roles, moduleName)
        if (!permissions.view) {
            return Response(
                ResponseType.FORBIDDEN,
                body = ErrorMessage("Access Denied! You do not have permission to access this route."),
            ).getHttpResponse()
        }
        return try {
            Response(
                ResponseType.SUCCESS,
                message = "",
                body =
                    AnalyticsEmployeesData(
                        gendersData = analyticsService.getGendersData(organisationId = organisationId, reviewCycleId = reviewCycleId),
                        averageTenure = analyticsService.getAverageTenure(organisationId = organisationId, reviewCycleId = reviewCycleId),
                        averageAge = analyticsService.getAverageAge(organisationId = organisationId, reviewCycleId = reviewCycleId),
                        experienceRangeCount =
                            analyticsService.getEmployeeCountByTotalExperience(
                                organisationId = organisationId,
                                reviewCycleId = reviewCycleId,
                            ),
                        employeesType =
                            analyticsService.getEmployeesType(
                                organisationId = organisationId,
                                reviewCycleId = reviewCycleId,
                            ),
                        teamEmployeeCount =
                            analyticsService.getEmployeesCountInTeamDuringReviewCycle(
                                organisationId = organisationId,
                                reviewCycleId = reviewCycleId,
                            ),
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }
}

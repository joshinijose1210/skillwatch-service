package scalereal.api.reviewCycle

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
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
import scalereal.core.exception.DateException
import scalereal.core.exception.ReviewCycleException
import scalereal.core.models.domain.ActiveReviewCycleResponse
import scalereal.core.models.domain.CheckInWithManager
import scalereal.core.models.domain.CheckInWithManagerParams
import scalereal.core.models.domain.ManagerReviewCycleResponse
import scalereal.core.models.domain.MyManagerReviewCycleResponse
import scalereal.core.models.domain.ReviewCycle
import scalereal.core.models.domain.ReviewCycleResponse
import scalereal.core.models.domain.ReviewCycleTimeline
import scalereal.core.models.domain.UserActivityData
import scalereal.core.modules.Modules
import scalereal.core.reviewCycle.ReviewCycleService
import scalereal.core.roles.RoleService
import java.sql.Date

@Tag(name = "Review Cycle")
@Controller(value = "/api/review-cycle")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Singleton
class ReviewCycleController(
    private val reviewCycleService: ReviewCycleService,
) {
    @Inject
    lateinit var roleService: RoleService

    private val moduleName = Modules.REVIEW_CYCLE.moduleName

    @Operation(summary = "Get review cycle list")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = ReviewCycleResponse::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Produces(MediaType.APPLICATION_JSON)
    @Get("/getAll/")
    fun fetch(
        organisationId: Long,
        page: Int?,
        limit: Int?,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    ReviewCycleResponse(
                        totalReviewCycles =
                            reviewCycleService.count(
                                organisationId = organisationId,
                            ),
                        reviewCycles =
                            reviewCycleService.fetch(
                                organisationId = organisationId,
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

    @Operation(summary = "Create review cycle")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "400", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Post("/")
    fun create(
        organisationId: Long,
        actionBy: Long,
        ipAddress: String,
        startDate: Date,
        endDate: Date,
        publish: Boolean,
        selfReviewStartDate: Date,
        selfReviewEndDate: Date,
        managerReviewStartDate: Date,
        managerReviewEndDate: Date,
        checkInWithManagerStartDate: Date,
        checkInWithManagerEndDate: Date,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions = roleService.hasPermission(organisationId, authentication.roles, moduleName)
        if (!permissions.edit) {
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
                    reviewCycleService.create(
                        ReviewCycle(
                            organisationId = organisationId,
                            reviewCycleId = -999,
                            startDate = startDate,
                            endDate = endDate,
                            publish = publish,
                            lastModified = null,
                            selfReviewStartDate = selfReviewStartDate,
                            selfReviewEndDate = selfReviewEndDate,
                            managerReviewStartDate = managerReviewStartDate,
                            managerReviewEndDate = managerReviewEndDate,
                            checkInWithManagerStartDate = checkInWithManagerStartDate,
                            checkInWithManagerEndDate = checkInWithManagerEndDate,
                            isSelfReviewDatePassed = false,
                            isManagerReviewDatePassed = false,
                            isCheckInWithManagerDatePassed = false,
                        ),
                        UserActivityData(
                            actionBy = actionBy,
                            ipAddress = ipAddress,
                        ),
                    ),
            )
        } catch (e: DateException) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        } catch (e: ReviewCycleException) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Operation(summary = "Edit review cycle")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "400", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Put("/{id}")
    fun update(
        organisationId: Long,
        actionBy: Long,
        ipAddress: String,
        id: Long,
        startDate: Date,
        endDate: Date,
        publish: Boolean,
        selfReviewStartDate: Date,
        selfReviewEndDate: Date,
        managerReviewStartDate: Date,
        managerReviewEndDate: Date,
        checkInWithManagerStartDate: Date,
        checkInWithManagerEndDate: Date,
        notifyEmployees: Boolean,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions = roleService.hasPermission(organisationId, authentication.roles, moduleName)
        if (!permissions.edit) {
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
                    reviewCycleService.update(
                        ReviewCycle(
                            organisationId = organisationId,
                            reviewCycleId = id,
                            startDate = startDate,
                            endDate = endDate,
                            publish = publish,
                            lastModified = null,
                            selfReviewStartDate = selfReviewStartDate,
                            selfReviewEndDate = selfReviewEndDate,
                            managerReviewStartDate = managerReviewStartDate,
                            managerReviewEndDate = managerReviewEndDate,
                            checkInWithManagerStartDate = checkInWithManagerStartDate,
                            checkInWithManagerEndDate = checkInWithManagerEndDate,
                            isSelfReviewDatePassed = false,
                            isManagerReviewDatePassed = false,
                            isCheckInWithManagerDatePassed = false,
                        ),
                        UserActivityData(
                            actionBy = actionBy,
                            ipAddress = ipAddress,
                        ),
                        notifyEmployees = notifyEmployees,
                    ),
            )
        } catch (e: DateException) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        } catch (e: ReviewCycleException) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Operation(summary = "Get self review list")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = ActiveReviewCycleResponse::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/self-review")
    fun fetchSelfReviewCycle(
        organisationId: Long,
        reviewTypeId: List<Int>,
        reviewToId: List<Int>,
        reviewFromId: List<Int>,
        reviewCycleId: List<Int>,
        page: Int?,
        limit: Int?,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    ActiveReviewCycleResponse(
                        totalReviewCycles =
                            reviewCycleService.countSelfReviewCycle(
                                organisationId,
                                reviewTypeId,
                                reviewToId,
                                reviewFromId,
                                reviewCycleId,
                            ),
                        reviewCycles =
                            reviewCycleService.fetchSelfReviewCycle(
                                organisationId,
                                reviewTypeId,
                                reviewToId,
                                reviewFromId,
                                reviewCycleId,
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

    @Operation(summary = "Review for team member list")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = ManagerReviewCycleResponse::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/manager-review")
    fun fetchManagerReviewCycle(
        organisationId: Long,
        reviewTypeId: Int,
        reviewToId: List<Int>,
        reviewFromId: Long,
        reviewCycleId: List<Int>,
        managerReviewDraft: Boolean?,
        managerReviewPublished: Boolean?,
        page: Int?,
        limit: Int?,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    ManagerReviewCycleResponse(
                        totalManagerReviewCycles =
                            reviewCycleService.countManagerReviewCycle(
                                organisationId = organisationId,
                                reviewTypeId = reviewTypeId,
                                reviewToId = reviewToId,
                                reviewFromId = reviewFromId,
                                reviewCycleId = reviewCycleId,
                                managerReviewDraft = managerReviewDraft,
                                managerReviewPublished = managerReviewPublished,
                            ),
                        managerReviewCycles =
                            reviewCycleService.fetchManagerReviewCycle(
                                organisationId = organisationId,
                                reviewTypeId = reviewTypeId,
                                reviewToId = reviewToId,
                                reviewFromId = reviewFromId,
                                reviewCycleId = reviewCycleId,
                                managerReviewDraft = managerReviewDraft,
                                managerReviewPublished = managerReviewPublished,
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

    @Operation(summary = "Get my manager review list")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = MyManagerReviewCycleResponse::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/my-manager-review")
    fun fetchMyManagerReviewCycle(
        organisationId: Long,
        reviewTypeId: Int,
        reviewToId: Long,
        reviewFromId: List<Int>,
        reviewCycleId: List<Int>,
        page: Int?,
        limit: Int?,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    MyManagerReviewCycleResponse(
                        totalManagerReviewCycles =
                            reviewCycleService.countMyManagerReviewCycle(
                                organisationId,
                                reviewTypeId,
                                reviewToId,
                                reviewFromId,
                                reviewCycleId,
                            ),
                        myManagerReviewCycles =
                            reviewCycleService.fetchMyManagerReviewCycle(
                                organisationId = organisationId,
                                reviewTypeId = reviewTypeId,
                                reviewToId = reviewToId,
                                reviewFromId = reviewFromId,
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

    @Operation(summary = "Get all check-in with manager list")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = CheckInWithManager::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/check-in-with-manager")
    fun fetchCheckInWithManager(
        organisationId: Long,
        managerId: List<Int>,
        reviewCycleId: List<Int>,
        reviewToId: List<Int>,
        teamId: List<Int>,
        selfReviewDraft: Boolean?,
        selfReviewPublished: Boolean?,
        firstManagerReviewDraft: Boolean?,
        firstManagerReviewPublished: Boolean?,
        secondManagerReviewDraft: Boolean?,
        secondManagerReviewPublished: Boolean?,
        checkInDraft: Boolean?,
        checkInPublished: Boolean?,
        sortRating: String?,
        filterRatingId: Int,
        page: Int?,
        limit: Int?,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    CheckInWithManager(
                        totalCheckInWithManager =
                            reviewCycleService.countCheckInWithManager(
                                CheckInWithManagerParams(
                                    organisationId = organisationId,
                                    managerId = managerId,
                                    reviewCycleId = reviewCycleId,
                                    reviewToId = reviewToId,
                                    teamId = teamId,
                                    selfReviewDraft = selfReviewDraft,
                                    selfReviewPublish = selfReviewPublished,
                                    firstManagerReviewDraft = firstManagerReviewDraft,
                                    firstManagerReviewPublish = firstManagerReviewPublished,
                                    secondManagerReviewDraft = secondManagerReviewDraft,
                                    secondManagerReviewPublish = secondManagerReviewPublished,
                                    checkInDraft = checkInDraft,
                                    checkInPublished = checkInPublished,
                                    filterRatingId = filterRatingId,
                                ),
                            ),
                        checkInWithManagers =
                            reviewCycleService.fetchCheckInWithManager(
                                CheckInWithManagerParams(
                                    organisationId = organisationId,
                                    managerId = managerId,
                                    reviewCycleId = reviewCycleId,
                                    reviewToId = reviewToId,
                                    teamId = teamId,
                                    selfReviewDraft = selfReviewDraft,
                                    selfReviewPublish = selfReviewPublished,
                                    firstManagerReviewDraft = firstManagerReviewDraft,
                                    firstManagerReviewPublish = firstManagerReviewPublished,
                                    secondManagerReviewDraft = secondManagerReviewDraft,
                                    secondManagerReviewPublish = secondManagerReviewPublished,
                                    checkInDraft = checkInDraft,
                                    checkInPublished = checkInPublished,
                                    filterRatingId = filterRatingId,
                                ),
                                sortRating = sortRating,
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

    @Operation(summary = "Get review timeline data")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            content = [Content(schema = Schema(type = "array", implementation = ReviewCycleTimeline::class))],
        ),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/timeline")
    fun fetchData(
        organisationId: Long,
        reviewToId: Long,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = reviewCycleService.fetchReviewCycleData(organisationId, reviewToId),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Get active review cycle")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = ReviewCycle::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/active")
    fun fetchActiveReviewCycle(organisationId: Long): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = reviewCycleService.fetchActiveReviewCycle(organisationId),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
}

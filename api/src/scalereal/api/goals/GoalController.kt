package scalereal.api.goals

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Patch
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import jakarta.inject.Singleton
import scalereal.api.common.ErrorMessage
import scalereal.api.common.Response
import scalereal.api.common.ResponseType
import scalereal.api.util.getAuthenticatedUser
import scalereal.core.goals.GoalService
import scalereal.core.models.GoalProgress
import scalereal.core.models.GoalProgressInfo
import scalereal.core.models.GoalType
import scalereal.core.models.GoalTypeInfo
import scalereal.core.models.domain.CreateGoalRequest
import scalereal.core.models.domain.CreateGoalResponse
import scalereal.core.models.domain.Goal
import scalereal.core.models.domain.GoalGroup
import scalereal.core.models.domain.GoalListResponse
import scalereal.core.modules.Modules
import scalereal.core.roles.RoleService
import java.lang.Exception

@Tag(name = "Goal")
@Controller(value = "/api/goals")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Singleton
class GoalController(
    private val goalService: GoalService,
) {
    @Inject
    lateinit var roleService: RoleService
    private val moduleName = Modules.TEAM_GOALS.moduleName

    @Operation(
        summary = "Create goal",
        requestBody =
            RequestBody(
                required = true,
                content = [Content(schema = Schema(implementation = CreateGoalRequest::class))],
            ),
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "Goal created successfully",
            content = [Content(schema = Schema(implementation = CreateGoalResponse::class))],
        ),
    )
    @Post("/")
    fun create(
        createGoalRequest: CreateGoalRequest,
        authentication: Authentication,
    ): HttpResponse<Any> =
        try {
            val authenticatedUser = getAuthenticatedUser(authentication)
            Response(
                ResponseType.CREATED,
                "",
                body = goalService.create(createGoalRequest, authenticatedUser),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Get previous three review cycles goals")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = GoalGroup::class))]),
    )
    @Get("/previous-three-cycles")
    fun fetchGoal(
        organisationId: Long,
        reviewToId: Long,
        reviewCycleId: Long,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    goalService.fetchGoal(
                        organisationId = organisationId,
                        reviewToId = reviewToId,
                        reviewCycleId = reviewCycleId,
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Update goal progress")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = Goal::class))]),
        ApiResponse(responseCode = "400", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Patch("/{id}/progress")
    fun update(
        @PathVariable id: Long,
        progressId: Int,
        authentication: Authentication,
    ): HttpResponse<Any> =
        try {
            val authenticatedUser = getAuthenticatedUser(authentication)
            Response(
                ResponseType.SUCCESS,
                "",
                body = goalService.updateGoalProgress(id, progressId, authenticatedUser),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Update goal description and type")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = Goal::class))]),
        ApiResponse(responseCode = "400", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Patch("/{id}/details")
    fun updateDetails(
        @PathVariable id: Long,
        description: String?,
        typeId: Int?,
        authentication: Authentication,
    ): HttpResponse<Any> =
        try {
            val actionBy = getAuthenticatedUser(authentication)
            Response(
                ResponseType.SUCCESS,
                "",
                body = goalService.updateGoalDetails(id, description, typeId, actionBy),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Get goal progress list")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            content = [Content(schema = Schema(type = "array", implementation = GoalProgressInfo::class))],
        ),
    )
    @Get("/progress-list")
    fun fetchProgressList(): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = GoalProgress.getProgressListWithId(),
            )
        } catch (e: kotlin.Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Get all goals")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = GoalGroup::class))]),
    )
    @Get("/")
    fun fetchGoals(
        organisationId: Long,
        progressId: List<Int>,
        typeId: List<Int>,
        reviewCycleId: List<Int>,
        page: Int?,
        limit: Int?,
        authentication: Authentication,
    ): HttpResponse<Any> =
        try {
            val actionBy = getAuthenticatedUser(authentication)
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    GoalListResponse(
                        totalGoals =
                            goalService.countAllGoals(
                                listOf(actionBy.toInt()),
                                organisationId,
                                progressId,
                                typeId,
                                reviewCycleId,
                            ),
                        goals =
                            goalService.fetchAllGoals(
                                listOf(actionBy.toInt()),
                                organisationId,
                                progressId,
                                typeId,
                                reviewCycleId,
                                page = page ?: 1,
                                limit = limit ?: Int.MAX_VALUE,
                            ),
                    ),
            )
        } catch (e: kotlin.Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Get all team goals")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = GoalGroup::class))]),
    )
    @Get("/team")
    fun fetchTeamGoals(
        assignedTo: List<Int>,
        organisationId: Long,
        progressId: List<Int>,
        typeId: List<Int>,
        reviewCycleId: List<Int>,
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
                "",
                body =
                    GoalListResponse(
                        totalGoals =
                            goalService.countAllGoals(
                                assignedTo,
                                organisationId,
                                progressId,
                                typeId,
                                reviewCycleId,
                            ),
                        goals =
                            goalService.fetchAllGoals(
                                assignedTo,
                                organisationId,
                                progressId,
                                typeId,
                                reviewCycleId,
                                page = page ?: 1,
                                limit = limit ?: Int.MAX_VALUE,
                            ),
                    ),
            )
        } catch (e: kotlin.Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Operation(summary = "Get goal type list")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            content = [Content(schema = Schema(type = "array", implementation = GoalTypeInfo::class))],
        ),
    )
    @Get("/types")
    fun fetchGoalTypeList(): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = GoalType.getGoalTypes(),
            )
        } catch (e: kotlin.Exception) {
            Response(
                ResponseType.SERVER_ERROR,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
}

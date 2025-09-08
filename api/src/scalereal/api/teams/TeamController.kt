package scalereal.api.teams

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
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
import scalereal.core.models.domain.TeamData
import scalereal.core.models.domain.TeamResponse
import scalereal.core.models.domain.UserActivityData
import scalereal.core.modules.Modules
import scalereal.core.roles.RoleService
import scalereal.core.teams.DefaultTeam
import scalereal.core.teams.Team
import scalereal.core.teams.TeamService

@Tag(name = "Team")
@Controller(value = "/api/teams")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Singleton
class TeamController(
    private val teamService: TeamService,
) {
    @Inject
    lateinit var roleService: RoleService

    private val moduleName = Modules.TEAMS.moduleName

    @Operation(summary = "Create team")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "400", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Produces(MediaType.APPLICATION_JSON)
    @Post("/")
    fun create(
        teams: List<TeamData>,
        actionBy: Long,
        ipAddress: String,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions = roleService.hasPermission(teams[0].organisationId, authentication.roles, moduleName)
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
                    teamService.create(
                        teams,
                        UserActivityData(
                            actionBy = actionBy,
                            ipAddress = ipAddress,
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

    @Operation(summary = "Get all teams")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = TeamResponse::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/")
    fun fetchAll(
        organisationId: Long,
        searchText: String?,
        departmentId: List<Int>?,
        page: Int?,
        limit: Int?,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    TeamResponse(
                        unlinkedTeamsCount =
                            teamService.getUnlinkedTeamsCount(
                                organisationId,
                            ),
                        totalTeams =
                            teamService.count(
                                organisationId = organisationId,
                                searchText = searchText ?: "",
                                departmentId = departmentId ?: listOf(-99),
                            ),
                        teams =
                            teamService.fetchAll(
                                organisationId = organisationId,
                                searchText = searchText ?: "",
                                departmentId = departmentId ?: listOf(-99),
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

    @Operation(summary = "Edit team")
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
        departmentId: Long,
        teamName: String,
        teamStatus: Boolean,
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
                    teamService.update(
                        organisationId = organisationId,
                        id = id,
                        departmentId = departmentId,
                        teamName = teamName,
                        teamStatus = teamStatus,
                        userActivityData =
                            UserActivityData(
                                actionBy = actionBy,
                                ipAddress = ipAddress,
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

    @Operation(summary = "Get default teams by departmentId")
    @Secured(SecurityRule.IS_ANONYMOUS)
    @Get("/default-setup")
    fun getDefaultTeams(
        @QueryValue departmentId: Int,
    ): HttpResponse<List<DefaultTeam>> {
        val teams = Team.getTeamsByDepartmentId(departmentId)
        return if (teams.isNotEmpty()) {
            HttpResponse.ok(teams)
        } else {
            HttpResponse.notFound()
        }
    }
}

package scalereal.api.userActivity

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
import scalereal.core.models.domain.UserActivityResponse
import scalereal.core.modules.Modules
import scalereal.core.roles.RoleService
import scalereal.core.userActivity.UserActivityService

@Tag(name = "User Activity")
@Controller(value = "api/user-activity")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Singleton
class UserActivityController(
    private val userActivityService: UserActivityService,
) {
    @Inject
    lateinit var roleService: RoleService

    private val moduleName = Modules.USER_ACTIVITY_LOG.moduleName

    @Operation(summary = "Get user activity")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = UserActivityResponse::class))]),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Produces(MediaType.APPLICATION_JSON)
    @Get("/")
    fun fetchUserActivities(
        organisationId: Long,
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
                    UserActivityResponse(
                        totalUserActivities = userActivityService.userActivitiesCount(organisationId),
                        userActivities =
                            userActivityService.fetchUserActivities(
                                organisationId,
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
    }
}

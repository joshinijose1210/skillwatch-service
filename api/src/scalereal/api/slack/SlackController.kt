package scalereal.api.slack

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Patch
import io.micronaut.http.annotation.Post
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
import scalereal.core.modules.Modules
import scalereal.core.roles.RoleService
import scalereal.core.slack.SlackService

@Tag(name = "Slack Integration")
@Controller(value = "/api/slack")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Singleton
class SlackController(
    private val slackService: SlackService,
) {
    @Inject
    lateinit var roleService: RoleService

    @Operation(summary = "Get slack connected status")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(type = "boolean"))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/is-connected")
    fun isSlackConnected(organisationId: Long): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                body = slackService.isSlackConnected(organisationId),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Disconnect slack")
    @Patch
    fun disconnectSlack(
        organisationId: Long,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions =
            roleService.hasPermission(
                organisationId = organisationId,
                roleName = authentication.roles,
                moduleName = Modules.INTEGRATIONS.moduleName,
            )
        if (!permissions.edit) {
            return Response(
                ResponseType.FORBIDDEN,
                body = ErrorMessage("Access Denied! You do not have permission to access this route."),
            ).getHttpResponse()
        }
        return try {
            Response(
                ResponseType.SUCCESS,
                body = slackService.disconnectSlack(organisationId),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Operation(summary = "Connect slack")
    @Post("/access-token")
    fun generateAccessToken(
        organisationId: Long,
        code: String,
        state: String,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions =
            roleService.hasPermission(
                organisationId = organisationId,
                roleName = authentication.roles,
                moduleName = Modules.INTEGRATIONS.moduleName,
            )
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
                body = slackService.generateAccessToken(organisationId, code, state),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }
}

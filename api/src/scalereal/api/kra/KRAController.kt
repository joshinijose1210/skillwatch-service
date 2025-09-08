package scalereal.api.kra

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Put
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
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
import scalereal.core.exception.KRANotFoundException
import scalereal.core.kra.KRAService
import scalereal.core.models.domain.GetAllKRAResponse
import scalereal.core.models.domain.UpdateKRAWeightageRequest
import scalereal.core.models.domain.UserActivityData
import scalereal.core.modules.Modules
import scalereal.core.roles.RoleService

@Tag(name = "KRA")
@Controller(value = "/api/kra")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Singleton
class KRAController(
    private val kraService: KRAService,
) {
    @Inject
    lateinit var roleService: RoleService

    private val moduleName = Modules.KRAs.moduleName

    @Operation(summary = "Get All KRA")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            content = [Content(array = ArraySchema(schema = Schema(implementation = GetAllKRAResponse::class)))],
        ),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/")
    fun getAllKRAs(organisationId: Long): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = kraService.getAllKRAs(organisationId = organisationId),
            )
        } catch (e: KRANotFoundException) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Update KRA weightage")
    @Put("/")
    fun updateKRAWeightage(
        organisationId: Long,
        updateKRAWeightageRequest: List<UpdateKRAWeightageRequest>,
        actionBy: Long,
        ipAddress: String,
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
                    kraService.updateKRAWeightage(
                        organisationId = organisationId,
                        updateKRAWeightageRequest = updateKRAWeightageRequest,
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
}

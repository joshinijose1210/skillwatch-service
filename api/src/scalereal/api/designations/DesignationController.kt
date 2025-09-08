package scalereal.api.designations

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
import scalereal.core.designations.DefaultDesignation
import scalereal.core.designations.Designation
import scalereal.core.designations.DesignationService
import scalereal.core.exception.DesignationAlreadyExistsException
import scalereal.core.models.domain.DesignationData
import scalereal.core.models.domain.DesignationResponse
import scalereal.core.models.domain.UserActivityData
import scalereal.core.modules.Modules
import scalereal.core.roles.RoleService

@Tag(name = "Designation")
@Controller(value = "api/designation")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Singleton
class DesignationController(
    private val designationService: DesignationService,
) {
    @Inject
    lateinit var roleService: RoleService

    private val moduleName = Modules.DESIGNATIONS.moduleName

    @Operation(summary = "Create designation")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "400", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Produces(MediaType.APPLICATION_JSON)
    @Post("/")
    fun create(
        designations: List<DesignationData>,
        actionBy: Long,
        ipAddress: String,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions = roleService.hasPermission(designations[0].organisationId, authentication.roles, moduleName)
        if (!permissions.edit) {
            return Response(
                status = ResponseType.FORBIDDEN,
                body = ErrorMessage(errorMessage = "Access Denied! You do not have permission to access this route."),
            ).getHttpResponse()
        }
        return try {
            Response(
                status = ResponseType.SUCCESS,
                message = "",
                body =
                    designationService.create(
                        designations = designations,
                        userActivityData =
                            UserActivityData(
                                actionBy = actionBy,
                                ipAddress = ipAddress,
                            ),
                    ),
            )
        } catch (e: DesignationAlreadyExistsException) {
            Response(
                status = ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        } catch (e: Exception) {
            Response(
                status = ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Operation(summary = "Get all designations")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = DesignationResponse::class))]),
        ApiResponse(responseCode = "400", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/")
    fun fetchAll(
        organisationId: Long,
        searchText: String?,
        departmentId: List<Int>?,
        teamId: List<Int>?,
        page: Int?,
        limit: Int?,
    ): HttpResponse<Any> =
        try {
            Response(
                status = ResponseType.SUCCESS,
                message = "",
                body =
                    DesignationResponse(
                        unlinkedDesignationsCount =
                            designationService.unlinkedDesignationsCount(
                                organisationId = organisationId,
                            ),
                        totalDesignation =
                            designationService.count(
                                organisationId = organisationId,
                                departmentId = departmentId ?: listOf(-99),
                                teamId = teamId ?: listOf(-99),
                                searchText = searchText ?: "",
                            ),
                        designations =
                            designationService.fetchAll(
                                organisationId = organisationId,
                                searchText = searchText ?: "",
                                departmentId = departmentId ?: listOf(-99),
                                teamId = teamId ?: listOf(-99),
                                page = page ?: 1,
                                limit = limit ?: Int.MAX_VALUE,
                            ),
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Edit designation")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Put("/{id}")
    fun update(
        organisationId: Long,
        departmentId: Long,
        teamId: Long,
        id: Long,
        designationName: String,
        status: Boolean,
        actionBy: Long,
        ipAddress: String,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions = roleService.hasPermission(organisationId, authentication.roles, moduleName)
        if (!permissions.edit) {
            return Response(
                status = ResponseType.FORBIDDEN,
                body = ErrorMessage(errorMessage = "Access Denied! You do not have permission to access this route."),
            ).getHttpResponse()
        }
        return try {
            Response(
                status = ResponseType.SUCCESS,
                message = "",
                body =
                    designationService.update(
                        organisationId = organisationId,
                        departmentId = departmentId,
                        teamId = teamId,
                        id = id,
                        designationName = designationName,
                        status = status,
                        UserActivityData(
                            actionBy = actionBy,
                            ipAddress = ipAddress,
                        ),
                    ),
            )
        } catch (e: Exception) {
            Response(
                status = ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Operation(summary = "Get default designations by teamId")
    @Secured(SecurityRule.IS_ANONYMOUS)
    @Get("/default-setup")
    fun getDesignations(
        @QueryValue teamId: Int,
    ): HttpResponse<List<DefaultDesignation>> {
        val designations = Designation.getDesignationsByTeamId(teamId)
        return if (designations.isNotEmpty()) {
            HttpResponse.ok(designations)
        } else {
            HttpResponse.notFound()
        }
    }
}

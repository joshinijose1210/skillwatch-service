package scalereal.api.departments

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
import scalereal.core.departments.DefaultDepartment
import scalereal.core.departments.Department
import scalereal.core.departments.DepartmentService
import scalereal.core.models.domain.DepartmentData
import scalereal.core.models.domain.DepartmentResponse
import scalereal.core.models.domain.UserActivityData
import scalereal.core.modules.Modules
import scalereal.core.roles.RoleService

@Tag(name = "Department")
@Controller(value = "/api/departments")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Singleton
class DepartmentController(
    private val departmentService: DepartmentService,
) {
    @Inject
    lateinit var roleService: RoleService

    private val moduleName = Modules.DEPARTMENTS.moduleName

    @Operation(summary = "Create department")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "400", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Produces(MediaType.APPLICATION_JSON)
    @Post("/")
    fun create(
        departments: List<DepartmentData>,
        actionBy: Long,
        ipAddress: String,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions = roleService.hasPermission(departments[0].organisationId, authentication.roles, moduleName)
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
                    departmentService.create(
                        departments,
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

    @Operation(summary = "Get all departments")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = DepartmentResponse::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/")
    fun fetchAll(
        organisationId: Long,
        searchText: String?,
        page: Int?,
        limit: Int?,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    DepartmentResponse(
                        totalDepartments =
                            departmentService.departmentCount(
                                organisationId = organisationId,
                                searchText = searchText ?: "",
                            ),
                        departments =
                            departmentService.fetchAll(
                                organisationId = organisationId,
                                searchText = searchText ?: "",
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

    @Operation(summary = "Edit department")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "400", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Put("/{id}")
    fun update(
        organisationId: Long,
        id: Long,
        departmentName: String,
        departmentStatus: Boolean,
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
                    departmentService.update(
                        organisationId = organisationId,
                        departmentId = id,
                        departmentName = departmentName,
                        departmentStatus = departmentStatus,
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

    @Operation(summary = "Get default departments")
    @Secured(SecurityRule.IS_ANONYMOUS)
    @Get("/default-setup")
    fun getDefaultDepartments(): HttpResponse<List<DefaultDepartment>> = HttpResponse.ok(Department.getDefaults())
}

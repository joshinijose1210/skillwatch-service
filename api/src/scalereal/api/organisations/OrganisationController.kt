package scalereal.api.organisations

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Part
import io.micronaut.http.annotation.Patch
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.multipart.CompletedFileUpload
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
import scalereal.api.common.ErrorResponse
import scalereal.api.common.Response
import scalereal.api.common.ResponseType
import scalereal.core.models.AccessType
import scalereal.core.models.domain.Domain
import scalereal.core.models.domain.OrganisationDetails
import scalereal.core.models.domain.OrganisationDomains
import scalereal.core.models.domain.OrganisationResponse
import scalereal.core.models.domain.OrganisationSettings
import scalereal.core.modules.Modules
import scalereal.core.organisations.OrganisationService
import scalereal.core.roles.RoleService
import kotlin.Exception

@Tag(name = "Organisation")
@Controller(value = "/api/organisation")
@Secured(SecurityRule.IS_ANONYMOUS)
@Singleton
class OrganisationController(
    private val organisationService: OrganisationService,
) {
    @Inject
    lateinit var roleService: RoleService

    private val moduleName = Modules.COMPANY_INFORMATION.moduleName

    @Operation(summary = "Create organisation")
    @Post("/")
    fun createOrganisation(
        userEmailId: String,
        organisationName: String,
        organisationSize: Int,
        contactNo: String,
        departmentName: String,
        teamName: String,
        designationName: String,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    organisationService.createOrganisation(
                        userEmailId = userEmailId,
                        organisationName = organisationName,
                        organisationSize = organisationSize,
                        contactNo = contactNo,
                        departmentName = departmentName,
                        teamName = teamName,
                        designationName = designationName,
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Edit organisation details")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Put("/{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    fun update(
        id: Long,
        organisationName: String,
        contactNo: String,
        timeZone: String,
        @Part("organisationLogo") organisationLogo: CompletedFileUpload?,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions = roleService.hasPermission(organisationId = id, authentication.roles, moduleName)
        if (!permissions.edit) {
            return Response(
                ResponseType.FORBIDDEN,
                body = ErrorMessage(ErrorResponse.FORBIDDEN),
            ).getHttpResponse()
        }
        return try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    organisationService.update(
                        id = id,
                        organisationName = organisationName,
                        contactNo = contactNo,
                        timeZone = timeZone,
                        organisationLogo = organisationLogo,
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Operation(summary = "Get organisation details")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = OrganisationDetails::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Get("/")
    fun getOrganisationDetails(id: Long): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    organisationService.getOrganisationDetails(
                        id = id,
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Get organisation logo")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Get("/logo")
    fun getOrganisationLogo(id: Long): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = organisationService.getOrganisationLogo(id),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Delete organisation logo")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Patch("/logo/{id}")
    fun deleteOrganisationLogo(
        id: Long,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions = roleService.hasPermission(organisationId = id, authentication.roles, moduleName)
        if (!permissions.edit) {
            return Response(
                ResponseType.FORBIDDEN,
                body = ErrorMessage(ErrorResponse.FORBIDDEN),
            ).getHttpResponse()
        }
        return try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = organisationService.deleteOrganisationLogo(id),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Operation(summary = "Get allowed domains")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            content = [Content(schema = Schema(type = "array", implementation = OrganisationDomains::class))],
        ),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Get("/domains")
    fun getDomainDetails(
        id: Long,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions = roleService.hasPermission(organisationId = id, authentication.roles, Modules.SETTINGS.moduleName)
        if (!permissions.view) {
            return Response(
                ResponseType.FORBIDDEN,
                body = ErrorMessage(ErrorResponse.FORBIDDEN),
            ).getHttpResponse()
        }
        return try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    organisationService.getAllowedDomains(
                        id = id,
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Operation(summary = "Edit allowed domains")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Post("/domains")
    fun saveAllowedDomains(
        organisationId: Long,
        domains: List<Domain>,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions =
            roleService.hasPermission(
                organisationId = organisationId,
                authentication.roles,
                Modules.SETTINGS.moduleName,
            )
        if (!permissions.edit) {
            return Response(
                ResponseType.FORBIDDEN,
                body = ErrorMessage(ErrorResponse.FORBIDDEN),
            ).getHttpResponse()
        }
        return try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = organisationService.saveAllowedDomains(organisationId, domains),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Operation(summary = "Get all organisations")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = OrganisationResponse::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Get("/fetchAll")
    fun getAllOrganisationDetails(
        page: Int?,
        limit: Int?,
        authentication: Authentication,
    ): HttpResponse<Any> {
        if (!(authentication.attributes[AccessType.SUPER_ADMIN.toString()] as Boolean)) {
            return Response(
                ResponseType.FORBIDDEN,
                body = ErrorMessage(ErrorResponse.FORBIDDEN),
            ).getHttpResponse()
        }
        return try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    OrganisationResponse(
                        totalCount = organisationService.getAllOrganisationCount(),
                        users =
                            organisationService.getAllOrganisation(
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

    @Operation(
        summary = "Edit general settings of organisation",
        description = "Update manager review mandatory and anonymous suggestions allowed settings",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Put("/general-settings")
    fun editGeneralSettings(
        organisationId: Long,
        isManagerReviewMandatory: Boolean,
        isAnonymousSuggestionAllowed: Boolean,
        isBiWeeklyFeedbackReminderEnabled: Boolean,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions =
            roleService.hasPermission(
                organisationId = organisationId,
                authentication.roles,
                Modules.SETTINGS.moduleName,
            )
        if (!permissions.edit) {
            return Response(
                ResponseType.FORBIDDEN,
                body = ErrorMessage(ErrorResponse.FORBIDDEN),
            ).getHttpResponse()
        }
        return try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    organisationService.editGeneralSettings(
                        organisationId,
                        isManagerReviewMandatory,
                        isAnonymousSuggestionAllowed,
                        isBiWeeklyFeedbackReminderEnabled,
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Operation(
        summary = "Get general settings details of organisation",
        description = "Get manager review mandatory and anonymous suggestions allowed settings",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = OrganisationSettings::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Get("/general-settings")
    fun getGeneralSettings(organisationId: Long): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = organisationService.getGeneralSettings(organisationId),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(
        summary = "Update organisation's time-zone",
        description = "Update organisation's time-zone",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Put("/time-zone")
    fun updateTimeZone(
        organisationId: Long,
        timeZone: String,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions =
            roleService.hasPermission(
                organisationId = organisationId,
                authentication.roles,
                Modules.SETTINGS.moduleName,
            )
        if (!permissions.edit) {
            return Response(
                ResponseType.FORBIDDEN,
                body = ErrorMessage(ErrorResponse.FORBIDDEN),
            ).getHttpResponse()
        }
        return try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    organisationService.updateTimeZone(
                        organisationId,
                        timeZone,
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

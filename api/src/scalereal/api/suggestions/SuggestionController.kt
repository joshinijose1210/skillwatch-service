package scalereal.api.suggestions

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Patch
import io.micronaut.http.annotation.Post
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
import scalereal.api.common.ErrorResponse
import scalereal.api.common.Response
import scalereal.api.common.ResponseType
import scalereal.core.models.Constants.USER_ID_TOKEN_CLAIM
import scalereal.core.models.SuggestionProgress
import scalereal.core.models.SuggestionProgressInfo
import scalereal.core.models.domain.SuggestionResponse
import scalereal.core.modules.Modules
import scalereal.core.roles.RoleService
import scalereal.core.suggestions.SuggestionService

@Tag(name = "Suggestion Box")
@Controller(value = "/api/suggestion")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Singleton
class SuggestionController(
    private val suggestionService: SuggestionService,
) {
    @Inject
    lateinit var roleService: RoleService

    @Operation(summary = "Add Suggestion")
    @Post("/")
    fun create(
        suggestion: String,
        markdownText: String,
        suggestedById: Long,
        isDraft: Boolean,
        isAnonymous: Boolean,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.CREATED,
                "",
                body =
                    suggestionService.create(
                        suggestion = suggestion,
                        markdownText = markdownText,
                        suggestedById = suggestedById,
                        isDraft = isDraft,
                        isAnonymous = isAnonymous,
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Edit Suggestion")
    @Put("/{id}")
    fun update(
        id: Long,
        suggestion: String,
        markdownText: String,
        suggestedById: Long,
        isDraft: Boolean,
        isAnonymous: Boolean,
    ): HttpResponse<Any> =
        try {
            Response(
                status = ResponseType.SUCCESS,
                message = "",
                body =
                    suggestionService.update(
                        id = id,
                        suggestion = suggestion,
                        markdownText = markdownText,
                        suggestedById = suggestedById,
                        isDraft = isDraft,
                        isAnonymous = isAnonymous,
                    ),
            )
        } catch (e: java.lang.Exception) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Get Suggestion List")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            content = [Content(schema = Schema(implementation = SuggestionResponse::class))],
        ),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/")
    fun fetch(
        organisationId: Long,
        suggestionById: List<Int>,
        reviewCycleId: List<Int>,
        isDraft: List<Boolean>,
        progressIds: List<Int>?,
        page: Int?,
        limit: Int?,
        sortBy: String?,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions =
            roleService.hasPermission(
                organisationId = organisationId,
                roleName = authentication.roles,
                moduleName = Modules.RECEIVED_SUGGESTIONS.moduleName,
            )
        if (!permissions.view && suggestionById == listOf(-99)) {
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
                    SuggestionResponse(
                        totalSuggestions =
                            suggestionService.countAllSuggestion(
                                organisationId = organisationId,
                                suggestionById = suggestionById,
                                reviewCycleId = reviewCycleId,
                                isDraft = isDraft,
                                progressIds = progressIds ?: listOf(-99),
                            ),
                        suggestions =
                            suggestionService.fetch(
                                organisationId = organisationId,
                                suggestionById = suggestionById,
                                reviewCycleId = reviewCycleId,
                                isDraft = isDraft,
                                progressIds = progressIds ?: listOf(-99),
                                page = page ?: 1,
                                limit = limit ?: Int.MAX_VALUE,
                                sortBy = sortBy ?: "dateDesc",
                            ),
                    ),
            )
        } catch (e: java.lang.Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Operation(summary = "Get suggestion progress list")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            content = [Content(schema = Schema(type = "array", implementation = SuggestionProgressInfo::class))],
        ),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/progress-list")
    fun fetchProgressList(): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = SuggestionProgress.getProgressListWithId(),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Patch("/progress/{suggestionId}")
    @Operation(summary = "Update suggestion progress")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    fun editSuggestionProgress(
        suggestionId: Long,
        progressId: Int,
        comment: String?,
        markDownComment: String?,
        organisationId: Long,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions =
            roleService.hasPermission(
                organisationId = organisationId,
                roleName = authentication.roles,
                moduleName = Modules.RECEIVED_SUGGESTIONS.moduleName,
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
                    suggestionService.editSuggestionProgress(
                        suggestionId = suggestionId,
                        progressId = progressId,
                        comment = comment,
                        markDownComment = markDownComment,
                        commentedBy = authentication.attributes[USER_ID_TOKEN_CLAIM] as Long,
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Operation(summary = "Get all pending suggestions count")
    @Get("/pending-count")
    fun getCount(
        organisationId: Long,
        reviewCycleId: List<Int>,
    ): HttpResponse<Any> =
        try {
            val count = suggestionService.getAllPendingSuggestionsCount(organisationId, reviewCycleId)
            HttpResponse.ok(mapOf("pendingSuggestions" to count))
        } catch (e: Exception) {
            HttpResponse.serverError(ErrorMessage(e.message.toString()))
        }
}

package scalereal.api.slack

import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Singleton
import scalereal.core.slack.SlackAppService

@Tag(name = "Slack App")
@Secured(SecurityRule.IS_ANONYMOUS)
@Singleton
@Controller("/slack")
class SlackAppController(
    private val slackAppService: SlackAppService,
) {
    @Operation(summary = "Handle all slack app events")
    @Post(value = "/events", consumes = [MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON])
    @Throws(java.lang.Exception::class)
    fun slackEvents(request: HttpRequest<String>): MutableHttpResponse<out Any?>? = slackAppService.processSlackRequest(request)

    @Operation(summary = "Open add feedback modal")
    @Post(value = "/command/add-feedback", consumes = [MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON])
    fun slackAddFeedback(request: HttpRequest<String>) = slackAppService.processAddFeedbackCommand(request)

    @Operation(summary = "Open draft feedback list")
    @Post(value = "/command/draft-feedback", consumes = [MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON])
    fun slackDraftFeedback(request: HttpRequest<String>) = slackAppService.processEditFeedbackCommand(request)

    @Operation(summary = "Open request feedback options")
    @Post(value = "/command/request-feedback", consumes = [MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON])
    fun slackRequestFeedback(request: HttpRequest<String>) = slackAppService.processRequestFeedbackCommand(request)

    @Operation(summary = "Open add company suggestion modal")
    @Post(value = "/command/add-company-suggestion", consumes = [MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON])
    fun slackAddCompanySuggestion(request: HttpRequest<String>) = slackAppService.processAddCompanySuggestionCommand(request)

    @Operation(summary = "View request feedback list")
    @Post(value = "/command/request-received", consumes = [MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON])
    fun slackViewRequestFeedback(request: HttpRequest<String>) = slackAppService.processViewRequestFeedbackCommand(request)
}

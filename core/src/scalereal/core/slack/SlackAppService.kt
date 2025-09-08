package scalereal.core.slack

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeRepository
import scalereal.core.models.domain.Employee
import scalereal.core.models.domain.PayloadInfo
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.Exception

@Singleton
class SlackAppService(
    private val employeeRepository: EmployeeRepository,
    private val slackRepository: SlackRepository,
    private val slackPayload: SlackPayloadData,
    private val slackAddFeedback: SlackAddFeedback,
    private val slackEditFeedback: SlackEditFeedback,
    private val slackRequestFeedback: SlackRequestFeedback,
    private val slackAppHomeView: SlackAppHomeView,
    private val addCompanySuggestion: AddCompanySuggestion,
    private val viewRequestFeedback: ViewRequestFeedback,
) {
    fun processSlackRequest(request: HttpRequest<String>): MutableHttpResponse<out Any?>? {
        val requestBody: String = request.body.get()
        val appHomePayload: String = URLDecoder.decode(requestBody, StandardCharsets.UTF_8.toString())
        val type = slackPayload.extractTypeFromPayload(requestBody)
        return when (type) {
            "url_verification" -> HttpResponse.ok(requestBody)
            else -> processHomeViewEvent(appHomePayload)
        }
    }

    fun processHomeViewEvent(appHomePayload: String): MutableHttpResponse<out Any?>? {
        val event = slackPayload.extractEventType(appHomePayload)

        if (event == "app_home_opened") {
            val workspaceId = slackPayload.extractWorkspaceIdFromHomePayload(appHomePayload)
            workspaceId?.let { id ->
                val accessToken = slackRepository.getAccessToken(id)
                val userId = slackPayload.extractUserId(appHomePayload)
                accessToken?.let { slackAppHomeView.publishHomeView(userId, it) }
            }
        }

        val decodedPayload = appHomePayload.substring(8)
        val workspaceId = slackPayload.extractWorkspaceIdFromEventPayload(decodedPayload)
        val accessToken = workspaceId?.let { slackRepository.getAccessToken(it) }
        val payloadInfo = slackPayload.extractInfoFromPayload(decodedPayload)
        val emailId =
            payloadInfo.userId.let {
                accessToken?.let { it1 -> slackPayload.getUserEmail(it, it1) }
            }
        val employeeExists = emailId?.let { employeeRepository.isEmployeeExists(it) }
        val employeeDetail = emailId?.let { employeeRepository.fetchByEmailId(it) }

        return when {
            employeeExists == true && employeeDetail != null -> {
                accessToken?.let { processEvents(decodedPayload, payloadInfo, employeeDetail, it) }
            }
            else -> {
                accessToken?.let { slackAppHomeView.openUserNotFoundForm(payloadInfo.triggerId, it) }
                HttpResponse.ok()
            }
        }
    }

    fun processEvents(
        decodedPayload: String,
        payloadInfo: PayloadInfo,
        employeeDetail: Employee,
        accessToken: String,
    ): MutableHttpResponse<out Any?>? =
        when {
            payloadInfo.actionId == "open_feedback_form" || payloadInfo.actionId == "add_feedback_button" -> {
                slackAddFeedback.openFeedbackForm(accessToken, payloadInfo.triggerId, employeeDetail.organisationId, employeeDetail.id)
                HttpResponse.ok()
            }

            payloadInfo.actionId == "draft_feedback" || payloadInfo.callbackId == "submit_feedback" -> {
                processFeedbackSubmission(decodedPayload, payloadInfo, accessToken)
            }

            payloadInfo.actionId == "back_to_request_options" -> {
                slackRequestFeedback.requestFeedbackViewUpdate(decodedPayload, accessToken)
                HttpResponse.ok()
            }

            payloadInfo.actionId == "back_to_draft_list" -> {
                slackEditFeedback.updateDraftFeedbackListForm(accessToken, decodedPayload, employeeDetail)
                HttpResponse.ok()
            }

            payloadInfo.callbackId == "request_feedback" -> {
                processRequestFeedback(decodedPayload, accessToken)
            }

            payloadInfo.actionId == "request_feedback_button" -> {
                slackRequestFeedback.requestFeedbackOption(payloadInfo.triggerId, accessToken)
                HttpResponse.ok()
            }

            payloadInfo.actionId == "request_feedback_for_self" -> {
                slackRequestFeedback.openRequestFeedback(decodedPayload, employeeDetail, payloadInfo.actionId, accessToken)
                HttpResponse.ok()
            }

            payloadInfo.actionId == "request_feedback_for_team" -> {
                slackRequestFeedback.openRequestFeedback(decodedPayload, employeeDetail, payloadInfo.actionId, accessToken)
                HttpResponse.ok()
            }

            payloadInfo.actionId == "draft_feedback_button" -> {
                slackEditFeedback.openDraftFeedbackListForm(accessToken, payloadInfo.triggerId, employeeDetail)
                HttpResponse.ok()
            }

            payloadInfo.actionId == "get-draft-feedback" -> {
                val feedbackId = slackPayload.getFeedbackIdFromPayload(decodedPayload).toString()
                slackEditFeedback.openDraftFeedbackForm(
                    accessToken,
                    decodedPayload,
                    employeeDetail.organisationId,
                    employeeDetail.id,
                    feedbackId,
                )
                HttpResponse.ok()
            }

            payloadInfo.actionId == "draft_draft_feedback" || payloadInfo.callbackId == "submit_draft_feedback" -> {
                processDraftFeedback(decodedPayload, payloadInfo, accessToken)
            }

            payloadInfo.actionId == "redirect_to_skillwatch" -> {
                HttpResponse.ok()
            }

            payloadInfo.actionId == "suggestion_box_button" -> {
                addCompanySuggestion.openSuggestionForm(accessToken, payloadInfo.triggerId, employeeDetail.organisationId)
                HttpResponse.ok()
            }

            payloadInfo.callbackId == "submit_suggestion" -> {
                processAddCompanySuggestionSubmission(decodedPayload, accessToken)
            }

            payloadInfo.actionId == "view_request_feedback_button" -> {
                viewRequestFeedback.openRequestFeedbackListForm(accessToken, payloadInfo.triggerId, employeeDetail)
                HttpResponse.ok()
            }

            payloadInfo.actionId == "get-request-feedback" -> {
                val requestFeedbackId = slackPayload.getRequestFeedbackIdFromPayload(decodedPayload).toString()
                viewRequestFeedback.openAddRequestFeedbackForm(
                    accessToken,
                    decodedPayload,
                    requestFeedbackId,
                )
                HttpResponse.ok()
            }

            payloadInfo.actionId == "back_to_request_list" -> {
                viewRequestFeedback.updateRequestFeedbackListForm(accessToken, decodedPayload, employeeDetail)
                HttpResponse.ok()
            }

            payloadInfo.actionId == "draft_request_feedback" -> {
                processAddRequestFeedbackSubmission(accessToken, decodedPayload, employeeDetail, true)
            }

            payloadInfo.callbackId == "submit_request_feedback" -> {
                processAddRequestFeedbackSubmission(accessToken, decodedPayload, employeeDetail, false)
            }

            else -> {
                throw Exception("There is no event to process")
            }
        }

    fun processFeedbackSubmission(
        decodedPayload: String,
        payloadInfo: PayloadInfo,
        accessToken: String,
    ): MutableHttpResponse<out Any?> {
        try {
            val errorMap = slackAddFeedback.feedbackAddEvent(decodedPayload, payloadInfo.actionId, payloadInfo.callbackId, accessToken)
            return if (errorMap.isEmpty()) {
                if (payloadInfo.actionId == "draft_feedback") {
                    slackEditFeedback.successDraftForm(accessToken, decodedPayload, payloadInfo.actionId, payloadInfo.callbackId)
                    HttpResponse.ok()
                } else {
                    val updateResponse = slackAddFeedback.successFeedbackForm(payloadInfo.actionId, payloadInfo.callbackId)
                    HttpResponse.ok(updateResponse).contentType(MediaType.APPLICATION_JSON)
                }
            } else {
                HttpResponse.ok()
            }
        } catch (_: Exception) {
            val errorResponse =
                mapOf("response_action" to "errors", "errors" to mapOf("feedback_input" to "Please add more than 50 characters."))
            return HttpResponse.ok(errorResponse).contentType(MediaType.APPLICATION_JSON)
        }
    }

    fun processRequestFeedback(
        decodedPayload: String,
        accessToken: String,
    ): MutableHttpResponse<out Any?> =
        try {
            slackRequestFeedback.requestFeedbackAddEvent(decodedPayload, accessToken)
            val updateResponse = slackRequestFeedback.successRequestFeedbackForm()
            HttpResponse.ok(updateResponse).contentType(MediaType.APPLICATION_JSON)
        } catch (_: Exception) {
            val errorMessage = "Cannot select the same employee in 'Feedback From' and 'Feedback About' both."
            val errorResponse = mapOf("response_action" to "errors", "errors" to mapOf("feedback_to_input" to errorMessage))
            HttpResponse.ok(errorResponse).contentType(MediaType.APPLICATION_JSON)
        }

    fun processDraftFeedback(
        decodedPayload: String,
        payloadInfo: PayloadInfo,
        accessToken: String,
    ): MutableHttpResponse<out Any?> =
        try {
            slackEditFeedback.feedbackUpdateEvent(decodedPayload, payloadInfo.actionId, payloadInfo.callbackId, accessToken)
            if (payloadInfo.actionId == "draft_draft_feedback") {
                slackEditFeedback.successDraftForm(accessToken, decodedPayload, payloadInfo.actionId, payloadInfo.callbackId)
                HttpResponse.ok()
            } else {
                val updateResponse = slackAddFeedback.successFeedbackForm(payloadInfo.actionId, payloadInfo.callbackId)
                HttpResponse.ok(updateResponse).contentType(MediaType.APPLICATION_JSON)
            }
        } catch (_: Exception) {
            val errorResponse =
                mapOf("response_action" to "errors", "errors" to mapOf("feedback_input" to "Please add more than 50 characters."))
            HttpResponse.ok(errorResponse).contentType(MediaType.APPLICATION_JSON)
        }

    fun processAddFeedbackCommand(request: HttpRequest<String>) {
        val commandPayloadInfo = slackPayload.extractInfoFromCommandPayload(request.body.get())
        val workspaceId = slackPayload.extractWorkspaceIdFromCommandPayload(request.body.get())
        val accessToken = workspaceId?.let { slackRepository.getAccessToken(it) }
        val emailId =
            commandPayloadInfo.userId?.let { userId ->
                accessToken?.let { slackPayload.getUserEmail(userId, it) }
            }
        val employeeDetail = emailId?.let { employeeRepository.fetchByEmailId(it) }

        if (employeeDetail != null && accessToken != null) {
            if (employeeRepository.isEmployeeExists(emailId)) {
                slackAddFeedback.openFeedbackForm(
                    accessToken,
                    commandPayloadInfo.triggerId,
                    employeeDetail.organisationId,
                    employeeDetail.id,
                )
            } else {
                slackAppHomeView.openUserNotFoundForm(commandPayloadInfo.triggerId, accessToken)
            }
        }
    }

    fun processEditFeedbackCommand(request: HttpRequest<String>) {
        val commandPayloadInfo = slackPayload.extractInfoFromCommandPayload(request.body.get())
        val workspaceId = slackPayload.extractWorkspaceIdFromCommandPayload(request.body.get())
        val accessToken = workspaceId?.let { slackRepository.getAccessToken(it) }
        val emailId =
            commandPayloadInfo.userId?.let { userId ->
                accessToken?.let { slackPayload.getUserEmail(userId, it) }
            }
        val employeeDetail = emailId?.let { employeeRepository.fetchByEmailId(it) }

        if (employeeDetail != null && accessToken != null) {
            if (employeeRepository.isEmployeeExists(emailId)) {
                slackEditFeedback.openDraftFeedbackListForm(accessToken, commandPayloadInfo.triggerId, employeeDetail)
            } else {
                slackAppHomeView.openUserNotFoundForm(commandPayloadInfo.triggerId, accessToken)
            }
        }
    }

    fun processRequestFeedbackCommand(request: HttpRequest<String>) {
        val commandPayloadInfo = slackPayload.extractInfoFromCommandPayload(request.body.get())
        val workspaceId = slackPayload.extractWorkspaceIdFromCommandPayload(request.body.get())
        val accessToken = workspaceId?.let { slackRepository.getAccessToken(it) }
        val emailId =
            commandPayloadInfo.userId?.let { userId ->
                accessToken?.let { slackPayload.getUserEmail(userId, it) }
            }
        val employeeDetail = emailId?.let { employeeRepository.fetchByEmailId(it) }

        if (employeeDetail != null && accessToken != null) {
            if (employeeRepository.isEmployeeExists(emailId)) {
                slackRequestFeedback.requestFeedbackOption(commandPayloadInfo.triggerId, accessToken)
            } else {
                slackAppHomeView.openUserNotFoundForm(commandPayloadInfo.triggerId, accessToken)
            }
        }
    }

    fun processAddCompanySuggestionSubmission(
        decodedPayload: String,
        accessToken: String,
    ): MutableHttpResponse<out Any?> {
        try {
            val errorMap = addCompanySuggestion.addSuggestionEvent(decodedPayload, accessToken)
            return if (errorMap.isEmpty()) {
                val updateResponse = addCompanySuggestion.successAddSuggestionForm()
                HttpResponse.ok(updateResponse).contentType(MediaType.APPLICATION_JSON)
            } else {
                HttpResponse.ok()
            }
        } catch (e: Exception) {
            val errorResponse =
                mapOf(
                    "response_action" to "errors",
                    "errors" to mapOf("suggestion_input" to e.message.toString()),
                )
            return HttpResponse.ok(errorResponse).contentType(MediaType.APPLICATION_JSON)
        }
    }

    fun processAddCompanySuggestionCommand(request: HttpRequest<String>) {
        val commandPayloadInfo = slackPayload.extractInfoFromCommandPayload(request.body.get())
        val workspaceId = slackPayload.extractWorkspaceIdFromCommandPayload(request.body.get())
        val accessToken = workspaceId?.let { slackRepository.getAccessToken(it) }
        val emailId =
            commandPayloadInfo.userId?.let { userId ->
                accessToken?.let { slackPayload.getUserEmail(userId, it) }
            }
        val employeeDetail = emailId?.let { employeeRepository.fetchByEmailId(it) }

        if (employeeDetail != null && accessToken != null) {
            if (employeeRepository.isEmployeeExists(emailId)) {
                addCompanySuggestion.openSuggestionForm(
                    accessToken,
                    commandPayloadInfo.triggerId,
                    employeeDetail.organisationId,
                )
            } else {
                slackAppHomeView.openUserNotFoundForm(commandPayloadInfo.triggerId, accessToken)
            }
        }
    }

    fun processViewRequestFeedbackCommand(request: HttpRequest<String>) {
        val commandPayloadInfo = slackPayload.extractInfoFromCommandPayload(request.body.get())
        val workspaceId = slackPayload.extractWorkspaceIdFromCommandPayload(request.body.get())
        val accessToken = workspaceId?.let { slackRepository.getAccessToken(it) }
        val emailId =
            commandPayloadInfo.userId?.let { userId ->
                accessToken?.let { slackPayload.getUserEmail(userId, it) }
            }
        val employeeDetail = emailId?.let { employeeRepository.fetchByEmailId(it) }

        if (employeeDetail != null && accessToken != null) {
            if (employeeRepository.isEmployeeExists(emailId)) {
                viewRequestFeedback.openRequestFeedbackListForm(
                    accessToken,
                    commandPayloadInfo.triggerId,
                    employeeDetail,
                )
            } else {
                slackAppHomeView.openUserNotFoundForm(commandPayloadInfo.triggerId, accessToken)
            }
        }
    }

    fun processAddRequestFeedbackSubmission(
        accessToken: String,
        decodedPayload: String,
        employeeDetail: Employee,
        isDraft: Boolean,
    ): MutableHttpResponse<out Any?> {
        try {
            val errorMap = viewRequestFeedback.requestFeedbackSubmission(decodedPayload, employeeDetail, isDraft)
            return if (errorMap.isEmpty()) {
                if (isDraft) {
                    viewRequestFeedback.successRequestFeedbackDraftForm(accessToken, decodedPayload)
                    HttpResponse.ok()
                } else {
                    val updateResponse = viewRequestFeedback.successAddRequestFeedbackForm()
                    HttpResponse.ok(updateResponse).contentType(MediaType.APPLICATION_JSON)
                }
            } else {
                val errorResponse = mapOf("response_action" to "errors", "errors" to errorMap)
                return HttpResponse.ok(errorResponse).contentType(MediaType.APPLICATION_JSON)
            }
        } catch (e: Exception) {
            val errorResponse =
                mapOf(
                    "response_action" to "errors",
                    "errors" to mapOf("input_field" to e.message.toString()),
                )
            return HttpResponse.ok(errorResponse).contentType(MediaType.APPLICATION_JSON)
        }
    }
}

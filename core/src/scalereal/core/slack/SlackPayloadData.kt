package scalereal.core.slack

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.slack.api.Slack
import com.slack.api.methods.request.users.UsersInfoRequest
import com.slack.api.methods.response.users.UsersInfoResponse
import com.slack.api.model.block.composition.OptionObject
import com.slack.api.model.block.composition.PlainTextObject
import io.github.furstenheim.CopyDown
import io.github.furstenheim.OptionsBuilder
import jakarta.inject.Singleton
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import scalereal.core.employees.EmployeeRepository
import scalereal.core.feedbacks.FeedbackRequestRepository
import scalereal.core.feedbacks.FeedbackRequestService
import scalereal.core.feedbacks.FeedbackService
import scalereal.core.feedbacks.FeedbackTypeRepository
import scalereal.core.models.domain.CommandPayloadInfo
import scalereal.core.models.domain.FeedbackData
import scalereal.core.models.domain.FeedbackRequestData
import scalereal.core.models.domain.FeedbackRequestParams
import scalereal.core.models.domain.FeedbackType
import scalereal.core.models.domain.PayloadInfo
import scalereal.core.models.domain.RequestFeedbackFields
import scalereal.core.models.domain.SlackAddSuggestionData
import scalereal.core.models.domain.SlackFeedbackData
import scalereal.core.models.domain.SlackRequestFeedbackData
import scalereal.core.models.domain.UserInfo
import scalereal.core.slack.Constants.LENGTH_VALIDATION_ERROR

@Singleton
class SlackPayloadData(
    private val feedbackRequestService: FeedbackRequestService,
    private val feedbackTypeRepository: FeedbackTypeRepository,
    private val employeeRepository: EmployeeRepository,
    private val feedbackService: FeedbackService,
    private val feedbackRequestRepository: FeedbackRequestRepository,
) {
    fun extractWorkspaceIdFromHomePayload(payload: String): String? {
        val objectMapper = ObjectMapper()
        val rootNode: JsonNode = objectMapper.readTree(payload)
        return rootNode.path("team_id").asText(null)
    }

    fun extractWorkspaceIdFromEventPayload(payload: String): String? {
        val objectMapper = ObjectMapper()
        val rootNode: JsonNode = objectMapper.readTree(payload)
        return rootNode.path("team").path("id").asText(null)
    }

    fun extractWorkspaceIdFromCommandPayload(payload: String): String? =
        payload
            .split("&")
            .map { it.split("=") }
            .find { it[0] == "team_id" }
            ?.getOrNull(1)

    fun extractInfoFromPayload(payload: String): PayloadInfo {
        val objectMapper = ObjectMapper()
        val jsonNode: JsonNode = objectMapper.readTree(payload)

        val actionsNode = jsonNode.get("actions")
        val viewNode = jsonNode.get("view")
        val triggerIdNode = jsonNode.get("trigger_id")
        val userNode = jsonNode.get("user")

        val actionId = actionsNode?.find { it.has("action_id") }?.get("action_id")?.asText() ?: ""
        val callbackId = viewNode?.get("callback_id")?.asText() ?: ""
        val triggerId = triggerIdNode?.asText() ?: ""
        val userId = userNode?.get("id")?.asText() ?: ""

        return PayloadInfo(actionId, callbackId, triggerId, userId)
    }

    fun extractTypeFromPayload(payload: String): String? {
        val objectMapper = ObjectMapper()
        return try {
            val jsonNode: JsonNode = objectMapper.readTree(payload)
            val typeNode = jsonNode.get("type")
            return typeNode?.asText()
        } catch (e: Exception) {
            null
        }
    }

    fun extractInfoFromCommandPayload(payload: String): CommandPayloadInfo {
        val keyValuePairs = payload.split('&')
        val result = mutableMapOf<String, String>()

        for (pair in keyValuePairs) {
            val parts = pair.split('=')
            if (parts.size == 2 && (parts[0] == "trigger_id" || parts[0] == "user_id")) {
                result[parts[0]] = parts[1]
            }
        }
        return CommandPayloadInfo(
            triggerId = result["trigger_id"] ?: "",
            userId = result["user_id"],
        )
    }

    fun extractUserInfoFromEventPayload(payload: String): UserInfo {
        val objectMapper = ObjectMapper()
        val payloadNode: JsonNode = objectMapper.readTree(payload)
        val userNode = payloadNode.get("user")
        val userId = userNode.get("id").asText()
        val username = userNode.get("username").asText()
        return UserInfo(userId, username)
    }

    fun getUserEmail(
        userId: String,
        accessToken: String,
    ): String? {
        val slack = Slack.getInstance()
        val methodsClient = slack.methods(accessToken)
        val request =
            UsersInfoRequest
                .builder()
                .user(userId)
                .build()
        val response: UsersInfoResponse = methodsClient.usersInfo(request)
        return if (response.isOk) {
            response.user.profile.email
        } else {
            null
        }
    }

    fun getFeedbackData(payload: String): SlackFeedbackData {
        val objectMapper = ObjectMapper()
        val payloadNode: JsonNode = objectMapper.readTree(payload)
        var employeeSelectValue = ""
        val feedbackTypeSelectValue: String
        val plainTextInputValue: String

        val viewNode = payloadNode.get("view")
        val stateNode = viewNode.get("state")
        if (stateNode != null) {
            stateNode.get("values").get(0)
            val employeeSelectNode = stateNode.get("values").get("employee_select_input").get("employee_select")
            if (employeeSelectNode?.get("selected_option")!!.get("value") != null) {
                employeeSelectValue = employeeSelectNode.get("selected_option").get("value").asText()
            } else {
                return SlackFeedbackData(
                    feedbackTypeId = 0,
                    feedbackToId = 0,
                    feedback = "",
                    errorsMessage = "Please select employee name.",
                    blockId = "employee_select_input",
                )
            }
        }

        val feedbackTypeSelectNode = stateNode.get("values").get("feedback_type_input").get("feedback_type_select")
        if (feedbackTypeSelectNode?.get("selected_option")!!.get("value") != null) {
            feedbackTypeSelectValue = feedbackTypeSelectNode.get("selected_option").get("value").asText()
        } else {
            return SlackFeedbackData(
                feedbackTypeId = 0,
                feedbackToId = 0,
                feedback = "",
                errorsMessage = "Please select feedback type",
                blockId = "feedback_type_input",
            )
        }

        val plainTextInputNode = stateNode.get("values").get("feedback_input").get("plain_text_input-action3")
        if (plainTextInputNode != null) {
            plainTextInputValue = plainTextInputNode.get("value").asText()
        } else {
            return SlackFeedbackData(
                feedbackTypeId = 0,
                feedbackToId = 0,
                feedback = "",
                errorsMessage = "Please add more than 50 characters",
                blockId = "feedback_input",
            )
        }

        val slackFeedbackData =
            SlackFeedbackData(
                feedbackToId = employeeSelectValue.toLong(),
                feedbackTypeId = feedbackTypeSelectValue.toInt(),
                feedback = plainTextInputValue,
                errorsMessage = null,
                blockId = null,
            )
        return slackFeedbackData
    }

    fun getRequestFeedbackData(payload: String): SlackRequestFeedbackData {
        val objectMapper = jacksonObjectMapper()
        val payloadNode: JsonNode = objectMapper.readTree(payload)

        var feedbackFromSelectValue = emptyList<Long>()
        var feedbackToSelectValues = emptyList<Long>()
        var actionItemSelectValue: Long? = null
        var feedbackRequestDescriptionValue = ""

        val stateNode =
            payloadNode
                .path("view")
                .path("state")
                .path("values")

        val feedbackFromSelectNode =
            stateNode
                .path("feedback_from_input")
                .path("feedback_from_select")
                .path("selected_options")
        if (feedbackFromSelectNode.isArray) {
            feedbackFromSelectValue = feedbackFromSelectNode.map { it.path("value").asText().toLong() }
        }

        val feedbackToSelectNode =
            stateNode
                .path("feedback_to_input")
                .path("feedback_to_select")
                .path("selected_options")
        if (feedbackToSelectNode.isArray) {
            feedbackToSelectValues = feedbackToSelectNode.map { it.path("value").asText().toLong() }
        }

        val actionItemSelectNode =
            stateNode
                .path("action_item_input")
                .path("action_item_select")
                .path("selected_option")
        if (actionItemSelectNode.isObject) {
            actionItemSelectValue = actionItemSelectNode.path("value").asText().toLong()
        }

        val feedbackRequestDescriptionNode =
            stateNode
                .path("feedback_request_description_input")
                .path("feedback_request_description")
        if (feedbackRequestDescriptionNode.isObject) {
            feedbackRequestDescriptionValue = feedbackRequestDescriptionNode.path("value").asText()
        }

        return SlackRequestFeedbackData(
            feedbackFromSelectValue = feedbackFromSelectValue,
            feedbackToSelectValues = feedbackToSelectValues,
            goalSelectValue = actionItemSelectValue,
            feedbackRequestDescriptionValue = feedbackRequestDescriptionValue,
        )
    }

    fun getViewIdFromPayload(decodedPayload: String): String? {
        val objectMapper = ObjectMapper()
        val jsonNode: JsonNode = objectMapper.readTree(decodedPayload)

        var viewId: String? = null
        if (jsonNode.get("view") != null) {
            val viewNode = jsonNode.get("view")
            viewId = viewNode["id"]?.asText()
        }
        return viewId
    }

    fun extractUserId(payload: String): String {
        val objectMapper = ObjectMapper()
        val payloadNode: JsonNode = objectMapper.readTree(payload)
        val userNode = payloadNode.get("event")
        return userNode.get("user").asText()
    }

    fun extractEventType(json: String): String? {
        val objectMapper = ObjectMapper()
        return try {
            val jsonNode = objectMapper.readTree(json)
            return jsonNode.path("event").path("type").asText()
        } catch (e: Exception) {
            null
        }
    }

    fun getFeedbackIdFromPayload(decodedPayload: String): String? {
        val objectMapper = ObjectMapper()
        val jsonNode: JsonNode = objectMapper.readTree(decodedPayload)

        var feedbackId: String? = null
        if (jsonNode.has("actions")) {
            val actionsNode = jsonNode.get("actions")

            if (actionsNode.isArray) {
                for (actionNode in actionsNode) {
                    if (actionNode.has("value")) {
                        feedbackId = actionNode.get("value").asText()
                        break
                    }
                }
            }
        }
        if (feedbackId == null) {
            val viewNode = jsonNode.get("view")
            if (viewNode.has("private_metadata")) {
                feedbackId = viewNode.get("private_metadata").asText()
            } else {
                throw Exception("Feedback ID not found in the payload")
            }
        }
        return feedbackId
    }

    fun getMetadataIdFromPayload(decodedPayload: String): String {
        val objectMapper = ObjectMapper()
        val jsonNode = objectMapper.readTree(decodedPayload)

        jsonNode.get("actions")?.let { actionsNode ->
            if (actionsNode.isArray) {
                for (actionNode in actionsNode) {
                    actionNode.get("value")?.asText()?.let { return it }
                }
            }
        }

        jsonNode.get("view")?.get("private_metadata")?.asText()?.let { metadata ->
            if (metadata.isNotBlank()) return metadata
        }

        throw Exception("Feedback ID not found in the payload")
    }

    fun parsePrivateMetadata(decodedPayload: String): JsonNode {
        val objectMapper = jacksonObjectMapper()
        val jsonNode = objectMapper.readTree(decodedPayload)
        val privateMeta = jsonNode.get("view")?.get("private_metadata")?.asText() ?: "{}"
        return objectMapper.readTree(privateMeta)
    }

    fun getRequestFeedbackIdFromPayload(decodedPayload: String): String {
        val objectMapper = ObjectMapper()
        val jsonNode: JsonNode = objectMapper.readTree(decodedPayload)

        jsonNode.get("actions")?.let { actionsNode ->
            if (actionsNode.isArray) {
                for (actionNode in actionsNode) {
                    actionNode.get("value")?.asText()?.let { return it }
                }
            }
        }

        jsonNode.get("view")?.get("private_metadata")?.asText()?.let { metadata ->
            if (metadata.isNotBlank()) return metadata
        }

        throw Exception("Request Feedback ID not found in the payload")
    }

    fun getEmployees(
        organisationId: Long,
        feedbackFromId: Long,
    ): MutableList<OptionObject> {
        val employees = employeeRepository.getActiveEmployees(organisationId)
        val optionList = mutableListOf<OptionObject>()

        employees
            .filter { employee -> employee.id != feedbackFromId }
            .forEach { employee ->
                val option =
                    OptionObject
                        .builder()
                        .text(
                            PlainTextObject.builder().text("${employee.firstName} ${employee.lastName} (${employee.employeeId})").build(),
                        ).value(employee.id.toString())
                        .build()

                optionList.add(option)
            }
        return optionList
    }

    fun getFeedbackTypes(): MutableList<OptionObject> {
        val feedbackTypes = feedbackTypeRepository.fetchAll()
        val optionList = mutableListOf<OptionObject>()

        feedbackTypes.forEach { type ->
            val option =
                OptionObject
                    .builder()
                    .text(PlainTextObject.builder().text(type.feedbackType).build())
                    .value("${type.feedbackTypeId}")
                    .build()

            optionList.add(option)
        }
        return optionList
    }

    fun getFeedbackToEmployees(
        organisationId: Long,
        feedbackToId: Long,
    ): MutableList<OptionObject> {
        val employees = employeeRepository.getActiveEmployees(organisationId)
        val optionList = mutableListOf<OptionObject>()

        employees.forEach { employee ->
            val name =
                if (employee.id == feedbackToId) {
                    "Me"
                } else {
                    "${employee.firstName} ${employee.lastName} (${employee.employeeId})"
                }
            val option =
                OptionObject
                    .builder()
                    .text(PlainTextObject.builder().text(name).build())
                    .value(employee.id.toString())
                    .build()

            optionList.add(option)
        }
        return optionList
    }

    fun getGoalsList(
        organisationId: Long,
        feedbackToId: Long,
    ): MutableList<OptionObject> {
        val optionList = mutableListOf<OptionObject>()
        try {
            val goals = feedbackRequestService.getGoals(organisationId, feedbackToId)
            if (goals.isNotEmpty()) {
                goals.forEach { goal ->
                    goal.description?.let {
                        val trimmedText =
                            if (it.length > 75) {
                                "${it.substring(0, 72)}..."
                            } else {
                                it
                            }

                        val option =
                            OptionObject
                                .builder()
                                .text(PlainTextObject.builder().text(trimmedText).build())
                                .value(goal.id.toString())
                                .build()
                        optionList.add(option)
                    }
                }
            }
        } catch (e: Exception) {
            return optionList
        }
        return optionList
    }

    fun fetchLatestDraftFeedbacks(
        organisationId: Long,
        feedbackFromId: Long,
    ): List<FeedbackData> {
        val allFeedbacks =
            feedbackService.fetchAllSubmittedFeedbacks(
                organisationId = organisationId,
                feedbackFromId = feedbackFromId,
                feedbackTypeId = listOf(-99),
                feedbackToId = listOf(-99),
                reviewCycleId = listOf(-99),
                page = 1,
                limit = Int.MAX_VALUE,
                sortBy = "dateDesc",
            )
        val draftFeedbacks = allFeedbacks.filter { it.isDraft }
        return draftFeedbacks.take(3)
    }

    fun fetchPendingRequestFeedback(
        organisationId: Long,
        feedbackFromId: Long,
    ): List<FeedbackRequestData> {
        val allPendingRequests =
            feedbackRequestRepository.fetchFeedbackRequestData(
                FeedbackRequestParams(
                    organisationId = organisationId,
                    feedbackFromId = listOf(feedbackFromId.toInt()),
                    feedbackToId = listOf(-99),
                    requestedById = listOf(-99),
                    reviewCycleId = listOf(-99),
                    isSubmitted = listOf("false"),
                    sortBy = "dateDesc",
                ),
                offset = 0,
                limit = 10,
            )
        return allPendingRequests
    }

    fun convertHtmlToMarkdown(html: String): String {
        val copyDown = CopyDown(OptionsBuilder.anOptions().withStrongDelimiter("*").build())
        return copyDown.convert(html)
    }

    fun convertMarkdownToHtml(markdown: String): String {
        val parser = Parser.builder().build()
        val document: Node = parser.parse(markdown)
        val renderer = HtmlRenderer.builder().build()
        return renderer.render(document)
    }

    fun getSuggestionData(payload: String): SlackAddSuggestionData {
        val objectMapper = ObjectMapper()
        val payloadNode = objectMapper.readTree(payload)
        val valuesNode = payloadNode["view"].get("state").get("values")

        val suggestionText =
            valuesNode["suggestion_input"]
                ?.get("plain_text_input_action")
                ?.get("value")
                ?.asText()
                ?.trim()
                ?: ""

        val isAnonymous =
            valuesNode["anonymous_suggestion_toggle"]
                ?.get("anonymous_toggle_action")
                ?.get("selected_options")
                ?.any { it["value"]?.asText() == "is_anonymous" } ?: false

        if (suggestionText.length !in 50..1000) {
            return SlackAddSuggestionData(
                isAnonymous = isAnonymous,
                suggestion = "",
                errorsMessage = LENGTH_VALIDATION_ERROR,
                blockId = "suggestion_input",
            )
        }

        return SlackAddSuggestionData(
            isAnonymous = isAnonymous,
            suggestion = suggestionText,
            errorsMessage = null,
            blockId = null,
        )
    }

    fun extractRequestFeedbackFields(payload: String): RequestFeedbackFields {
        val objectMapper = ObjectMapper()
        val payloadNode = objectMapper.readTree(payload)
        val values = payloadNode.path("view").path("state").path("values")

        return RequestFeedbackFields(
            positive = extract(values, "positive_feedback_input", "positive_feedback_value"),
            improvement = extract(values, "improvement_feedback_input", "improvement_feedback_value"),
            appreciation = extract(values, "appreciation_feedback_input", "appreciation_feedback_value"),
        )
    }

    fun extract(
        values: JsonNode,
        blockId: String,
        actionId: String,
    ): String =
        values
            .path(blockId)
            .path(actionId)
            .path("value")
            .asText("")

    fun fetchFeedbackTypeIdsByName(): Map<String, Int> {
        val feedbackTypes: List<FeedbackType> = feedbackTypeRepository.fetchAll()
        return feedbackTypes.associate { it.feedbackType.lowercase() to it.feedbackTypeId }
    }
}

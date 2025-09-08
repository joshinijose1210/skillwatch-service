package scalereal.core.models.domain

import com.slack.api.model.block.LayoutBlock

data class SlackDetails(
    val organisationId: Long,
    val accessToken: String,
    val channelId: String,
    val webhookURL: String,
)

data class SlackMessage(
    val text: String,
)

data class SlackFeedbackData(
    val feedbackToId: Long,
    val feedbackTypeId: Int,
    val feedback: String,
    val errorsMessage: String?,
    val blockId: String?,
)

data class UserInfo(
    val id: String,
    val username: String,
)

data class SlackRequestFeedbackData(
    val feedbackFromSelectValue: List<Long>,
    val feedbackToSelectValues: List<Long>,
    val goalSelectValue: Long?,
    val feedbackRequestDescriptionValue: String,
)

data class PayloadInfo(
    val actionId: String,
    val callbackId: String,
    val triggerId: String,
    val userId: String,
)

data class CommandPayloadInfo(
    val triggerId: String,
    val userId: String?,
)

data class SlackAddSuggestionData(
    val isAnonymous: Boolean,
    val suggestion: String,
    val errorsMessage: String?,
    val blockId: String?,
)

data class RequestFeedbackFields(
    val positive: String,
    val improvement: String,
    val appreciation: String,
)

data class ModalContent(
    val blocks: MutableList<LayoutBlock>,
    val privateMetadata: String,
)

package scalereal.core.slack

import com.slack.api.Slack
import com.slack.api.methods.request.views.ViewsOpenRequest
import com.slack.api.model.block.InputBlock
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.SectionBlock
import com.slack.api.model.block.composition.MarkdownTextObject
import com.slack.api.model.block.composition.PlainTextObject
import com.slack.api.model.block.element.ButtonElement
import com.slack.api.model.block.element.PlainTextInputElement
import com.slack.api.model.block.element.StaticSelectElement
import com.slack.api.model.view.Views
import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeRepository
import scalereal.core.feedbacks.FeedbackService
import scalereal.core.models.domain.AddFeedbackData
import scalereal.core.models.domain.CreateFeedbackParams

@Singleton
class SlackAddFeedback(
    private val feedbackService: FeedbackService,
    private val employeeRepository: EmployeeRepository,
    private val slackPayload: SlackPayloadData,
) {
    fun openFeedbackForm(
        accessToken: String,
        triggerId: String,
        organisationId: Long,
        feedbackFromId: Long,
    ): String {
        try {
            val response =
                Slack.getInstance().methods(accessToken).viewsOpen(
                    ViewsOpenRequest
                        .builder()
                        .triggerId(triggerId)
                        .view(
                            Views.view { viewBuilder ->
                                viewBuilder
                                    .type("modal")
                                    .title(
                                        Views.viewTitle {
                                            it.type("plain_text")
                                            it.text("Add Feedback")
                                            it.emoji(true)
                                        },
                                    ).callbackId("submit_feedback")
                                    .blocks(addFeedbackFormBlock(organisationId, feedbackFromId))
                                    .submit(
                                        Views.viewSubmit {
                                            it.type("plain_text")
                                            it.text("Submit").build()
                                            it.emoji(true)
                                        },
                                    )
                            },
                        ).build(),
                )
            return if (response.isOk) {
                "Modal view was opened successfully"
            } else {
                "Failed to open the modal view"
            }
        } catch (e: Exception) {
            return "An error occurred: ${e.message}"
        }
    }

    fun addFeedbackFormBlock(
        organisationId: Long,
        feedbackFromId: Long,
    ): MutableList<LayoutBlock> {
        val inputLabel1 =
            PlainTextObject
                .builder()
                .text("Employee Name")
                .emoji(true)
                .build()

        val options = slackPayload.getEmployees(organisationId, feedbackFromId)
        val staticBlock1 =
            StaticSelectElement
                .builder()
                .actionId("employee_select")
                .placeholder(PlainTextObject.builder().text("Select an option").build())
                .options(options)
                .build()

        val inputBlock1 =
            InputBlock
                .builder()
                .label(inputLabel1)
                .blockId("employee_select_input")
                .element(staticBlock1)
                .build()

        val inputLabel2 =
            PlainTextObject
                .builder()
                .text("Feedback Type")
                .emoji(true)
                .build()

        val feedbackTypes = slackPayload.getFeedbackTypes()
        val staticBlock2 =
            StaticSelectElement
                .builder()
                .actionId("feedback_type_select")
                .placeholder(PlainTextObject.builder().text("Select an option").build())
                .options(feedbackTypes)
                .build()

        val inputBlock2 =
            InputBlock
                .builder()
                .label(inputLabel2)
                .blockId("feedback_type_input")
                .element(staticBlock2)
                .build()

        val inputLabel3 =
            PlainTextObject
                .builder()
                .text("Feedback")
                .emoji(true)
                .build()

        val multilineTextInput =
            PlainTextInputElement
                .builder()
                .actionId("plain_text_input-action3")
                .placeholder(PlainTextObject.builder().text("Write your feedback here...").build())
                .multiline(true)
                .minLength(50)
                .maxLength(1000)
                .build()

        val inputBlock3 =
            InputBlock
                .builder()
                .label(inputLabel3)
                .blockId("feedback_input")
                .hint(PlainTextObject.builder().text("Markdown text allowed.").build())
                .element(multilineTextInput)
                .build()

        val sectionBlock =
            SectionBlock
                .builder()
                .text(MarkdownTextObject.builder().text(" ").build())
                .accessory(
                    ButtonElement
                        .builder()
                        .text(
                            PlainTextObject
                                .builder()
                                .text("Save As Draft")
                                .emoji(true)
                                .build(),
                        ).actionId("draft_feedback")
                        .build(),
                ).build()

        return mutableListOf(inputBlock1, inputBlock2, inputBlock3, sectionBlock)
    }

    fun feedbackAddEvent(
        payload: String,
        actionId: String?,
        callbackId: String?,
        accessToken: String,
    ): HashMap<String?, String?> {
        val isDraft =
            when {
                actionId == "draft_feedback" -> true
                callbackId == "submit_feedback" -> false
                else -> throw Exception("Invalid Action Id")
            }
        val userInfo = slackPayload.extractUserInfoFromEventPayload(payload)
        val emailId = slackPayload.getUserEmail(userInfo.id, accessToken)
        if (emailId != null) {
            if (employeeRepository.isEmployeeExists(emailId)) {
                val employeeDetail = employeeRepository.fetchByEmailId(emailId)
                val slackFeedbackData = slackPayload.getFeedbackData(payload)
                val characterCount = slackFeedbackData.feedback.length
                val htmlText = slackPayload.convertMarkdownToHtml(slackFeedbackData.feedback)

                if (slackFeedbackData.errorsMessage.isNullOrEmpty()) {
                    if (employeeDetail != null) {
                        if (characterCount in 50..1000) {
                            feedbackService.create(
                                CreateFeedbackParams(
                                    feedbackToId = slackFeedbackData.feedbackToId,
                                    feedbackFromId = employeeDetail.id,
                                    requestId = null,
                                    isDraft = isDraft,
                                    feedback =
                                        listOf(
                                            AddFeedbackData(
                                                feedbackTypeId = slackFeedbackData.feedbackTypeId,
                                                feedbackText = htmlText,
                                                markdownText = slackFeedbackData.feedback,
                                            ),
                                        ),
                                ),
                            )
                        } else {
                            throw IllegalArgumentException("Please write more than 50 characters.")
                        }
                    }
                } else {
                    return hashMapOf(slackFeedbackData.blockId to slackFeedbackData.errorsMessage)
                }
            }
        }
        return hashMapOf()
    }

    fun successFeedbackForm(
        actionId: String,
        callbackId: String?,
    ): String {
        val text =
            when {
                actionId == "draft_feedback" -> Constants.FEEDBACK_DRAFT_DETAIL
                callbackId == "submit_feedback" -> Constants.FEEDBACK_SAVED_DETAIL
                actionId == "draft_draft_feedback" -> Constants.FEEDBACK_DRAFT_DETAIL
                callbackId == "submit_draft_feedback" -> Constants.FEEDBACK_SAVED_DETAIL
                else -> throw Exception("Invalid action Id or Callback Id")
            }
        return "{\n" +
            "  \"response_action\": \"update\",\n" +
            "  \"view\": {\n" +
            "    \"type\": \"modal\",\n" +
            "    \"title\": {\n" +
            "      \"type\": \"plain_text\",\n" +
            "      \"text\": \"Feedback Sent\"\n" +
            "    },\n" +
            "    \"blocks\": [\n" +
            "      {\n" +
            "        \"type\": \"section\",\n" +
            "        \"text\": {\n" +
            "          \"type\": \"plain_text\",\n" +
            "          \"text\": \"$text\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}\n"
    }
}

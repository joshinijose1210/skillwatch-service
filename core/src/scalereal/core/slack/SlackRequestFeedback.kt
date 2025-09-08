package scalereal.core.slack

import com.slack.api.Slack
import com.slack.api.methods.request.views.ViewsOpenRequest
import com.slack.api.methods.request.views.ViewsUpdateRequest
import com.slack.api.model.block.ActionsBlock
import com.slack.api.model.block.HeaderBlock
import com.slack.api.model.block.InputBlock
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.SectionBlock
import com.slack.api.model.block.composition.ConfirmationDialogObject
import com.slack.api.model.block.composition.MarkdownTextObject
import com.slack.api.model.block.composition.OptionObject
import com.slack.api.model.block.composition.PlainTextObject
import com.slack.api.model.block.element.ButtonElement
import com.slack.api.model.block.element.MultiStaticSelectElement
import com.slack.api.model.block.element.PlainTextInputElement
import com.slack.api.model.block.element.StaticSelectElement
import com.slack.api.model.view.Views
import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeRepository
import scalereal.core.feedbacks.FeedbackRequestService
import scalereal.core.models.domain.Employee

@Singleton
class SlackRequestFeedback(
    private val slackPayload: SlackPayloadData,
    private val employeeRepository: EmployeeRepository,
    private val feedbackRequestService: FeedbackRequestService,
) {
    fun openRequestFeedback(
        decodedPayload: String,
        employeeDetails: Employee,
        actionId: String,
        accessToken: String,
    ): String {
        try {
            val requestTo = if (actionId == "request_feedback_for_self") RequestType.SELF.value else RequestType.TEAM.value
            val requestBlock = requestFeedbackFormBlock(employeeDetails.organisationId, employeeDetails.id, requestTo)
            val viewId = slackPayload.getViewIdFromPayload(decodedPayload)
            val response =
                Slack.getInstance().methods(accessToken).viewsUpdate(
                    ViewsUpdateRequest
                        .builder()
                        .viewId(viewId)
                        .view(
                            Views.view { viewBuilder ->
                                viewBuilder
                                    .type("modal")
                                    .title(
                                        Views.viewTitle {
                                            it.type("plain_text")
                                            it.text("Request Feedback")
                                            it.emoji(true)
                                        },
                                    ).callbackId("request_feedback")
                                    .blocks(requestBlock)
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

    fun requestFeedbackFormBlock(
        organisationId: Long,
        feedbackToId: Long,
        requestTo: String,
    ): MutableList<LayoutBlock> {
        val blocks = mutableListOf<LayoutBlock>()

        val plainText =
            PlainTextObject
                .builder()
                .text("Request feedback for $requestTo")
                .emoji(true)
                .build()

        val contextBlock =
            HeaderBlock
                .builder()
                .text(plainText)
                .build()

        blocks.add(contextBlock)

        val inputLabel1 =
            PlainTextObject
                .builder()
                .text("Feedback From")
                .emoji(true)
                .build()

        val feedbackFromOptions = slackPayload.getEmployees(organisationId, feedbackToId)
        val staticBlock1 =
            MultiStaticSelectElement
                .builder()
                .actionId("feedback_from_select")
                .placeholder(PlainTextObject.builder().text("Select an option").build())
                .options(feedbackFromOptions)
                .build()

        val inputBlock1 =
            InputBlock
                .builder()
                .label(inputLabel1)
                .blockId("feedback_from_input")
                .element(staticBlock1)
                .build()

        blocks.add(inputBlock1)

        val inputLabel2 =
            PlainTextObject
                .builder()
                .text("Feedback About")
                .emoji(true)
                .build()

        val feedbackToOptions = slackPayload.getFeedbackToEmployees(organisationId, feedbackToId)
        val defaultOption =
            OptionObject
                .builder()
                .text(PlainTextObject.builder().text("Me").build())
                .value(feedbackToId.toString())
                .build()

        val staticBlock2 =
            MultiStaticSelectElement
                .builder()
                .actionId("feedback_to_select")
                .placeholder(PlainTextObject.builder().text("Select an option").build())
                .options(if (requestTo == RequestType.SELF.value) mutableListOf(defaultOption) else feedbackToOptions)
                .initialOptions(if (requestTo == RequestType.SELF.value) mutableListOf(defaultOption) else null)
                .build()

        val inputBlock2 =
            InputBlock
                .builder()
                .label(inputLabel2)
                .blockId("feedback_to_input")
                .element(staticBlock2)
                .build()

        blocks.add(inputBlock2)

        val actionItems = slackPayload.getGoalsList(organisationId, feedbackToId)
        if (actionItems.isNotEmpty() && requestTo == RequestType.SELF.value) {
            val inputLabel3 =
                PlainTextObject
                    .builder()
                    .text("Goal")
                    .emoji(true)
                    .build()

            val staticBlock3 =
                StaticSelectElement
                    .builder()
                    .actionId("action_item_select")
                    .placeholder(PlainTextObject.builder().text("Select an option").build())
                    .options(actionItems)
                    .build()

            val inputBlock3 =
                InputBlock
                    .builder()
                    .label(inputLabel3)
                    .blockId("action_item_input")
                    .element(staticBlock3)
                    .optional(true)
                    .build()

            blocks.add(inputBlock3)
        }

        val inputLabel4 =
            PlainTextObject
                .builder()
                .text("Request Description")
                .emoji(true)
                .build()

        val multilineTextInput =
            PlainTextInputElement
                .builder()
                .actionId("feedback_request_description")
                .placeholder(
                    PlainTextObject.builder().text("Set Context: Please mention the project/task for which you want the feedback").build(),
                ).multiline(true)
                .minLength(50)
                .maxLength(1000)
                .build()

        val inputBlock4 =
            InputBlock
                .builder()
                .label(inputLabel4)
                .blockId("feedback_request_description_input")
                .hint(PlainTextObject.builder().text("Markdown text allowed.").build())
                .element(multilineTextInput)
                .build()

        blocks.add(inputBlock4)

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
                                .text("Back")
                                .emoji(true)
                                .build(),
                        ).actionId("back_to_request_options")
                        .confirm(
                            ConfirmationDialogObject
                                .builder()
                                .title(
                                    PlainTextObject.builder().text("Are you sure you want to go back ?").build(),
                                ).text(
                                    MarkdownTextObject
                                        .builder()
                                        .text(
                                            "If you leave now, you will lose the data you've entered so far.",
                                        ).build(),
                                ).confirm(PlainTextObject.builder().text("Yes").build())
                                .deny(PlainTextObject.builder().text("No").build())
                                .build(),
                        ).build(),
                ).build()
        blocks.add(sectionBlock)

        return blocks
    }

    fun requestFeedbackAddEvent(
        payload: String,
        accessToken: String,
    ) {
        val userInfo = slackPayload.extractUserInfoFromEventPayload(payload)
        val emailId = slackPayload.getUserEmail(userInfo.id, accessToken)
        if (emailId != null) {
            if (employeeRepository.isEmployeeExists(emailId)) {
                val employeeDetail = employeeRepository.fetchByEmailId(emailId)
                val slackFeedbackData = slackPayload.getRequestFeedbackData(payload)
                val htmlText = slackPayload.convertMarkdownToHtml(slackFeedbackData.feedbackRequestDescriptionValue)
                if (slackFeedbackData.feedbackFromSelectValue.none { it in slackFeedbackData.feedbackToSelectValues }) {
                    if (employeeDetail != null) {
                        feedbackRequestService.addInternalFeedbackRequest(
                            employeeDetail.id,
                            slackFeedbackData.feedbackToSelectValues,
                            slackFeedbackData.feedbackFromSelectValue,
                            slackFeedbackData.goalSelectValue,
                            htmlText,
                            slackFeedbackData.feedbackRequestDescriptionValue,
                        )
                    }
                } else {
                    throw Exception("Cannot select the same employee in Feedback About and Feedback From")
                }
            }
        }
    }

    fun successRequestFeedbackForm(): String =
        "{\n" +
            "  \"response_action\": \"update\",\n" +
            "  \"view\": {\n" +
            "    \"type\": \"modal\",\n" +
            "    \"title\": {\n" +
            "      \"type\": \"plain_text\",\n" +
            "      \"text\": \"Request Sent\"\n" +
            "    },\n" +
            "    \"blocks\": [\n" +
            "      {\n" +
            "        \"type\": \"section\",\n" +
            "        \"text\": {\n" +
            "          \"type\": \"plain_text\",\n" +
            "          \"text\": \"Feedback requested successfully.\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}\n"

    fun requestFeedbackOption(
        triggerId: String,
        accessToken: String,
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
                                            it.text("Request Feedback")
                                            it.emoji(true)
                                        },
                                    ).blocks(requestFeedbackOptionBlocks())
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

    fun requestFeedbackViewUpdate(
        decodedPayload: String,
        accessToken: String,
    ): String {
        try {
            val viewId = slackPayload.getViewIdFromPayload(decodedPayload)
            val response =
                Slack.getInstance().methods(accessToken).viewsUpdate(
                    ViewsUpdateRequest
                        .builder()
                        .viewId(viewId)
                        .view(
                            Views.view { viewBuilder ->
                                viewBuilder
                                    .type("modal")
                                    .title(
                                        Views.viewTitle {
                                            it.type("plain_text")
                                            it.text("SkillWatch")
                                            it.emoji(true)
                                        },
                                    ).blocks(requestFeedbackOptionBlocks())
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

    fun requestFeedbackOptionBlocks(): MutableList<LayoutBlock> {
        val blocks = mutableListOf<LayoutBlock>()

        val plainText =
            SectionBlock
                .builder()
                .text(MarkdownTextObject.builder().text("*Request Feedback about:*").build())
                .build()

        blocks.add(plainText)

        val sectionBlock1 =
            ActionsBlock
                .builder()
                .elements(
                    listOf(
                        ButtonElement
                            .builder()
                            .text(
                                PlainTextObject
                                    .builder()
                                    .text("Yourself")
                                    .emoji(true)
                                    .build(),
                            ).actionId("request_feedback_for_self")
                            .style("primary")
                            .build(),
                        ButtonElement
                            .builder()
                            .text(
                                PlainTextObject
                                    .builder()
                                    .text("Team Members")
                                    .emoji(true)
                                    .build(),
                            ).actionId("request_feedback_for_team")
                            .style("primary")
                            .build(),
                    ),
                ).build()
        blocks.add(sectionBlock1)
        return blocks
    }

    enum class RequestType(
        val value: String,
    ) {
        SELF("yourself"),
        TEAM("team member"),
    }
}

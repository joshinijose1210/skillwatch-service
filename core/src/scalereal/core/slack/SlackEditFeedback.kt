package scalereal.core.slack

import com.slack.api.Slack
import com.slack.api.methods.request.views.ViewsOpenRequest
import com.slack.api.methods.request.views.ViewsUpdateRequest
import com.slack.api.model.block.ActionsBlock
import com.slack.api.model.block.DividerBlock
import com.slack.api.model.block.HeaderBlock
import com.slack.api.model.block.InputBlock
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.SectionBlock
import com.slack.api.model.block.composition.ConfirmationDialogObject
import com.slack.api.model.block.composition.MarkdownTextObject
import com.slack.api.model.block.composition.OptionObject
import com.slack.api.model.block.composition.PlainTextObject
import com.slack.api.model.block.element.BlockElement
import com.slack.api.model.block.element.ButtonElement
import com.slack.api.model.block.element.PlainTextInputElement
import com.slack.api.model.block.element.StaticSelectElement
import com.slack.api.model.view.Views
import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeRepository
import scalereal.core.feedbacks.FeedbackRepository
import scalereal.core.feedbacks.FeedbackService
import scalereal.core.models.domain.Employee
import scalereal.core.models.domain.UpdateFeedbackData
import scalereal.core.models.domain.UpdateFeedbackParams
import kotlin.Exception

@Singleton
class SlackEditFeedback(
    private val feedbackService: FeedbackService,
    private val feedbackRepository: FeedbackRepository,
    private val employeeRepository: EmployeeRepository,
    private val slackPayload: SlackPayloadData,
) {
    fun openDraftFeedbackForm(
        accessToken: String,
        decodedPayload: String,
        organisationId: Long,
        feedbackFromId: Long,
        feedbackId: String,
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
                                            it.text("Edit Feedback")
                                            it.emoji(true)
                                        },
                                    ).callbackId("submit_draft_feedback")
                                    .privateMetadata(feedbackId)
                                    .blocks(draftFeedbackFormBlock(organisationId, feedbackFromId, feedbackId))
                                    .submit(
                                        Views.viewSubmit {
                                            it.type("plain_text")
                                            it.text("Submit")
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

    fun draftFeedbackFormBlock(
        organisationId: Long,
        feedbackFromId: Long,
        feedbackId: String,
    ): MutableList<LayoutBlock> {
        val draftFeedback = feedbackRepository.fetchFeedbackById(feedbackId.toLong(), organisationId)
        val markdownFeedback = slackPayload.convertHtmlToMarkdown(draftFeedback.feedback)

        val inputLabel1 =
            PlainTextObject
                .builder()
                .text("Employee Name")
                .emoji(true)
                .build()

        val options = slackPayload.getEmployees(organisationId, feedbackFromId)
        val defaultOption =
            OptionObject
                .builder()
                .text(
                    PlainTextObject
                        .builder()
                        .text(
                            "${draftFeedback.empFirstName} ${draftFeedback.empLastName} (${draftFeedback.feedbackToEmployeeId})",
                        ).build(),
                ).value(draftFeedback.feedbackToId.toString())
                .build()

        val staticBlock1 =
            StaticSelectElement
                .builder()
                .actionId("employee_select")
                .placeholder(PlainTextObject.builder().text("Select an option").build())
                .initialOption(defaultOption)
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
        val defaultType =
            OptionObject
                .builder()
                .text(PlainTextObject.builder().text(draftFeedback.feedbackType).build())
                .value("${draftFeedback.feedbackTypeId}")
                .build()

        val staticBlock2 =
            StaticSelectElement
                .builder()
                .actionId("feedback_type_select")
                .placeholder(PlainTextObject.builder().text("Select an option").build())
                .initialOption(defaultType)
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
                .initialValue(markdownFeedback)
                .multiline(true)
                .minLength(50)
                .maxLength(1000)
                .focusOnLoad(true)
                .build()

        val inputBlock3 =
            InputBlock
                .builder()
                .label(inputLabel3)
                .blockId("feedback_input")
                .hint(PlainTextObject.builder().text("Markdown text allowed.").build())
                .element(multilineTextInput)
                .build()

        val button1 =
            ButtonElement
                .builder()
                .text(PlainTextObject.builder().text("Back").build())
                .actionId("back_to_draft_list")
                .confirm(
                    ConfirmationDialogObject
                        .builder()
                        .title(PlainTextObject.builder().text("Are you sure you want to go back ?").build())
                        .text(MarkdownTextObject.builder().text("If you leave now, you will lose the new edited data.").build())
                        .confirm(PlainTextObject.builder().text("Yes").build())
                        .deny(PlainTextObject.builder().text("No").build())
                        .build(),
                ).build()

        val button2 =
            ButtonElement
                .builder()
                .text(PlainTextObject.builder().text("Save As Draft").build())
                .actionId("draft_draft_feedback")
                .build()

        val actionBlock =
            ActionsBlock
                .builder()
                .elements(mutableListOf(button1, button2) as List<BlockElement>?)
                .build()

        return mutableListOf(inputBlock1, inputBlock2, inputBlock3, actionBlock)
    }

    fun feedbackUpdateEvent(
        payload: String,
        actionId: String?,
        callbackId: String?,
        accessToken: String,
    ) {
        val isDraft =
            when {
                actionId == "draft_draft_feedback" -> true
                callbackId == "submit_draft_feedback" -> false
                else -> throw IllegalArgumentException("Invalid action Id or Callback Id")
            }

        val userInfo = slackPayload.extractUserInfoFromEventPayload(payload)
        val emailId = slackPayload.getUserEmail(userInfo.id, accessToken)

        if (emailId != null) {
            val employeeDetail = employeeRepository.fetchByEmailId(emailId)
            if (employeeDetail != null) {
                val slackFeedbackData = slackPayload.getFeedbackData(payload)
                val feedbackId = slackPayload.getFeedbackIdFromPayload(payload)
                val characterCount = slackFeedbackData.feedback.length
                val htmlText = slackPayload.convertMarkdownToHtml(slackFeedbackData.feedback)

                if (characterCount in 50..1000) {
                    if (feedbackId != null) {
                        feedbackService.update(
                            UpdateFeedbackParams(
                                feedbackToId = slackFeedbackData.feedbackToId,
                                feedbackFromId = employeeDetail.id,
                                requestId = null,
                                isDraft = isDraft,
                                feedbackData =
                                    listOf(
                                        UpdateFeedbackData(
                                            feedbackId = feedbackId.toLong(),
                                            feedbackTypeId = slackFeedbackData.feedbackTypeId,
                                            feedbackText = htmlText,
                                            markdownText = slackFeedbackData.feedback,
                                            isNewlyAdded = false,
                                            isRemoved = false,
                                        ),
                                    ),
                            ),
                        )
                    }
                } else {
                    throw IllegalArgumentException("Please write more than 50 characters.")
                }
            }
        }
    }

    fun successDraftForm(
        accessToken: String,
        decodedPayload: String,
        actionId: String,
        callbackId: String?,
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
                                    .callbackId("close_form")
                                    .title(
                                        Views.viewTitle {
                                            it.type("plain_text")
                                            it.text(Constants.FEEDBACK_DRAFT_TITLE)
                                        },
                                    ).blocks(successViewBlock(actionId, callbackId))
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

    fun successViewBlock(
        actionId: String,
        callbackId: String?,
    ): MutableList<LayoutBlock> {
        val text =
            when {
                actionId == "draft_feedback" -> Constants.FEEDBACK_DRAFT_DETAIL
                callbackId == "submit_feedback" -> Constants.FEEDBACK_SAVED_DETAIL
                actionId == "draft_draft_feedback" -> Constants.FEEDBACK_DRAFT_DETAIL
                callbackId == "submit_draft_feedback" -> Constants.FEEDBACK_SAVED_DETAIL
                else -> throw Exception("Invalid action Id or Callback Id")
            }

        val markDownText = MarkdownTextObject.builder().text(text).build()

        val contextBlock =
            SectionBlock
                .builder()
                .text(markDownText)
                .build()

        return mutableListOf(contextBlock)
    }

    fun openDraftFeedbackListForm(
        accessToken: String,
        triggerId: String,
        employeeDetails: Employee,
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
                                    .callbackId("open_draft_view_list")
                                    .title(
                                        Views.viewTitle {
                                            it.type("plain_text")
                                            it.text("Edit Feedback")
                                        },
                                    ).blocks(draftFeedbackListViewBlock(employeeDetails.organisationId, employeeDetails.id))
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

    fun updateDraftFeedbackListForm(
        accessToken: String,
        decodedPayload: String,
        employeeDetails: Employee,
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
                                    .callbackId("open_draft_view_list")
                                    .title(
                                        Views.viewTitle {
                                            it.type("plain_text")
                                            it.text("Edit Feedback")
                                        },
                                    ).blocks(draftFeedbackListViewBlock(employeeDetails.organisationId, employeeDetails.id))
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

    fun draftFeedbackListViewBlock(
        organisationId: Long,
        feedbackFromId: Long,
    ): MutableList<LayoutBlock> {
        val feedbackDataList = slackPayload.fetchLatestDraftFeedbacks(organisationId, feedbackFromId)
        val blocks = mutableListOf<LayoutBlock>()

        if (feedbackDataList.isEmpty()) {
            val message =
                SectionBlock
                    .builder()
                    .text(MarkdownTextObject.builder().text("*No draft feedback found*").build())
                    .build()
            blocks.add(message)
        } else {
            val header =
                HeaderBlock
                    .builder()
                    .text(
                        PlainTextObject
                            .builder()
                            .text("Drafted Feedback")
                            .build(),
                    ).build()

            blocks.add(header)

            for (index in 0 until minOf(3, feedbackDataList.size)) {
                val feedback = feedbackDataList[index]

                val divider = DividerBlock.builder().build()
                blocks.add(divider)

                val feedbackTo = "${feedback.empFirstName} ${feedback.empLastName} (${feedback.feedbackToEmployeeId})"
                val feedbackType = feedback.feedbackType

                val feedbackToSection =
                    SectionBlock
                        .builder()
                        .text(
                            MarkdownTextObject
                                .builder()
                                .text("*Feedback To:* $feedbackTo")
                                .build(),
                        ).build()
                blocks.add(feedbackToSection)

                val feedbackTypeSection =
                    SectionBlock
                        .builder()
                        .text(
                            MarkdownTextObject
                                .builder()
                                .text("*Feedback Type:* $feedbackType")
                                .build(),
                        ).accessory(
                            ButtonElement
                                .builder()
                                .text(
                                    PlainTextObject
                                        .builder()
                                        .text("Edit")
                                        .emoji(true)
                                        .build(),
                                ).value("${feedback.srNo}")
                                .actionId("get-draft-feedback")
                                .build(),
                        ).build()
                blocks.add(feedbackTypeSection)
            }
        }
        return blocks
    }
}

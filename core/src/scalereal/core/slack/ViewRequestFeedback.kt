package scalereal.core.slack

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
import com.slack.api.model.block.composition.PlainTextObject
import com.slack.api.model.block.element.BlockElement
import com.slack.api.model.block.element.ButtonElement
import com.slack.api.model.block.element.PlainTextInputElement
import com.slack.api.model.view.Views
import jakarta.inject.Singleton
import scalereal.core.feedbacks.FeedbackRequestRepository
import scalereal.core.feedbacks.FeedbackRequestService
import scalereal.core.feedbacks.FeedbackService
import scalereal.core.models.domain.AddFeedbackData
import scalereal.core.models.domain.CreateFeedbackParams
import scalereal.core.models.domain.Employee
import scalereal.core.models.domain.ModalContent
import scalereal.core.models.domain.UpdateFeedbackData
import scalereal.core.models.domain.UpdateFeedbackParams
import scalereal.core.slack.Constants.AT_LEAST_ONE_FIELD_REQUIRED_ERROR
import scalereal.core.slack.Constants.LENGTH_VALIDATION_ERROR

@Singleton
class ViewRequestFeedback(
    private val slackPayload: SlackPayloadData,
    private val feedbackRequestRepository: FeedbackRequestRepository,
    private val feedbackService: FeedbackService,
    private val feedbackRequestService: FeedbackRequestService,
) {
    fun openRequestFeedbackListForm(
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
                                    .callbackId("view_request_feedback_list")
                                    .title(
                                        Views.viewTitle {
                                            it.type("plain_text")
                                            it.text("Request Received")
                                        },
                                    ).blocks(
                                        viewRequestFeedbackListViewBlock(
                                            employeeDetails.organisationId,
                                            employeeDetails.id,
                                        ),
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

    fun updateRequestFeedbackListForm(
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
                                    .callbackId("view_request_feedback_list")
                                    .title(
                                        Views.viewTitle {
                                            it.type("plain_text")
                                            it.text("Request Received")
                                        },
                                    ).blocks(viewRequestFeedbackListViewBlock(employeeDetails.organisationId, employeeDetails.id))
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

    fun viewRequestFeedbackListViewBlock(
        organisationId: Long,
        feedbackFromId: Long,
    ): MutableList<LayoutBlock> {
        val pendingRequestFeedbackList = slackPayload.fetchPendingRequestFeedback(organisationId, feedbackFromId)
        val blocks = mutableListOf<LayoutBlock>()

        if (pendingRequestFeedbackList.isEmpty()) {
            val message =
                SectionBlock
                    .builder()
                    .text(MarkdownTextObject.builder().text("*No pending request feedback found*").build())
                    .build()
            blocks.add(message)
        } else {
            val header =
                HeaderBlock
                    .builder()
                    .text(
                        PlainTextObject
                            .builder()
                            .text("Pending Request Feedback")
                            .build(),
                    ).build()
            blocks.add(header)

            for (index in 0 until minOf(10, pendingRequestFeedbackList.size)) {
                val pendingRequestFeedback = pendingRequestFeedbackList[index]

                val divider = DividerBlock.builder().build()
                blocks.add(divider)

                val requestedBy =
                    "${pendingRequestFeedback.requestedByFirstName} ${pendingRequestFeedback.requestedByLastName} " +
                        "(${pendingRequestFeedback.requestedByEmployeeId})"
                val feedbackTo =
                    "${pendingRequestFeedback.feedbackToFirstName} ${pendingRequestFeedback.feedbackToLastName} " +
                        "(${pendingRequestFeedback.feedbackToEmployeeId})"
                val feedbackAboutSection =
                    SectionBlock
                        .builder()
                        .text(
                            MarkdownTextObject
                                .builder()
                                .text("*Requested From:* $requestedBy")
                                .build(),
                        ).build()
                blocks.add(feedbackAboutSection)

                val buttonText = if (pendingRequestFeedback.isDraft) "Edit" else "Add"
                val feedbackToDetail = if (requestedBy == feedbackTo) "Self" else feedbackTo
                val feedbackToSection =
                    SectionBlock
                        .builder()
                        .text(
                            MarkdownTextObject
                                .builder()
                                .text("*Feedback About:* $feedbackToDetail")
                                .build(),
                        ).accessory(
                            ButtonElement
                                .builder()
                                .text(
                                    PlainTextObject
                                        .builder()
                                        .text(buttonText)
                                        .emoji(true)
                                        .build(),
                                ).value("${pendingRequestFeedback.requestId}")
                                .actionId("get-request-feedback")
                                .build(),
                        ).build()
                blocks.add(feedbackToSection)
            }
        }
        return blocks
    }

    fun openAddRequestFeedbackForm(
        accessToken: String,
        decodedPayload: String,
        requestFeedbackId: String,
    ): String {
        try {
            val viewId = slackPayload.getViewIdFromPayload(decodedPayload)
            val modalContent = buildRequestFeedbackModalBlocks(requestFeedbackId)
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
                                            it.text("Add Requested Feedback")
                                            it.emoji(true)
                                        },
                                    ).callbackId("submit_request_feedback")
                                    .privateMetadata(modalContent.privateMetadata)
                                    .blocks(modalContent.blocks)
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

    fun buildRequestFeedbackModalBlocks(requestFeedbackId: String): ModalContent {
        val requestFeedback = feedbackRequestService.getFeedbackRequestDetailsById(requestFeedbackId.toLong())
        val htmlText = requestFeedback.request?.let { slackPayload.convertHtmlToMarkdown(it) }
        val requestedBy =
            "${requestFeedback.requestedByFirstName} ${requestFeedback.requestedByLastName} (${requestFeedback.requestedByEmployeeId})"
        val feedbackTo =
            "${requestFeedback.feedbackToFirstName} ${requestFeedback.feedbackToLastName} (${requestFeedback.feedbackToEmployeeId})"

        val feedbackTypeIds = slackPayload.fetchFeedbackTypeIdsByName()
        val draftsByTypeId = requestFeedback.feedbackData.filter { it.isDraft }.associateBy { it.feedbackTypeId }
        val positiveDraft = feedbackTypeIds[FeedbackType.POSITIVE.label]?.let { draftsByTypeId[it] }
        val improvementDraft = feedbackTypeIds[FeedbackType.IMPROVEMENT.label]?.let { draftsByTypeId[it] }
        val appreciationDraft = feedbackTypeIds[FeedbackType.APPRECIATION.label]?.let { draftsByTypeId[it] }

        val positivePrefill = positiveDraft?.let { slackPayload.convertHtmlToMarkdown(it.feedback) } ?: ""
        val improvementPrefill = improvementDraft?.let { slackPayload.convertHtmlToMarkdown(it.feedback) } ?: ""
        val appreciationPrefill = appreciationDraft?.let { slackPayload.convertHtmlToMarkdown(it.feedback) } ?: ""

        val metadata =
            mutableMapOf<String, Any>(
                "requestId" to requestFeedbackId,
                "mode" to if (positiveDraft != null || improvementDraft != null || appreciationDraft != null) "edit" else "add",
            )
        positiveDraft?.let { metadata["positiveFeedbackId"] = it.feedbackId }
        improvementDraft?.let { metadata["improvementFeedbackId"] = it.feedbackId }
        appreciationDraft?.let { metadata["appreciationFeedbackId"] = it.feedbackId }
        val privateMetadata = jacksonObjectMapper().writeValueAsString(metadata)

        val blocks = mutableListOf<LayoutBlock>()

        blocks.add(
            SectionBlock
                .builder()
                .text(
                    MarkdownTextObject
                        .builder()
                        .text("*Feedback Request From:*\n$requestedBy")
                        .build(),
                ).build(),
        )

        val feedbackToDetail = if (requestedBy == feedbackTo) "Self" else feedbackTo
        blocks.add(
            SectionBlock
                .builder()
                .text(
                    MarkdownTextObject
                        .builder()
                        .text("*Feedback About:*\n$feedbackToDetail")
                        .build(),
                ).build(),
        )

        blocks.add(
            SectionBlock
                .builder()
                .text(
                    MarkdownTextObject
                        .builder()
                        .text("*Context:*\n$htmlText")
                        .build(),
                ).build(),
        )

        blocks.add(
            InputBlock
                .builder()
                .blockId("positive_feedback_input")
                .label(PlainTextObject.builder().text("Positive Feedback").build())
                .hint(PlainTextObject.builder().text("Markdown text allowed.").build())
                .optional(true)
                .element(
                    PlainTextInputElement
                        .builder()
                        .actionId("positive_feedback_value")
                        .placeholder(PlainTextObject.builder().text("Write Positive Feedback here...").build())
                        .initialValue(positivePrefill)
                        .multiline(true)
                        .minLength(50)
                        .maxLength(1000)
                        .build(),
                ).build(),
        )

        blocks.add(
            InputBlock
                .builder()
                .blockId("improvement_feedback_input")
                .label(PlainTextObject.builder().text("Improvement Pointers").build())
                .hint(PlainTextObject.builder().text("Markdown text allowed.").build())
                .optional(true)
                .element(
                    PlainTextInputElement
                        .builder()
                        .actionId("improvement_feedback_value")
                        .placeholder(PlainTextObject.builder().text("Write Improvement Pointers here...").build())
                        .initialValue(improvementPrefill)
                        .multiline(true)
                        .minLength(50)
                        .maxLength(1000)
                        .build(),
                ).build(),
        )

        blocks.add(
            InputBlock
                .builder()
                .blockId("appreciation_feedback_input")
                .label(PlainTextObject.builder().text("Appreciation Note").build())
                .hint(PlainTextObject.builder().text("Markdown text allowed.").build())
                .optional(true)
                .element(
                    PlainTextInputElement
                        .builder()
                        .actionId("appreciation_feedback_value")
                        .placeholder(PlainTextObject.builder().text("Write Appreciation Notes here...").build())
                        .initialValue(appreciationPrefill)
                        .multiline(true)
                        .minLength(50)
                        .maxLength(1000)
                        .build(),
                ).build(),
        )

        val backButton =
            ButtonElement
                .builder()
                .text(PlainTextObject.builder().text("Back").build())
                .actionId("back_to_request_list")
                .confirm(
                    ConfirmationDialogObject
                        .builder()
                        .title(PlainTextObject.builder().text("Are you sure you want to go back ?").build())
                        .text(MarkdownTextObject.builder().text("If you leave now, you will lose the new edited data.").build())
                        .confirm(PlainTextObject.builder().text("Yes").build())
                        .deny(PlainTextObject.builder().text("No").build())
                        .build(),
                ).build()

        val saveDraftButton =
            ButtonElement
                .builder()
                .text(PlainTextObject.builder().text("Save As Draft").build())
                .actionId("draft_request_feedback")
                .build()

        blocks.add(
            ActionsBlock
                .builder()
                .elements(mutableListOf(backButton, saveDraftButton) as List<BlockElement>?)
                .build(),
        )

        return ModalContent(blocks = blocks, privateMetadata = privateMetadata)
    }

    fun requestFeedbackSubmission(
        decodedPayload: String,
        employeeDetail: Employee,
        isDraft: Boolean,
    ): HashMap<String?, String?> {
        val meta = slackPayload.parsePrivateMetadata(decodedPayload)
        val requestId = meta["requestId"].asText().toLong()
        val feedbackTypeIds = slackPayload.fetchFeedbackTypeIdsByName()
        val requestFeedback = feedbackRequestRepository.fetchFeedbackRequestDetails(requestId)
        val feedbackFields = slackPayload.extractRequestFeedbackFields(decodedPayload)

        val errorMap = hashMapOf<String?, String?>()

        val positiveValue = feedbackFields.positive
        val improvementValue = feedbackFields.improvement
        val appreciationValue = feedbackFields.appreciation

        if (positiveValue.isBlank() && improvementValue.isBlank() && appreciationValue.isBlank()) {
            return hashMapOf(
                "positive_feedback_input" to AT_LEAST_ONE_FIELD_REQUIRED_ERROR,
                "improvement_feedback_input" to AT_LEAST_ONE_FIELD_REQUIRED_ERROR,
                "appreciation_feedback_input" to AT_LEAST_ONE_FIELD_REQUIRED_ERROR,
            )
        }

        val validations =
            listOf(
                "positive_feedback_input" to positiveValue,
                "improvement_feedback_input" to improvementValue,
                "appreciation_feedback_input" to appreciationValue,
            )

        for ((field, value) in validations) {
            if (value.isNotBlank() && value.length !in 50..1000) {
                errorMap[field] = LENGTH_VALIDATION_ERROR
            }
        }

        if (errorMap.isNotEmpty()) return errorMap

        val updateList = mutableListOf<UpdateFeedbackData>()
        val createList = mutableListOf<AddFeedbackData>()

        fun handleFeedback(
            fieldValue: String,
            feedbackId: Long?,
            typeName: String,
        ) {
            val typeId = feedbackTypeIds[typeName] ?: return
            val htmlValue = slackPayload.convertMarkdownToHtml(fieldValue)
            if (fieldValue.isNotBlank()) {
                if (feedbackId != null) {
                    updateList.add(
                        UpdateFeedbackData(
                            feedbackId = feedbackId,
                            feedbackTypeId = typeId,
                            feedbackText = htmlValue,
                            markdownText = fieldValue,
                            isNewlyAdded = false,
                        ),
                    )
                } else {
                    createList.add(
                        AddFeedbackData(
                            feedbackTypeId = typeId,
                            feedbackText = htmlValue,
                            markdownText = fieldValue,
                        ),
                    )
                }
            }
        }

        handleFeedback(positiveValue, meta["positiveFeedbackId"]?.asLong(), FeedbackType.POSITIVE.label)
        handleFeedback(improvementValue, meta["improvementFeedbackId"]?.asLong(), FeedbackType.IMPROVEMENT.label)
        handleFeedback(appreciationValue, meta["appreciationFeedbackId"]?.asLong(), FeedbackType.APPRECIATION.label)

        if (updateList.isNotEmpty()) {
            feedbackService.update(
                UpdateFeedbackParams(
                    feedbackToId = requestFeedback.feedbackToId,
                    feedbackFromId = employeeDetail.id,
                    feedbackData = updateList,
                    requestId = requestId,
                    isDraft = isDraft,
                ),
            )
        }
        if (createList.isNotEmpty()) {
            feedbackService.create(
                CreateFeedbackParams(
                    feedbackToId = requestFeedback.feedbackToId,
                    feedbackFromId = employeeDetail.id,
                    requestId = requestId,
                    isDraft = isDraft,
                    feedback = createList,
                ),
            )
        }
        return hashMapOf()
    }

    fun successAddRequestFeedbackForm(): String =
        "{\n" +
            "  \"response_action\": \"update\",\n" +
            "  \"view\": {\n" +
            "    \"type\": \"modal\",\n" +
            "    \"title\": {\n" +
            "      \"type\": \"plain_text\",\n" +
            "      \"text\": \"Request Feedback Sent\"\n" +
            "    },\n" +
            "    \"blocks\": [\n" +
            "      {\n" +
            "        \"type\": \"section\",\n" +
            "        \"text\": {\n" +
            "          \"type\": \"plain_text\",\n" +
            "          \"text\": \"${Constants.FEEDBACK_SAVED_DETAIL}\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}\n"

    fun successRequestFeedbackDraftForm(
        accessToken: String,
        decodedPayload: String,
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
                                    ).blocks(successViewBlock())
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

    fun successViewBlock(): MutableList<LayoutBlock> {
        val markDownText = MarkdownTextObject.builder().text(Constants.REQUEST_FEEDBACK_SAVED_DETAIL).build()

        val contextBlock =
            SectionBlock
                .builder()
                .text(markDownText)
                .build()

        return mutableListOf(contextBlock)
    }
}

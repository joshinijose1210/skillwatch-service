package scalereal.core.slack

import com.slack.api.Slack
import com.slack.api.methods.request.views.ViewsOpenRequest
import com.slack.api.model.block.InputBlock
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.SectionBlock
import com.slack.api.model.block.composition.MarkdownTextObject
import com.slack.api.model.block.composition.OptionObject
import com.slack.api.model.block.composition.PlainTextObject
import com.slack.api.model.block.element.CheckboxesElement
import com.slack.api.model.block.element.PlainTextInputElement
import com.slack.api.model.view.Views
import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeRepository
import scalereal.core.organisations.OrganisationRepository
import scalereal.core.slack.Constants.SUGGESTION_SUBMITTED_SUCCESS_MESSAGE
import scalereal.core.suggestions.SuggestionService

@Singleton
class AddCompanySuggestion(
    private val slackPayload: SlackPayloadData,
    private val organisationRepository: OrganisationRepository,
    private val employeeRepository: EmployeeRepository,
    private val suggestionService: SuggestionService,
) {
    fun openSuggestionForm(
        accessToken: String,
        triggerId: String,
        organisationId: Long,
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
                                            it.text("Add Suggestion")
                                            it.emoji(true)
                                        },
                                    ).callbackId("submit_suggestion")
                                    .blocks(addCompanySuggestionFormBlock(organisationId))
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

    fun addCompanySuggestionFormBlock(organisationId: Long): MutableList<LayoutBlock> {
        val generalSettings = organisationRepository.getGeneralSettings(organisationId)
        val blocks = mutableListOf<LayoutBlock>()

        if (generalSettings.isAnonymousSuggestionAllowed) {
            val toggleBlock =
                SectionBlock
                    .builder()
                    .blockId("anonymous_suggestion_toggle")
                    .text(
                        MarkdownTextObject
                            .builder()
                            .text("Prefer to submit suggestions anonymously?")
                            .build(),
                    ).accessory(
                        CheckboxesElement
                            .builder()
                            .actionId("anonymous_toggle_action")
                            .options(
                                listOf(
                                    OptionObject
                                        .builder()
                                        .text(PlainTextObject.builder().text("Yes").build())
                                        .value("is_anonymous")
                                        .build(),
                                ),
                            ).build(),
                    ).build()

            blocks.add(toggleBlock)
        }

        val multilineTextInput =
            PlainTextInputElement
                .builder()
                .actionId("plain_text_input_action")
                .placeholder(
                    PlainTextObject
                        .builder()
                        .text("Write your suggestion to improve our company, culture or processes...")
                        .build(),
                ).multiline(true)
                .minLength(50)
                .maxLength(1000)
                .build()

        val inputBlock =
            InputBlock
                .builder()
                .blockId("suggestion_input")
                .label(PlainTextObject.builder().text("Suggestion").build())
                .hint(PlainTextObject.builder().text("Markdown text allowed.").build())
                .element(multilineTextInput)
                .build()

        blocks.add(inputBlock)

        return blocks
    }

    fun addSuggestionEvent(
        payload: String,
        accessToken: String,
    ): HashMap<String?, String?> {
        val emailId =
            slackPayload.getUserEmail(
                slackPayload.extractUserInfoFromEventPayload(payload).id,
                accessToken,
            ) ?: return hashMapOf()

        val employeeDetail =
            employeeRepository
                .takeIf { it.isEmployeeExists(emailId) }
                ?.fetchByEmailId(emailId)
                ?: return hashMapOf()

        val suggestionData = slackPayload.getSuggestionData(payload)
        if (!suggestionData.errorsMessage.isNullOrEmpty()) {
            return hashMapOf(suggestionData.blockId to suggestionData.errorsMessage)
        }

        suggestionService.create(
            suggestedById = employeeDetail.id,
            suggestion = slackPayload.convertMarkdownToHtml(suggestionData.suggestion),
            markdownText = suggestionData.suggestion,
            isAnonymous = suggestionData.isAnonymous,
            isDraft = false,
        )

        return hashMapOf()
    }

    fun successAddSuggestionForm(): String =
        "{\n" +
            "  \"response_action\": \"update\",\n" +
            "  \"view\": {\n" +
            "    \"type\": \"modal\",\n" +
            "    \"title\": {\n" +
            "      \"type\": \"plain_text\",\n" +
            "      \"text\": \"Suggestion Sent\"\n" +
            "    },\n" +
            "    \"blocks\": [\n" +
            "      {\n" +
            "        \"type\": \"section\",\n" +
            "        \"text\": {\n" +
            "          \"type\": \"plain_text\",\n" +
            "          \"text\": \"$SUGGESTION_SUBMITTED_SUCCESS_MESSAGE\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}\n"
}

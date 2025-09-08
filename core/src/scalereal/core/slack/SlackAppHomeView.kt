package scalereal.core.slack

import com.slack.api.Slack
import com.slack.api.methods.request.views.ViewsOpenRequest
import com.slack.api.methods.request.views.ViewsPublishRequest
import com.slack.api.model.block.ActionsBlock
import com.slack.api.model.block.DividerBlock
import com.slack.api.model.block.HeaderBlock
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.PlainTextObject
import com.slack.api.model.block.element.BlockElement
import com.slack.api.model.block.element.ButtonElement
import com.slack.api.model.view.Views
import jakarta.inject.Inject
import jakarta.inject.Singleton
import scalereal.core.models.AppConfig

@Singleton
class SlackAppHomeView(
    @Inject private var appConfig: AppConfig,
) {
    private val instanceUrl = appConfig.getInstanceUrl()

    fun publishHomeView(
        userId: String,
        accessToken: String,
    ): String {
        try {
            val response =
                Slack.getInstance().methods(accessToken).viewsPublish(
                    ViewsPublishRequest
                        .builder()
                        .userId(userId)
                        .view(
                            Views.view { viewBuilder ->
                                viewBuilder
                                    .type("home")
                                    .blocks(homeViewLayoutBlocks())
                            },
                        ).build(),
                )
            return if (response.isOk) {
                "Home view was opened successfully"
            } else {
                "Failed to open the home view"
            }
        } catch (e: Exception) {
            return "An error occurred: ${e.message}"
        }
    }

    fun homeViewLayoutBlocks(): MutableList<LayoutBlock> {
        val blocks = mutableListOf<LayoutBlock>()

        val headerText = PlainTextObject("Welcome to SkillWatch :tada:", true)
        val headerBlock = HeaderBlock.builder().text(headerText).build()
        blocks.add(headerBlock)

        val dividerBlock = DividerBlock.builder().build()
        blocks.add(dividerBlock)

        val plainText =
            PlainTextObject
                .builder()
                .text("Here's what you can do with SkillWatch:")
                .emoji(true)
                .build()

        val contextBlock =
            HeaderBlock
                .builder()
                .text(plainText)
                .build()

        blocks.add(contextBlock)

        val addFeedbackButton =
            ButtonElement
                .builder()
                .text(PlainTextObject.builder().text("Add Feedback").build())
                .actionId("add_feedback_button")
                .style("primary")
                .build()

        val editFeedbackButton =
            ButtonElement
                .builder()
                .text(PlainTextObject.builder().text("Edit Feedback").build())
                .actionId("draft_feedback_button")
                .style("primary")
                .build()

        val requestFeedbackButton =
            ButtonElement
                .builder()
                .text(PlainTextObject.builder().text("Request Feedback").build())
                .actionId("request_feedback_button")
                .style("primary")
                .build()

        val suggestionBoxButton =
            ButtonElement
                .builder()
                .text(PlainTextObject.builder().text("Suggestion Box").build())
                .actionId("suggestion_box_button")
                .style("primary")
                .build()

        val viewRequestFeedbackButton =
            ButtonElement
                .builder()
                .text(PlainTextObject.builder().text("View Request Received").build())
                .actionId("view_request_feedback_button")
                .style("primary")
                .build()

        val skillWatchButton =
            ButtonElement
                .builder()
                .text(PlainTextObject.builder().text("Go To SkillWatch").build())
                .actionId("redirect_to_skillwatch")
                .url("$instanceUrl/login")
                .style("primary")
                .build()

        val actionsBlock =
            ActionsBlock
                .builder()
                .elements(
                    mutableListOf(
                        addFeedbackButton,
                        editFeedbackButton,
                        requestFeedbackButton,
                        viewRequestFeedbackButton,
                        suggestionBoxButton,
                        skillWatchButton,
                    ) as List<BlockElement>?,
                ).build()

        blocks.add(actionsBlock)
        return blocks
    }

    fun openUserNotFoundForm(
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
                                            it.text("SkillWatch")
                                            it.emoji(true)
                                        },
                                    ).blocks(openUserNotFoundBlock())
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

    fun openUserNotFoundBlock(): MutableList<LayoutBlock> {
        val plainText =
            PlainTextObject
                .builder()
                .text("User not added on SkillWatch.")
                .emoji(true)
                .build()

        val contextBlock =
            HeaderBlock
                .builder()
                .text(plainText)
                .build()
        return mutableListOf(contextBlock)
    }
}

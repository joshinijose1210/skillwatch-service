package scalereal.core.slack

import com.slack.api.methods.MethodsClient
import com.slack.api.model.User
import com.slack.api.model.block.DividerBlock
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.SectionBlock
import com.slack.api.model.block.composition.MarkdownTextObject
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import scalereal.core.models.AppConfig
import scalereal.core.models.Constants
import scalereal.core.models.domain.SlackDetails
import scalereal.core.models.domain.SlackMessage

@Singleton
class SlackService(
    @Value("\${SLACK_CLIENT_ID}") private val slackClientId: String,
    @Value("\${SLACK_CLIENT_SECRET}") private val slackClientSecret: String,
    private val slackRepository: SlackRepository,
    private val methods: MethodsClient,
    @Inject private var appConfig: AppConfig,
    private val httpClient: HttpClient,
) {
    private val instanceUrl = appConfig.getInstanceUrl()

    private val logger = LoggerFactory.getLogger(SlackService::class.java)

    fun generateAccessToken(
        organisationId: Long,
        code: String,
        state: String,
    ) {
        try {
            val redirectUri = "$instanceUrl/configuration/integrations"
            if (getDetails(organisationId) == null) {
                if (state == Constants.SLACK_ORIGINAL_STATE) {
                    val response =
                        methods.oauthV2Access { m ->
                            m
                                .code(code)
                                .clientId(slackClientId)
                                .clientSecret(slackClientSecret)
                                .redirectUri(redirectUri)
                        }
                    if (response.isOk) {
                        val accessToken = response.accessToken
                        val channelId = response.incomingWebhook.channelId
                        val webhookURL = response.incomingWebhook.url
                        val workspaceId = response.team.id
                        slackRepository.addDetails(organisationId, accessToken, channelId, webhookURL, workspaceId)
                    } else {
                        throw Exception("Failed to obtain access token")
                    }
                } else {
                    throw Exception("Unauthorized request")
                }
            } else {
                throw Exception("Already connected with one slack workspace")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    fun isSlackConnected(organisationId: Long): Any =
        if (getDetails(organisationId) != null) {
            mapOf("message" to true)
        } else {
            mapOf("message" to false)
        }

    private fun getDetails(organisationId: Long): SlackDetails? = slackRepository.getDetails(organisationId)

    fun disconnectSlack(organisationId: Long): Map<String, String> {
        try {
            val slackDetails = getDetails(organisationId)
            if (slackDetails != null) {
                val slackToken = slackDetails.accessToken
                val response = methods.authRevoke { m -> m.token(slackToken) }
                deleteDetails(organisationId)
                return mapOf("message" to "Slack disconnected successfully")
            } else {
                throw Exception("Slack is not connected for your organisation")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private fun deleteDetails(organisationId: Long) = slackRepository.deleteDetails(organisationId)

    // Sending message through webhook
    fun postMessageByWebhook(
        organisationId: Long,
        message: String,
    ): Boolean {
        val slackDetails = getDetails(organisationId)
        if (slackDetails == null) {
            logger.warn("Slack details not found for organisationId: $organisationId")
            return false
        }

        val payload = SlackMessage(message)
        val request =
            HttpRequest
                .POST(slackDetails.webhookURL, payload)
                .contentType(MediaType.APPLICATION_JSON)

        return try {
            val response: HttpResponse<String> = httpClient.toBlocking().exchange(request)
            val success = response.status.code in 200..299

            if (!success) {
                logger.warn("Slack webhook call failed with status: ${response.status.code}")
            }

            success
        } catch (ex: Exception) {
            logger.error("Exception occurred while posting Slack message for organisationId: $organisationId", ex)
            false
        }
    }

    fun sendBlockMessageToUser(
        organisationId: Long,
        blockMessage: List<LayoutBlock>,
        channelId: String,
        message: String,
    ) {
        val slackDetails = getDetails(organisationId)
        if (slackDetails != null) {
            val accessToken = slackDetails.accessToken
            methods.chatPostMessage { m ->
                m
                    .blocks(blockMessage)
                    .channel(channelId)
                    .token(accessToken)
                    .text(message)
            }
        }
    }

    fun blockBuilder(message: String): MutableList<LayoutBlock> {
        val markdownText = MarkdownTextObject.builder().text(message).build()
        return mutableListOf(
            SectionBlock.builder().text(markdownText).build(),
            SectionBlock.builder().text(MarkdownTextObject.builder().text("\n").build()).build(),
            DividerBlock.builder().build(),
            SectionBlock.builder().text(MarkdownTextObject.builder().text("\n").build()).build(),
        )
    }

    fun getUserByEmailId(
        emailId: String,
        organisationId: Long,
    ): User? {
        try {
            val slackToken = getDetails(organisationId)?.accessToken
            val response = methods.usersLookupByEmail { m -> m.email(emailId).token(slackToken) }
            return response.user
        } catch (e: Exception) {
            throw Exception("User not found")
        }
    }
}

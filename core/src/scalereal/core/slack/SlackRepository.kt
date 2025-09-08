package scalereal.core.slack

import scalereal.core.models.domain.SlackDetails

interface SlackRepository {
    fun addDetails(
        organisationId: Long,
        accessToken: String,
        channelId: String,
        webhookURL: String,
        workspaceId: String,
    )

    fun getDetails(organisationId: Long): SlackDetails?

    fun deleteDetails(organisationId: Long)

    fun getAccessToken(workspaceId: String): String?
}

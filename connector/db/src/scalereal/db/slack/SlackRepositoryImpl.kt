package scalereal.db.slack

import jakarta.inject.Inject
import jakarta.inject.Singleton
import norm.command
import norm.query
import scalereal.core.models.domain.SlackDetails
import scalereal.core.slack.SlackRepository
import slack.AddSlackDetailsCommand
import slack.AddSlackDetailsParams
import slack.DeleteSlackDetailsCommand
import slack.DeleteSlackDetailsParams
import slack.GetAccessTokenParams
import slack.GetAccessTokenQuery
import slack.GetSlackDetailsParams
import slack.GetSlackDetailsQuery
import javax.sql.DataSource

@Singleton
class SlackRepositoryImpl(
    @Inject private val dataSource: DataSource,
) : SlackRepository {
    override fun addDetails(
        organisationId: Long,
        accessToken: String,
        channelId: String,
        webhookURL: String,
        workspaceId: String,
    ): Unit =
        dataSource.connection.use { connection ->
            AddSlackDetailsCommand()
                .command(
                    connection,
                    AddSlackDetailsParams(
                        organisationId = organisationId,
                        accessToken = accessToken,
                        channelId = channelId,
                        webhook_url = webhookURL,
                        workspaceId = workspaceId,
                    ),
                )
        }

    override fun getDetails(organisationId: Long): SlackDetails? =
        dataSource.connection.use { connection ->
            GetSlackDetailsQuery()
                .query(connection, GetSlackDetailsParams(organisationId = organisationId))
                .map {
                    SlackDetails(
                        organisationId = organisationId,
                        accessToken = it.accessToken,
                        channelId = it.channelId,
                        webhookURL = it.webhookUrl,
                    )
                }.firstOrNull()
        }

    override fun deleteDetails(organisationId: Long) {
        dataSource.connection.use { connection ->
            DeleteSlackDetailsCommand()
                .command(connection, DeleteSlackDetailsParams(organisationId))
        }
    }

    override fun getAccessToken(workspaceId: String): String? =
        dataSource.connection.use { connection ->
            GetAccessTokenQuery()
                .query(connection, GetAccessTokenParams(workspaceId))
                .map { it.accessToken }
                .firstOrNull()
        }
}

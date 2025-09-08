package scalereal.db.authentication

import jakarta.inject.Inject
import jakarta.inject.Singleton
import norm.command
import norm.query
import refresh_tokens.FindByRefreshTokenParams
import refresh_tokens.FindByRefreshTokenQuery
import refresh_tokens.RevokeRefreshTokenCommand
import refresh_tokens.RevokeRefreshTokenParams
import refresh_tokens.SaveRefreshTokenParams
import refresh_tokens.SaveRefreshTokenQuery
import refresh_tokens.UpdateUserIdCommand
import refresh_tokens.UpdateUserIdParams
import scalereal.core.authentication.RefreshTokenRepository
import scalereal.core.models.RefreshTokenEntity
import javax.sql.DataSource

@Singleton
class RefreshTokenRepositoryImpl(
    @Inject private val dataSource: DataSource,
) : RefreshTokenRepository {
    override fun save(
        userId: Long,
        refreshToken: String,
        revoked: Boolean,
    ): RefreshTokenEntity =
        dataSource.connection.use { connection ->
            SaveRefreshTokenQuery()
                .query(connection, SaveRefreshTokenParams(userId, refreshToken, revoked))
                .map {
                    RefreshTokenEntity(it.id, it.userId, it.refreshToken, it.revoked, it.createdAt)
                }.first()
        }

    override fun findByRefreshToken(refreshToken: String): RefreshTokenEntity? =
        dataSource.connection.use { connection ->
            FindByRefreshTokenQuery()
                .query(connection, FindByRefreshTokenParams(refreshToken))
                .map {
                    RefreshTokenEntity(
                        id = it.id,
                        userId = it.userId,
                        refreshToken = it.refreshToken,
                        revoked = it.revoked,
                        createdAt = it.createdAt,
                    )
                }.firstOrNull()
        }

    override fun updateByUsername(
        userId: Long,
        revoked: Boolean,
    ): Long =
        dataSource.connection.use { connection ->
            UpdateUserIdCommand()
                .command(connection, UpdateUserIdParams(userId))
                .updatedRecordsCount
                .toLong()
        }

    override fun revokeRefreshToken(
        userId: Long,
        refreshToken: String,
    ): Unit =
        dataSource.connection.use { connection ->
            RevokeRefreshTokenCommand()
                .command(connection, RevokeRefreshTokenParams(userId = userId, refreshToken = refreshToken))
        }
}

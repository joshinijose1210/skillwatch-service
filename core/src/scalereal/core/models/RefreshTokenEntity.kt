package scalereal.core.models

import java.sql.Timestamp

data class RefreshTokenEntity(
    val id: Long? = null,
    val userId: Long,
    val refreshToken: String?,
    val revoked: Boolean?,
    val createdAt: Timestamp,
)

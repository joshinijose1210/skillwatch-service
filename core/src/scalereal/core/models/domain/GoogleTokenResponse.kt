package scalereal.core.models.domain

data class GoogleTokenResponse(
    val issued_to: String,
    val audience: String,
    val user_id: String,
    val scope: String,
    val expires_in: Int,
    val email: String,
    val verified_email: Boolean,
    val access_type: String,
)

data class GoogleToken(
    val id_token: String,
)

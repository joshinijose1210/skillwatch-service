package scalereal.api.common

data class ErrorMessage(
    val errorMessage: String,
)

object ErrorResponse {
    const val FORBIDDEN = "Access Denied! You do not have permission to access this route."
}

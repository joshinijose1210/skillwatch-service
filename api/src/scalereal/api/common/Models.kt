package scalereal.api.common

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus

data class Response<T>(
    val status: ResponseType,
    val message: String? = null,
    val body: T? = null,
    val headers: Map<CharSequence, CharSequence> = emptyMap(),
) {
    fun getHttpResponse(): HttpResponse<Any> =
        when (status) {
            ResponseType.SUCCESS -> HttpResponse.ok(body)
            ResponseType.CREATED -> HttpResponse.created(body)
            ResponseType.UNAUTHORIZED -> HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(mapOf("error" to body))
            ResponseType.NO_CONTENT -> HttpResponse.noContent()
            ResponseType.NOT_FOUND -> HttpResponse.notFound(message ?: body)
            ResponseType.FORBIDDEN -> HttpResponse.status<Any>(HttpStatus.FORBIDDEN).body(mapOf("error" to body))
            ResponseType.BAD_REQUEST -> HttpResponse.badRequest(message ?: body)
            else -> HttpResponse.serverError(message ?: "Something went wrong.")
        }
}

enum class ResponseType {
    SUCCESS,
    CREATED,
    NO_CONTENT,
    BAD_REQUEST,
    NOT_FOUND,
    FORBIDDEN,
    UNAUTHORIZED,
    SERVER_ERROR,
}

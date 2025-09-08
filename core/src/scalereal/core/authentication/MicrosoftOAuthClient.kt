package scalereal.core.authentication

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("\${microsoft.oauth.url}")
interface MicrosoftOAuthClient {
    @Post("/common/oauth2/v2.0/token")
    fun getToken(
        @Header("Content-Type") contentType: String,
        @Body requestBody: Map<String, Any>,
    ): HttpResponse<Map<String, Any>>
}

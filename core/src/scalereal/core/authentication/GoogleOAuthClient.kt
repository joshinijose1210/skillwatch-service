package scalereal.core.authentication

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("\${google.oauth.url}")
interface GoogleOAuthClient {
    @Post("/token")
    fun getToken(
        @Body requestBody: Map<String, Any>,
    ): HttpResponse<Map<String, Any>>
}

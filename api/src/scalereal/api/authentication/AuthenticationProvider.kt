package scalereal.api.authentication

import com.nimbusds.jose.shaded.gson.stream.JsonReader
import com.nimbusds.jose.shaded.gson.stream.JsonToken
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.reactivestreams.Publisher
import scalereal.core.authentication.AuthenticationService
import scalereal.core.models.AccessType
import java.util.Locale

@Singleton
class AuthenticationProvider : AuthenticationProvider {
    @Inject
    lateinit var authenticationService: AuthenticationService

    override fun authenticate(
        httpRequest: HttpRequest<*>?,
        authenticationRequest: AuthenticationRequest<*, *>?,
    ): Publisher<AuthenticationResponse> {
        val endpoint = httpRequest?.path
        val email = authenticationRequest?.identity.toString()
        return when (endpoint) {
            "/login/username" ->
                authenticationService.authenticationUsingSecret(
                    email = email.lowercase(Locale.getDefault()),
                    password = authenticationRequest?.secret.toString(),
                    accessType = AccessType.USER,
                )
            "/google/callback" -> authenticationService.googleLogin(code = getCode(httpRequest))
            "/login/super-admin" ->
                authenticationService.authenticationUsingSecret(
                    email = email.lowercase(Locale.getDefault()),
                    password = authenticationRequest?.secret.toString(),
                    accessType = AccessType.SUPER_ADMIN,
                )
            "/zoho/callback" -> authenticationService.zohoLogin(code = getCode(httpRequest))
            "/microsoft/callback" -> authenticationService.microsoftLogin(code = getCode(httpRequest))
            else -> throw AuthenticationResponse.exception("Something went wrong! Please try again")
        }
    }

    private fun getCode(httpRequest: HttpRequest<*>?): String? =
        httpRequest
            ?.body
            ?.orElse(null)
            ?.let {
                try {
                    val reader = JsonReader(it.toString().reader())
                    var code: String? = null

                    reader.beginObject()
                    while (reader.hasNext()) {
                        val name = reader.nextName()
                        if (name == "code" && reader.peek() == JsonToken.STRING) {
                            code = reader.nextString()
                            break
                        } else {
                            reader.skipValue()
                        }
                    }
                    reader.endObject()

                    code
                } catch (e: Exception) {
                    null
                }
            }
}

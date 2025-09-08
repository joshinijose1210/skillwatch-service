package scalereal.api.authentication

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.reactive.awaitFirstOrDefault
import scalereal.core.authentication.AuthenticationService
import scalereal.core.authentication.RefreshTokenService

@Tag(name = "Authentication")
@Controller
@Secured(SecurityRule.IS_ANONYMOUS)
@Singleton
class AuthenticationClient(
    private val authenticationProvider: AuthenticationProvider,
    private val authenticateRefreshToken: AuthenticateRefreshToken,
) {
    @Inject
    lateinit var authenticationService: AuthenticationService

    @Inject
    lateinit var refreshTokenService: RefreshTokenService

    @Operation(summary = "Login", description = "Login using username and password")
    @Post("/login/username")
    suspend fun login(
        @Body credentials: UsernamePasswordCredentials,
    ): AuthenticationResponse =
        authenticationProvider
            .authenticate(
                httpRequest = HttpRequest.POST("/login/username", credentials),
                credentials,
            ).awaitFirstOrDefault(AuthenticationResponse.failure("Something went wrong! Please try again"))

    @Operation(summary = "G-suite Login", description = "Google login")
    @Post("/login/google/callback")
    suspend fun googleLogin(
        @Body code: String,
    ): AuthenticationResponse =
        authenticationProvider
            .authenticate(
                httpRequest = HttpRequest.POST("/google/callback", code),
                authenticationRequest = null,
            ).awaitFirstOrDefault(AuthenticationResponse.failure("Something went wrong! Please try again"))

    @Operation(summary = "Login", description = "Login for super admin ")
    @Post("/login/super-admin")
    suspend fun superAdminLogin(
        @Body credentials: UsernamePasswordCredentials,
    ): AuthenticationResponse =
        authenticationProvider
            .authenticate(
                httpRequest = HttpRequest.POST("/login/super-admin", credentials),
                credentials,
            ).awaitFirstOrDefault(AuthenticationResponse.failure("Something went wrong! Please try again"))

    @Operation(summary = "ZOHO Login", description = "ZOHO login")
    @Post("/login/zoho/callback")
    suspend fun zohoLogin(
        @Body code: String,
    ): AuthenticationResponse =
        authenticationProvider
            .authenticate(
                httpRequest = HttpRequest.POST("/zoho/callback", code),
                authenticationRequest = null,
            ).awaitFirstOrDefault(AuthenticationResponse.failure("Something went wrong! Please try again"))

    @Operation(summary = "Microsoft Login", description = "Microsoft login")
    @Post("/login/microsoft/callback")
    suspend fun microsoftLogin(
        @Body code: String,
    ): AuthenticationResponse =
        authenticationProvider
            .authenticate(
                httpRequest = HttpRequest.POST("/microsoft/callback", code),
                authenticationRequest = null,
            ).awaitFirstOrDefault(AuthenticationResponse.failure("Something went wrong! Please try again"))

    @Operation(summary = "Generate token", description = "Generate auth token using refresh token")
    @Post("/login/refresh-token")
    suspend fun generateAuthToken(
        id: Long,
        refreshToken: String,
    ): AuthenticationResponse =
        authenticateRefreshToken
            .generateNewAuthentication(id = id, refreshToken = refreshToken)
            .awaitFirstOrDefault(AuthenticationResponse.failure("Something went wrong! Please login again."))

    @Operation(summary = "Logout")
    @Post("/logout")
    fun logout(
        id: Long,
        refreshToken: String,
    ): HttpResponse<Any> {
        val convertedUUID = refreshTokenService.convertToUuidFormat(refreshToken)
        authenticationService.logout(id = id, refreshToken = convertedUUID)
        return HttpResponse.ok("Logged out successfully!")
    }
}

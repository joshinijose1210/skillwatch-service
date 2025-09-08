package scalereal.core.authentication

import com.nimbusds.jwt.JWTParser
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.AuthenticationException
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.token.jwt.generator.AccessRefreshTokenGenerator
import io.micronaut.security.token.jwt.render.AccessRefreshToken
import jakarta.inject.Inject
import jakarta.inject.Singleton
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import scalereal.core.employees.EmployeeService
import scalereal.core.encryption.EncryptionService
import scalereal.core.models.AccessType
import scalereal.core.models.AppConfig
import scalereal.core.models.Constants.USER_ID_TOKEN_CLAIM
import scalereal.core.models.domain.ZohoToken
import scalereal.core.organisations.OrganisationService
import scalereal.core.user.UserService
import java.net.URLDecoder

@Singleton
class AuthenticationService(
    @Value("\${google.oauth.client-id}") private val googleClientId: String,
    @Value("\${google.oauth.client-secret}") private val googleClientSecret: String,
    @Value("\${ZOHO_CLIENT_ID}") private val zohoClientId: String,
    @Value("\${ZOHO_CLIENT_SECRET}") private val zohoClientSecret: String,
    @Value("\${microsoft.oauth.client-id}") private val microsoftClientId: String,
    @Value("\${microsoft.oauth.client-secret}") private val microsoftClientSecret: String,
    @Inject private var appConfig: AppConfig,
    private val refreshTokenRepository: RefreshTokenRepository,
    @param:Client("https://accounts.zoho.in") private val zohoAccountsClient: HttpClient,
    private val googleOAuthClient: GoogleOAuthClient,
    private val microsoftOAuthClient: MicrosoftOAuthClient,
) {
    @Inject
    lateinit var employeeService: EmployeeService

    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var organisationService: OrganisationService

    @Inject
    lateinit var accessRefreshTokenGenerator: AccessRefreshTokenGenerator

    @Inject
    lateinit var encryptionService: EncryptionService

    fun logout(
        id: Long,
        refreshToken: String,
    ) = refreshTokenRepository.revokeRefreshToken(userId = id, refreshToken = refreshToken)

    fun googleLogin(code: String?): Flux<AuthenticationResponse> {
        if (!code.isNullOrBlank()) {
            val email = exchangeGoogleCodeForToken(code = code)
            if (email.isNotBlank()) {
                return authenticate(email, loginFlow = "G-suite")
            } else {
                throw AuthenticationException("Something went wrong! Please login again to continue")
            }
        } else {
            throw AuthenticationException("Something went wrong! Please login again to continue")
        }
    }

    fun zohoLogin(code: String?): Flux<AuthenticationResponse> {
        if (!code.isNullOrBlank()) {
            val email = exchangeZohoCodeForToken(code = code)
            if (email.isNotBlank()) {
                return authenticate(email, loginFlow = "ZOHO")
            } else {
                throw AuthenticationException("Something went wrong! Please login again to continue")
            }
        } else {
            throw AuthenticationException("Something went wrong! Please login again to continue")
        }
    }

    fun microsoftLogin(code: String?): Flux<AuthenticationResponse> {
        if (!code.isNullOrBlank()) {
            val email = exchangeMicrosoftCodeForToken(code = code)
            if (email.isNotBlank()) {
                return authenticate(email, loginFlow = "Microsoft")
            } else {
                throw AuthenticationException("Something went wrong! Please login again to continue")
            }
        } else {
            throw AuthenticationException("Something went wrong! Please login again to continue")
        }
    }

    fun authenticate(
        email: String,
        loginFlow: String,
    ): Flux<AuthenticationResponse> =
        Flux.create(
            { emitter: FluxSink<AuthenticationResponse> ->
                if (employeeService.isEmployeeExists(email) && organisationService.isOrganisationActive(email)) {
                    val roles = userService.getRole(email)
                    val employeeId = employeeService.fetchByEmailId(email)?.id!!
                    val accessRefreshToken = generateToken(email, employeeId, roles)
                    val tokens =
                        mutableMapOf<String, Any>(
                            "access_token" to accessRefreshToken.accessToken,
                            "refresh_token" to accessRefreshToken.refreshToken,
                            "token_type" to accessRefreshToken.tokenType,
                            "expires_in" to accessRefreshToken.expiresIn,
                            "login_flow" to loginFlow,
                        )
                    emitter.next(AuthenticationResponse.success(email, tokens))
                    emitter.complete()
                } else if (userService.isUserExist(email)) {
                    throw AuthenticationResponse.exception("Please add organisation details!")
                } else {
                    throw AuthenticationException("Unauthorized Access! Please contact System Admin/HR")
                }
            },
            FluxSink.OverflowStrategy.ERROR,
        )

    fun authenticationUsingSecret(
        email: String,
        password: String?,
        accessType: AccessType,
    ): Flux<AuthenticationResponse> =
        Flux.create(
            { emitter: FluxSink<AuthenticationResponse> ->
                if (password.isNullOrBlank()) {
                    emitter.error(AuthenticationResponse.exception("Please enter your password and try again!"))
                }
                val (userId, role, secret) =
                    when (accessType) {
                        AccessType.SUPER_ADMIN -> {
                            if (!userService.isSuperAdmin(email)) {
                                throw AuthenticationException("Unauthorized Access! Please contact System Admin")
                            }
                            val user = userService.getUser(email)
                            Triple(user.id, listOf(AccessType.SUPER_ADMIN.toString()), userService.getSecret(email))
                        }

                        AccessType.USER -> {
                            if (!(
                                    employeeService.isEmployeeExists(email) &&
                                        organisationService.isOrganisationActive(
                                            email,
                                        )
                                )
                            ) {
                                if (userService.isUserExist(email)) {
                                    throw AuthenticationResponse.exception("Please add organisation details!")
                                } else {
                                    throw AuthenticationException("Unauthorized Access! Please contact System Admin/HR")
                                }
                            }
                            val employeeId = employeeService.fetchByEmailId(email)?.id!!
                            Triple(employeeId, userService.getRole(email), employeeService.getSecret(email))
                        }
                    }
                val encryptedPassword = password?.let { encryptionService.encryptPassword(it) }
                if (encryptedPassword == secret) {
                    val accessRefreshToken = generateToken(email, userId, role, AccessType.SUPER_ADMIN == accessType)
                    val tokens =
                        mutableMapOf<String, Any>(
                            "access_token" to accessRefreshToken.accessToken,
                            "refresh_token" to accessRefreshToken.refreshToken,
                            "token_type" to accessRefreshToken.tokenType,
                            "expires_in" to accessRefreshToken.expiresIn,
                            "login_flow" to "Username,Password",
                        )
                    emitter.next(AuthenticationResponse.success(email, role, tokens))
                    emitter.complete()
                } else {
                    emitter.error(AuthenticationResponse.exception("Your password is incorrect. Please try again."))
                }
            },
            FluxSink.OverflowStrategy.ERROR,
        )

    private fun exchangeGoogleCodeForToken(code: String): String {
        val instanceUrl = appConfig.getInstanceUrl()
        val redirectUri = removeWildcardsAndPath(instanceUrl)
        try {
            val decodedCode = URLDecoder.decode(code, "UTF-8")
            val requestBody =
                mapOf(
                    "code" to decodedCode,
                    "client_id" to googleClientId,
                    "client_secret" to googleClientSecret,
                    "redirect_uri" to redirectUri,
                    "grant_type" to "authorization_code",
                )

            val idToken =
                googleOAuthClient
                    .getToken(requestBody)
                    .body()
                    ?.get("id_token")
                    ?.toString()

            if (!idToken.isNullOrBlank()) {
                return getEmailFromIdToken(idToken)
            } else {
                throw AuthenticationException("Something went wrong! Please login again to continue")
            }
        } catch (_: Exception) {
            throw AuthenticationException("Something went wrong! Please login again to continue")
        }
    }

    private fun exchangeZohoCodeForToken(code: String): String {
        val instanceUrl = appConfig.getInstanceUrl()
        try {
            val decodedCode = URLDecoder.decode(code, "UTF-8")
            val requestBody =
                mapOf(
                    "code" to decodedCode,
                    "client_id" to zohoClientId,
                    "client_secret" to zohoClientSecret,
                    "redirect_uri" to "$instanceUrl/login",
                    "grant_type" to "authorization_code",
                )

            val tokenResponse =
                zohoAccountsClient
                    .toBlocking()
                    .exchange(
                        HttpRequest
                            .POST("/oauth/v2/token", requestBody)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED),
                        ZohoToken::class.java,
                    ).body()

            if (tokenResponse != null) {
                val idToken = tokenResponse.id_token
                return getEmailFromIdToken(idToken)
            } else {
                throw AuthenticationException("Something went wrong! Please login again to continue")
            }
        } catch (_: Exception) {
            throw AuthenticationException("Something went wrong! Please login again to continue")
        }
    }

    private fun exchangeMicrosoftCodeForToken(code: String): String {
        val instanceUrl = appConfig.getInstanceUrl()
        val codeVerifier = "Nj9Youq443xUOCe_HsmBXJy5dKC8YsqlUdn1sga3CR0"
        try {
            val decodedCode = URLDecoder.decode(code, "UTF-8")
            val requestBody =
                mapOf(
                    "code" to decodedCode,
                    "client_id" to microsoftClientId,
                    "client_secret" to microsoftClientSecret,
                    "redirect_uri" to "$instanceUrl/login",
                    "grant_type" to "authorization_code",
                    "code_verifier" to codeVerifier,
                )

            val idToken =
                microsoftOAuthClient
                    .getToken(
                        contentType = MediaType.APPLICATION_FORM_URLENCODED,
                        requestBody = requestBody,
                    ).body()
                    ?.get("id_token")
                    ?.toString()

            if (!idToken.isNullOrBlank()) {
                return getEmailFromIdToken(idToken)
            } else {
                throw AuthenticationException("Something went wrong! Please login again to continue")
            }
        } catch (_: Exception) {
            throw AuthenticationException("Something went wrong! Please login again to continue")
        }
    }

    private fun getEmailFromIdToken(idToken: String): String {
        val jwtParser = JWTParser.parse(idToken)
        val claims = jwtParser.jwtClaimsSet.toJSONObject()
        val email = claims["email"].toString()
        return email
    }

    private fun generateToken(
        username: String,
        userId: Long,
        roles: List<String>,
        isSuperAdmin: Boolean = false,
    ): AccessRefreshToken {
        val userType =
            mapOf(
                AccessType.SUPER_ADMIN.toString() to isSuperAdmin,
                USER_ID_TOKEN_CLAIM to userId,
            )
        val auth = Authentication.build(username, roles, userType)
        val accessRefreshToken = accessRefreshTokenGenerator.generate(auth)
        return accessRefreshToken.orElseThrow { AuthenticationException("Unauthorized Access! Please contact System Admin/HR") }
    }

    private fun removeWildcardsAndPath(url: String): String {
        val regex = Regex("://([^/]+)")
        val matchResult = regex.find(url)
        return if (matchResult != null) {
            val hostname = matchResult.groupValues[1].replace("*.", "")
            url.replaceAfter("://", hostname)
        } else {
            url
        }
    }
}

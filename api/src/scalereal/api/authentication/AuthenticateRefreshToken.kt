package scalereal.api.authentication

import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.AuthenticationException
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.errors.IssuingAnAccessTokenErrorCode
import io.micronaut.security.errors.OauthErrorResponseException
import io.micronaut.security.token.jwt.generator.AccessRefreshTokenGenerator
import io.micronaut.security.token.jwt.render.AccessRefreshToken
import jakarta.inject.Inject
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import scalereal.core.authentication.RefreshTokenService
import scalereal.core.employees.EmployeeService
import scalereal.core.models.AccessType
import scalereal.core.models.Constants.USER_ID_TOKEN_CLAIM
import scalereal.core.organisations.OrganisationService
import scalereal.core.user.UserService
import java.sql.Timestamp
import java.time.LocalDateTime

class AuthenticateRefreshToken(
    private val refreshTokenService: RefreshTokenService,
) {
    @Inject
    lateinit var employeeService: EmployeeService

    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var accessRefreshTokenGenerator: AccessRefreshTokenGenerator

    @Inject
    lateinit var organisationService: OrganisationService

    fun generateNewAuthentication(
        id: Long,
        refreshToken: String,
    ): Flux<AuthenticationResponse> {
        val convertedUuid = refreshTokenService.convertToUuidFormat(refreshToken)

        return Flux.create { emitter: FluxSink<AuthenticationResponse> ->
            val (user, tokenOpt) = refreshTokenService.findByRefreshToken(convertedUuid)

            tokenOpt?.let {
                val sixMonths = Timestamp.valueOf(LocalDateTime.now().minusMonths(6))

                if (!it.createdAt.before(sixMonths)) {
                    when {
                        id != user.id -> {
                            refreshTokenService.revokeRefreshToken(userId = user.id, refreshToken)
                            throw AuthenticationException("Session expired! Please login again to continue.")
                        }

                        !user.status -> throw AuthenticationException("Account deactivated! Please contact System Admin/HR")
                        it.revoked == true ->
                            emitter.error(
                                OauthErrorResponseException(
                                    IssuingAnAccessTokenErrorCode.INVALID_GRANT,
                                    "Session expired! Please login again.",
                                    null,
                                ),
                            )

                        else -> {
                            refreshTokenService.revokeRefreshToken(userId = user.id, refreshToken = convertedUuid)
                            when {
                                employeeService.isEmployeeExists(user.emailId) &&
                                    organisationService.isOrganisationActive(user.emailId) -> {
                                    val roles = userService.getRole(user.emailId)
                                    val accessRefreshToken = generateToken(user.emailId, user.id, roles)
                                    val tokens =
                                        mapOf(
                                            "access_token" to accessRefreshToken.accessToken,
                                            "refresh_token" to accessRefreshToken.refreshToken,
                                            "token_type" to accessRefreshToken.tokenType,
                                            "expires_in" to accessRefreshToken.expiresIn,
                                        )
                                    emitter.next(AuthenticationResponse.success(user.emailId, roles, tokens))
                                    emitter.complete()
                                }

                                userService.isUserExist(user.emailId) ->
                                    throw AuthenticationResponse.exception("Please add organisation details!")

                                else -> throw AuthenticationException("Unauthorized Access! Please contact System Admin/HR")
                            }
                        }
                    }
                } else {
                    refreshTokenService.revokeRefreshToken(userId = user.id, refreshToken = convertedUuid)
                    emitter.error(
                        OauthErrorResponseException(
                            IssuingAnAccessTokenErrorCode.INVALID_GRANT,
                            "Session expired! Please login again.",
                            null,
                        ),
                    )
                }
            } ?: emitter.error(
                OauthErrorResponseException(
                    IssuingAnAccessTokenErrorCode.INVALID_GRANT,
                    "Session expired! Please login again.",
                    null,
                ),
            )
        }
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
}

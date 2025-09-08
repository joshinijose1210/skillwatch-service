package scalereal.api.authentication

import io.micronaut.security.authentication.Authentication
import io.micronaut.security.errors.IssuingAnAccessTokenErrorCode.INVALID_GRANT
import io.micronaut.security.errors.OauthErrorResponseException
import io.micronaut.security.token.event.RefreshTokenGeneratedEvent
import io.micronaut.security.token.refresh.RefreshTokenPersistence
import jakarta.inject.Singleton
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import scalereal.core.authentication.RefreshTokenService
import scalereal.core.models.AccessType

@Singleton
class CustomRefreshTokenPersistence(
    private val refreshTokenService: RefreshTokenService,
) : RefreshTokenPersistence {
    override fun persistToken(event: RefreshTokenGeneratedEvent?) {
        val isSuperAdmin = event?.authentication?.attributes?.get(AccessType.SUPER_ADMIN.toString()) as Boolean
        if (event?.refreshToken != null && event.authentication?.name != null && !isSuperAdmin) {
            refreshTokenService.save(userName = event.authentication.name, token = event.refreshToken, revoked = false)
        }
    }

    override fun getAuthentication(refreshToken: String): Publisher<Authentication> =
        Flux.create(
            { emitter: FluxSink<Authentication> ->
                val (user, tokenOpt) = refreshTokenService.findByRefreshToken(refreshToken)
                if (tokenOpt != null) {
                    if (tokenOpt.revoked == true) {
                        emitter.error(OauthErrorResponseException(INVALID_GRANT, "refresh token revoked", null))
                    } else {
                        emitter.next(Authentication.build(user.emailId))
                        emitter.complete()
                    }
                } else {
                    emitter.error(OauthErrorResponseException(INVALID_GRANT, "refresh token not found", null))
                }
            },
            FluxSink.OverflowStrategy.ERROR,
        )
}

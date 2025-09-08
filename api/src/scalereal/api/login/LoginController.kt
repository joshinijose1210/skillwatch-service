package scalereal.api.login

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Singleton
import scalereal.api.common.ErrorMessage
import scalereal.api.common.Response
import scalereal.api.common.ResponseType
import scalereal.core.linkHandling.LinkHandlingService
import scalereal.core.login.LoginService
import java.lang.Exception

@Controller(value = "/api")
@Secured(SecurityRule.IS_ANONYMOUS)
@Singleton
class LoginController(
    private val loginService: LoginService,
    private val linkHandlingService: LinkHandlingService,
) {
    @Tag(name = "Password")
    @Operation(summary = "Set password")
    @Produces(MediaType.APPLICATION_JSON)
    @Post("/set-password")
    fun setPassword(
        password: String,
        emailId: String,
        linkId: String,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = loginService.setPassword(password, emailId, linkId),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Tag(name = "Email")
    @Operation(summary = "Send reset password email")
    @Post("/reset-password-email")
    fun resetPasswordEmail(emailId: String): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = loginService.resetPasswordEmail(emailId),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Tag(name = "Email")
    @Operation(summary = "Resend welcome email")
    @Get("/resend-welcome-email")
    fun resendWelcomeEmail(emailId: String): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = loginService.resendWelcomeEmail(emailId),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Tag(name = "Email")
    @Operation(summary = "Check email link validity")
    @Get("/check-link-validity")
    fun checkLinkValidity(linkId: String): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = linkHandlingService.checkLinkValidity(linkId),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
}

package scalereal.api.users

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
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
import scalereal.core.models.domain.User
import scalereal.core.user.UserService

@Tag(name = "Users")
@Controller(value = "/api/user")
@Secured(SecurityRule.IS_ANONYMOUS)
@Singleton
class UserController(
    private val userService: UserService,
) {
    @Operation(summary = "Create User", description = "Saves personal details of org admin at time of sign up")
    @Produces(MediaType.APPLICATION_JSON)
    @Post("/")
    fun createUser(
        firstName: String,
        lastName: String,
        emailId: String,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    userService.createUser(
                        User(
                            id = -99,
                            firstName = firstName,
                            lastName = lastName,
                            emailId = emailId,
                        ),
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
}

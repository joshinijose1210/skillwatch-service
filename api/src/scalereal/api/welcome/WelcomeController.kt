package scalereal.api.welcome

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import scalereal.core.models.Constants

@Tag(name = "Welcome")
@Controller
class WelcomeController {
    @Secured(SecurityRule.IS_ANONYMOUS)
    @Get(uri = "/")
    @ApiResponse(
        description = "Welcome Page",
    )
    fun welcome(): HttpResponse<String> = HttpResponse.ok("Welcome to ${Constants.APPLICATION_NAME}")
}

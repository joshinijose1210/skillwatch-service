package scalereal.api

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import scalereal.core.models.Constants

@SecurityScheme(
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
)
@OpenAPIDefinition(
    info =
        Info(
            title = "${Constants.APPLICATION_NAME} Service",
            version = "1.0",
            description = "${Constants.APPLICATION_NAME}'s APIs",
        ),
)
object ApiApplication {
    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut
            .build()
            .packages("scalereal.api")
            .mainClass(ApiApplication.javaClass)
            .start()
    }
}

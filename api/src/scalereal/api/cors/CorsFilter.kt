package scalereal.api.cors

import io.micronaut.context.annotation.Value
import io.micronaut.core.async.publisher.Publishers
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.ClientFilterChain
import io.micronaut.http.filter.HttpClientFilter
import org.reactivestreams.Publisher

@Filter("/**")
class CorsFilter(
    @Value("\${micronaut.server.cors.allowed.origins}") private val allowedOrigins: String,
) : HttpClientFilter {
    override fun doFilter(
        request: MutableHttpRequest<*>,
        chain: ClientFilterChain,
    ): Publisher<out HttpResponse<*>> {
        if (isPreflightRequest(request)) {
            val response =
                HttpResponse
                    .ok<Any>()
                    .header("Access-Control-Allow-Origin", allowedOrigins)
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH")
                    .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                    .header("Access-Control-Max-Age", "3600")
            return Publishers.just(response)
        } else {
            return Publishers.map(chain.proceed(request)) { response: HttpResponse<*> ->
                if (response is MutableHttpResponse<*>) {
                    response
                        .header("Access-Control-Allow-Origin", allowedOrigins)
                        .header("Access-Control-Max-Age", "3600")
                }
                response
            }
        }
    }

    private fun isPreflightRequest(request: MutableHttpRequest<*>): Boolean =
        request.method == HttpMethod.OPTIONS && request.headers.contains("Access-Control-Request-Method")
}

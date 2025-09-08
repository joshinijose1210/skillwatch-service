package scalereal.api.util

import io.micronaut.security.authentication.Authentication

fun getAuthenticatedUser(authentication: Authentication): Long =
    (authentication.attributes["user_id"] ?: error("User ID not present in token"))
        .let { (it as? Number)?.toLong() ?: (it as? String)?.toLongOrNull() ?: error("Invalid User ID") }

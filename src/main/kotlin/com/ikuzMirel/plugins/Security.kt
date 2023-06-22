package com.ikuzMirel.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ikuzMirel.security.token.TokenConfig
import com.ikuzMirel.session.WebSocketSession
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.sessions.*

fun Application.configureSecurity(config: TokenConfig) {

    authentication {
        jwt("auth-jwt") {
            realm = this@configureSecurity.environment.config.property("jwt.realm").getString()
            verifier(
                JWT
                    .require(Algorithm.HMAC256(config.secret))
                    .withAudience(config.audience)
                    .withIssuer(config.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(config.audience)) JWTPrincipal(credential.payload) else null
            }
        }
    }

    install(Sessions) {
        cookie<WebSocketSession>("SESSION")
    }

    intercept(ApplicationCallPipeline.Plugins) {
        if (call.sessions.get<WebSocketSession>() == null) {
            val sender = call.parameters["Uid"].orEmpty()

            if (sender.isNotEmpty()) {
                call.sessions.set(WebSocketSession(sender, generateSessionId()))
            }
        }
    }
}

package com.ikuzMirel.plugins

import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.dsl.AuthScheme
import io.github.smiley4.ktorswaggerui.dsl.AuthType
import io.ktor.server.application.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

fun Application.configureSwaggerUI() {
    val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    install(SwaggerUI) {
        swagger {
            swaggerUrl = "swagger-ui"
            forwardRoot = true
        }
        info {
            title = "Flick API"
            version = "latest"
            description = ""
        }
        server {
            url = "http://localhost:8080"
            description = "Development Server"
        }
        securityScheme("FlickJWTAuth") {
            type = AuthType.HTTP
            scheme = AuthScheme.BEARER
            bearerFormat = "JWT"
        }
        encoding {
            exampleEncoder { type, example ->
                json.encodeToString(serializer(type!!), example)
            }
        }
    }
}
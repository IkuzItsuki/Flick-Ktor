package com.ikuzMirel.util

import io.github.smiley4.ktorswaggerui.dsl.OpenApiRoute
import io.github.smiley4.ktorswaggerui.dsl.documentation
import io.ktor.server.routing.*
import io.ktor.server.websocket.*

fun Route.webSocket(
    path: String,
    builder: OpenApiRoute.() -> Unit = { },
    protocol: String? = null,
    handler: suspend DefaultWebSocketServerSession.() -> Unit
): Route {
    return documentation(builder) { webSocket(path, protocol, handler) }
}
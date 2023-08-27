package com.ikuzMirel.routes

import com.ikuzMirel.exception.WSUserAlreadyExistsException
import com.ikuzMirel.session.WebSocketSession
import com.ikuzMirel.util.webSocket
import com.ikuzMirel.websocket.WebSocketConnection
import com.ikuzMirel.websocket.WebSocketHandler
import io.github.smiley4.ktorswaggerui.dsl.get
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import java.util.concurrent.ConcurrentHashMap

fun Route.connectToWebsocket(
    webSocketHandler: WebSocketHandler
) {
    webSocket(path = "websocket", {
        tags = listOf("Websocket")
        description = "Connect to WebSocket"
        securitySchemeName = "FlickJWTAuth"
        request {
            queryParameter<String>("Uid") {
                description = "User ID"
                required = true
                example = "64d3fa5564bb17218acf795e"
            }
        }
    }) {
        val session = call.sessions.get<WebSocketSession>()

        if (session == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session found."))
            return@webSocket
        }

        try {
            webSocketHandler.onConnect(
                userId = session.userId,
                sessionId = session.sessionId,
                socket = this
            )
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    webSocketHandler.sendMessage(
                        userId = session.userId,
                        message = frame.readText()
                    )
                }
            }
        } catch (e: WSUserAlreadyExistsException) {
            call.respond(HttpStatusCode.Conflict)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            webSocketHandler.tryDisconnect(session.userId)
            println("User ${session.userId} disconnected.")
        }
    }
}

fun Route.showAllConnections(connections: ConcurrentHashMap<String, WebSocketConnection>) {
    get("connections", {
        tags = listOf("Websocket")
        description = "Show all WebSocket connections"
        securitySchemeName = "FlickJWTAuth"
        response {
            HttpStatusCode.OK to {
                body<List<String>> {
                    example(
                        "Default",
                        listOf(
                            "64d3fa5564bb17218acf795e",
                            "64d3fa5564bb17218acf795f",
                        )
                    )
                }
            }
        }
    }) {
        call.respond(HttpStatusCode.OK, connections.keys.toList())
    }
}
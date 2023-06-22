package com.ikuzMirel.routes

import com.ikuzMirel.exception.WSUserAlreadyExistsException
import com.ikuzMirel.session.WebSocketSession
import com.ikuzMirel.websocket.WebSocketHandler
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach

fun Route.connectToWebsocket(
    webSocketHandler: WebSocketHandler
) {
    webSocket(path = "websocket") {
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

fun Route.showAllConnections(webSocketHandler: WebSocketHandler) {
    get("connections") {
        call.respond(HttpStatusCode.OK, webSocketHandler.connections.keys.toList())
    }
}
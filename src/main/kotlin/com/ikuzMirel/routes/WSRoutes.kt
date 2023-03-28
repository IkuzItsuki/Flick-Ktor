package com.ikuzMirel.routes

import com.ikuzMirel.WebSocket.WSController
import com.ikuzMirel.exception.WSUserAlreadyExistsException
import com.ikuzMirel.session.WSSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach

fun Route.WebSocket(
    WSController: WSController
) {
    webSocket(path = "/WSocket") {
        val session = call.sessions.get<WSSession>() ?: kotlin.run {
            val username = call.parameters["Uid"]
            val newSession = WSSession(username!!, generateSessionId())
            call.sessions.set(newSession)
            newSession
        }
        try {
            WSController.onConnect(
                userId = session.userId,
                sessionId = session.sessionId,
                socket = this
            )
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    WSController.sendMessage(
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
            WSController.tryDisconnect(session.userId)
        }
    }
}

fun Route.getAllMessages(WSController: WSController) {
    authenticate {
        get("/messages/{id}") {
            val cid = call.parameters["id"]
            if (cid == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            call.respond(
                HttpStatusCode.OK,
                WSController.getAllMessages(cid)
            )
        }
    }
}
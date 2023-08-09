package com.ikuzMirel.routes

import com.ikuzMirel.data.message.MessageDataSource
import com.ikuzMirel.data.message.MessageWithCid
import com.ikuzMirel.data.responses.ChatMsgResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.getAllMessages(messageDataSource: MessageDataSource) {
    get("messages") {
        val cid = call.parameters["id"]
        if (cid == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val response = messageDataSource.getAllMessages(cid)

        val responseWithCid = response.map {
            MessageWithCid(
                it._id.toString(),
                it.content,
                it.senderUid,
                it.timestamp,
                cid
            )
        }

        call.respond(
            HttpStatusCode.OK,
            ChatMsgResponse(responseWithCid)
        )
    }
}
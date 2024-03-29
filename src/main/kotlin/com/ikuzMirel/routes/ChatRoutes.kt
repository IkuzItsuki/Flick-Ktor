package com.ikuzMirel.routes

import com.ikuzMirel.data.chatMessage.ChatMessageDataSource
import com.ikuzMirel.data.chatMessage.ChatMessageWithCid
import com.ikuzMirel.data.responses.ChatMsgResponse
import io.github.smiley4.ktorswaggerui.dsl.get
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.getAllMessages(chatMessageDataSource: ChatMessageDataSource) {
    get("messages", {
        description = "Get all messages "
        tags = listOf("Messages")
        securitySchemeName = "FlickJWTAuth"
        request {
            queryParameter<String>("id") {
                description = "The collection id of the chat"
                example = "64d3fa5564bb17218acf795e"
                required = true
            }
        }
        response {
            HttpStatusCode.BadRequest to {
                description = "The collection id is missing."
            }
            HttpStatusCode.OK to {
                body<ChatMsgResponse> {
                    example(
                        "Default",
                        ChatMsgResponse(
                            listOf(
                                ChatMessageWithCid(
                                    "5f9b1b4b4b9a9b1b4b9a9b1b",
                                    "Hello",
                                    "5f9b1b4b4b9a9b1b4b9a9b1b",
                                    1604050800,
                                    "64d3fa5564bb17218acf795e"
                                ),
                                ChatMessageWithCid(
                                    "5f9b1b4b4b9a9b1b4b9a9b1b",
                                    "Hello",
                                    "5f9b1b4b4b9a9b1b4b9a9b1b",
                                    1604050800,
                                    "64d3fa5564bb17218acf795e"
                                )
                            )
                        )
                    )
                }
            }
        }
    }) {
        val cid = call.parameters["id"]
        if (cid == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val response = chatMessageDataSource.getAllMessages(cid)

        val responseWithCid = response.map {
            ChatMessageWithCid(
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
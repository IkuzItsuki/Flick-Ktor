package com.ikuzMirel.routes

import com.ikuzMirel.data.chatMessage.ChatMessageDataSource
import com.ikuzMirel.data.friends.*
import com.ikuzMirel.data.requests.FriendReqRequest
import com.ikuzMirel.data.responses.FriendReqResponse
import com.ikuzMirel.data.user.UserDataSource
import com.ikuzMirel.mq.Message
import com.ikuzMirel.mq.Publisher
import com.ikuzMirel.websocket.WebSocketMessage
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.types.ObjectId

// Many of the conditions in these routes are not directly necessary, but they are here to prevent any possible problems
// like preventing people abusing the API if the server was leaked or something like that.

fun Route.sendFriendRequest(
    userDataSource: UserDataSource,
    friendRequestDataSource: FriendRequestDataSource,
    friendDataSource: FriendDataSource,
    friendRequestPublisher: Publisher
) {
    post("friendRequests/send", {
        tags = listOf("FriendRequest")
        description = "Send friend request"
        securitySchemeName = "FlickJWTAuth"
        request {
            body<FriendReqRequest> {
                description = "Friend request data"
                required = true
                example(
                    "Default",
                    FriendReqRequest(
                        "64d3fa5564bb17218acf795e",
                        "64d3fa5564bb17218acf795e"
                    )
                )
            }
        }
        response {
            HttpStatusCode.BadRequest to {
                description = "The request is not valid"
            }
            HttpStatusCode.Conflict to {
                body<String> {
                    example(
                        "ID mismatch",
                        "Friend request sender id does not match with token"
                    )
                    example(
                        "Friend request already exists",
                        "Friend request already exists"
                    )
                    example(
                        "Already Friend",
                        "User is already friend with the other user"
                    )
                    example(
                        "Database operation failed",
                        "Friend request not sent"
                    )
                }
            }
            HttpStatusCode.OK to {
                body<FriendRequest> {
                    example(
                        "Default",
                        FriendRequest(
                            "64d3fa5564bb17218acf795e",
                            "DemoUser",
                            "64d3fa5564bb17218acf795e",
                            "DemoUser2",
                            "PENDING"
                        )
                    )
                }
            }
        }
    }) {
        val request = call.receiveNullable<FriendReqRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val requestUserId = call.principal<JWTPrincipal>()?.getClaim("userId", String::class)
        if (requestUserId != request.senderId) {
            call.respond(HttpStatusCode.Conflict, "Friend request sender id does not match with token")
            return@post
        }

        val friendRequestFormDatabase =
            friendRequestDataSource.getFriendRequestByUsers(request.senderId, request.receiverId)
        if (friendRequestFormDatabase != null && friendRequestFormDatabase.status != FriendRequestStatus.REJECTED.name) {
            call.respond(HttpStatusCode.Conflict, "Friend request already exists")
            return@post
        }

        val friendAlreadyExists =
            friendDataSource.getAllFriends(request.senderId).find { it._id == ObjectId(request.receiverId) }
        if (friendAlreadyExists != null) {
            call.respond(HttpStatusCode.Conflict, "Friend already exists")
            return@post
        }

        val senderData = userDataSource.getUserById(request.senderId)
        val receiverData = userDataSource.getUserById(request.receiverId)

        val friendRequest = FriendRequest(
            senderId = request.senderId,
            senderName = senderData?.username ?: "",
            receiverId = request.receiverId,
            receiverName = receiverData?.username ?: "",
            status = FriendRequestStatus.PENDING.name,
        )
        val result = friendRequestDataSource.sendFriendRequest(friendRequest)
        if (!result) {
            call.respond(HttpStatusCode.Conflict, "Friend request not sent")
            return@post
        }

        val webSocketMessage = WebSocketMessage(
            type = "friendRequest",
            data = friendRequest
        )

        val parsedMessage = Json.encodeToString(webSocketMessage)
        friendRequestPublisher.publish(
            Message.FriendRequestMessage(
                targetUserId = request.receiverId,
                data = parsedMessage
            )
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = friendRequest
        )
    }
}

fun Route.cancelFriendRequest(
    friendRequestDataSource: FriendRequestDataSource,
    friendRequestPublisher: Publisher
) {
    post("friendRequests/cancel", {
        tags = listOf("FriendRequest")
        description = "Cancel friend request"
        securitySchemeName = "FlickJWTAuth"
        request {
            queryParameter<String>("id") {
                description = "Friend request id"
                required = true
                example = "64d3fa5564bb17218acf795e"
            }
        }
        response {
            HttpStatusCode.BadRequest to {
                description = "The request is not valid"
            }
            HttpStatusCode.Conflict to {
                body<String> {
                    example(
                        "No friend request found",
                        "Friend request does not exist"
                    )
                    example(
                        "ID mismatch",
                        "Friend request is not sent by user"
                    )
                    example(
                        "Database operation failed",
                        "Friend request not cancelled"
                    )
                }
            }
            HttpStatusCode.OK to {
                description = "Friend request cancelled"
            }
        }
    }) {
        val request = call.parameters["id"] ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        val requestUserId = call.principal<JWTPrincipal>()?.getClaim("userId", String::class)

        val friendRequest = friendRequestDataSource.getFriendRequestById(request)
        if (friendRequest == null) {
            call.respond(HttpStatusCode.Conflict, "Friend request does not exist")
            return@post
        }
        if (friendRequest.senderId != requestUserId) {
            call.respond(HttpStatusCode.Conflict, "Friend request is not sent by user")
            return@post
        }

        val result = friendRequestDataSource.cancelFriendRequest(request)
        if (!result) {
            call.respond(HttpStatusCode.Conflict, "Friend request not cancelled")
            return@post
        }

        friendRequestPublisher.publish(
            Message.FriendRequestMessage(
                targetUserId = friendRequest.receiverId,
                data = Json.encodeToString(
                    WebSocketMessage(
                        type = "friendRequest",
                        data = friendRequest.copy(status = FriendRequestStatus.CANCELED.name)
                    )
                )
            )
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = "Friend request cancelled"
        )
    }
}

fun Route.acceptFriendRequest(
    friendRequestDataSource: FriendRequestDataSource,
    friendDataSource: FriendDataSource,
    userDataSource: UserDataSource,
    chatMessageDataSource: ChatMessageDataSource,
    friendRequestPublisher: Publisher
) {
    post("friendRequests/accept", {
        tags = listOf("FriendRequest")
        description = "Accept friend request"
        securitySchemeName = "FlickJWTAuth"
        request {
            queryParameter<String>("id") {
                description = "Friend request id"
                required = true
                example = "64d3fa5564bb17218acf795e"
            }
        }
        response {
            HttpStatusCode.BadRequest to {
                description = "The request is not valid"
            }
            HttpStatusCode.Conflict to {
                body<String> {
                    example(
                        "No friend request found",
                        "Friend request does not exist"
                    )
                    example(
                        "ID mismatch",
                        "Friend request is accepted by wrong user"
                    )
                    example(
                        "Database operation failed",
                        "Friend request not cancelled"
                    )
                }
            }
            HttpStatusCode.OK to {
                description = "Friend request accepted"
            }
        }
    }) {
        val request = call.parameters["id"] ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        val requestUserId = call.principal<JWTPrincipal>()?.getClaim("userId", String::class)

        val friendRequest = friendRequestDataSource.getFriendRequestById(request)
        if (friendRequest == null) {
            call.respond(HttpStatusCode.Conflict, "Friend request does not exist")
            return@post
        }
        if (friendRequest.receiverId != requestUserId) {
            call.respond(HttpStatusCode.Conflict, "Friend request is accepted by wrong user")
            return@post
        }
        val newFriendRequest = friendRequest.copy(status = FriendRequestStatus.ACCEPTED.name)

        val result = friendRequestDataSource.acceptFriendRequest(request)
        if (!result) {
            call.respond(HttpStatusCode.Conflict, "Friend request not accepted")
            return@post
        }

        val sender = userDataSource.getUserById(friendRequest.senderId)
        if (sender == null) {
            call.respond(HttpStatusCode.Conflict, "User not found")
            return@post
        }
        val receiver = userDataSource.getUserById(friendRequest.receiverId)
        if (receiver == null) {
            call.respond(HttpStatusCode.Conflict, "User not found")
            return@post
        }

        val messageCollection = chatMessageDataSource.createMessageCollection()
        val senderAddFriend = friendDataSource.addFriend(
            sender._id.toString(),
            Friend(
                username = receiver.username,
                _id = receiver._id,
                collectionId = messageCollection
            )
        )
        val receiverAddFriend = friendDataSource.addFriend(
            receiver._id.toString(),
            Friend(
                username = sender.username,
                _id = sender._id,
                collectionId = messageCollection
            )
        )
        if (!senderAddFriend || !receiverAddFriend) {
            call.respond(HttpStatusCode.Conflict, "Friend request not accepted")
            return@post
        }

        val webSocketMessage = WebSocketMessage(
            type = "friendRequest",
            data = newFriendRequest
        )

        val parsedFriendRequest = Json.encodeToString(webSocketMessage)
        friendRequestPublisher.publish(
            Message.FriendRequestMessage(
                targetUserId = friendRequest.senderId,
                data = parsedFriendRequest
            )
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = "Friend request accepted"
        )
    }
}

fun Route.rejectFriendRequest(
    friendRequestDataSource: FriendRequestDataSource,
    friendRequestPublisher: Publisher
) {
    post("friendRequests/reject", {
        tags = listOf("FriendRequest")
        description = "Reject friend request"
        securitySchemeName = "FlickJWTAuth"
        request {
            queryParameter<String>("id") {
                description = "Friend request id"
                required = true
                example = "64d3fa5564bb17218acf795e"
            }
        }
        response {
            HttpStatusCode.BadRequest to {
                description = "The request is not valid"
            }
            HttpStatusCode.Conflict to {
                body<String> {
                    example(
                        "No friend request found",
                        "Friend request does not exist"
                    )
                    example(
                        "ID mismatch",
                        "Friend request is rejected by wrong user"
                    )
                    example(
                        "Database operation failed",
                        "Friend request not cancelled"
                    )
                }
            }
            HttpStatusCode.OK to {
                description = "Friend request rejected"
            }
        }
    }) {
        val request = call.parameters["id"] ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        val requestUserId = call.principal<JWTPrincipal>()?.getClaim("userId", String::class)

        val friendRequest = friendRequestDataSource.getFriendRequestById(request)
        if (friendRequest == null) {
            call.respond(HttpStatusCode.Conflict, "Friend request does not exist")
            return@post
        }
        if (friendRequest.receiverId != requestUserId) {
            call.respond(HttpStatusCode.Conflict, "Friend request is rejected by wrong user")
            return@post
        }
        val newFriendRequest = friendRequest.copy(status = FriendRequestStatus.REJECTED.name)

        val result = friendRequestDataSource.declineFriendRequest(request)
        if (!result) {
            call.respond(HttpStatusCode.Conflict, "Friend request not rejected")
            return@post
        }

        val webSocketMessage = WebSocketMessage(
            type = "friendRequest",
            data = newFriendRequest
        )

        val parsedFriendRequest = Json.encodeToString(webSocketMessage)
        friendRequestPublisher.publish(
            Message.FriendRequestMessage(
                targetUserId = friendRequest.senderId,
                data = parsedFriendRequest
            )
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = "Friend request rejected"
        )
    }
}

fun Route.getAllSentFriendRequests(
    friendRequestDataSource: FriendRequestDataSource
) {
    get("friendRequests/sent", {
        tags = listOf("FriendRequest")
        description = "Get all sent friend requests"
        securitySchemeName = "FlickJWTAuth"
        response {
            HttpStatusCode.OK to {
                body<FriendReqResponse> {
                    example(
                        "Default",
                        FriendReqResponse(
                            listOf(
                                FriendRequest(
                                    "64d3fa5564bb17218acf795e",
                                    "DemoUser",
                                    "64d3fa5564bb17218acf795e",
                                    "DemoUser2",
                                    "PENDING"
                                ),
                                FriendRequest(
                                    "64d3fa5564bb17218acf795e",
                                    "DemoUser",
                                    "64d3fa5564bb17218acf795e",
                                    "DemoUser2",
                                    "PENDING"
                                )
                            )
                        )
                    )
                    example(
                        "No friend requests",
                        FriendReqResponse(emptyList())
                    )
                }
            }
        }
    }) {
        val id = call.principal<JWTPrincipal>()?.getClaim("userId", String::class)!!
        val friendRequests = friendRequestDataSource.getAllSentFriendRequests(id)
        call.respond(
            status = HttpStatusCode.OK,
            message = FriendReqResponse(friendRequests)
        )
    }
}

fun Route.getAllReceivedFriendRequests(
    friendRequestDataSource: FriendRequestDataSource
) {
    get("friendRequests/received", {
        tags = listOf("FriendRequest")
        description = "Get all received friend requests"
        securitySchemeName = "FlickJWTAuth"
        response {
            HttpStatusCode.OK to {
                body<FriendReqResponse> {
                    example(
                        "Default",
                        FriendReqResponse(
                            listOf(
                                FriendRequest(
                                    "64d3fa5564bb17218acf795e",
                                    "DemoUser",
                                    "64d3fa5564bb17218acf795e",
                                    "DemoUser2",
                                    "PENDING"
                                ),
                                FriendRequest(
                                    "64d3fa5564bb17218acf795e",
                                    "DemoUser",
                                    "64d3fa5564bb17218acf795e",
                                    "DemoUser2",
                                    "PENDING"
                                )
                            )
                        )
                    )
                    example(
                        "No friend requests",
                        FriendReqResponse(emptyList())
                    )
                }
            }
        }
    }) {
        val id = call.principal<JWTPrincipal>()?.getClaim("userId", String::class)!!
        val friendRequests = friendRequestDataSource.getAllReceivedFriendRequests(id)
        call.respond(
            status = HttpStatusCode.OK,
            message = FriendReqResponse(friendRequests)
        )
    }
}
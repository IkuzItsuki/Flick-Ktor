package com.ikuzMirel.routes

import com.ikuzMirel.WebSocket.WSController
import com.ikuzMirel.data.friends.Friend
import com.ikuzMirel.data.friends.FriendDataSource
import com.ikuzMirel.data.friends.FriendRequest
import com.ikuzMirel.data.friends.FriendRequestDataSource
import com.ikuzMirel.data.message.MessageDataSource
import com.ikuzMirel.data.requests.FriendReqRequest
import com.ikuzMirel.data.responses.FriendListResponse
import com.ikuzMirel.data.responses.FriendReqResponse
import com.ikuzMirel.data.user.UserDataSource
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.types.ObjectId

fun Route.sendFriendRequest(
    friendRequestDataSource: FriendRequestDataSource,
    friendDataSource: FriendDataSource,
    wsController: WSController
) {

    authenticate {
        post("friendRequests/send") {
            val request = call.receiveNullable<FriendReqRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val friendAlreadyExists =
                friendDataSource.getAllFriends(request.senderId).find { it.id == ObjectId(request.receiverId) }
            if (friendAlreadyExists != null) {
                call.respond(HttpStatusCode.Conflict, "Friend already exists")
                return@post
            }

            val friendRequest = FriendRequest(
                senderId = request.senderId,
                receiverId = request.receiverId,
                status = "pending"
            )
            val result = friendRequestDataSource.sendFriendRequest(friendRequest)
            if (!result) {
                call.respond(HttpStatusCode.Conflict, "Friend request not sent")
                return@post
            }

            val parsedMessage = Json.encodeToString(friendRequest)
            if (wsController.connections[request.receiverId] != null) {
                wsController.connections[request.receiverId]?.socket?.send(Frame.Text(parsedMessage))
            }
            call.respond(
                status = HttpStatusCode.OK,
                message = "Friend request sent"
            )
        }
    }
}

fun Route.acceptFriendRequest(
    friendRequestDataSource: FriendRequestDataSource,
    friendDataSource: FriendDataSource,
    userDataSource: UserDataSource,
    messageDataSource: MessageDataSource,
    wsController: WSController
) {
    authenticate {
        post("friendRequests/{id}/accept") {
            val request = call.parameters["id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val result = friendRequestDataSource.acceptFriendRequest(request)
            if (!result) {
                call.respond(HttpStatusCode.Conflict, "Friend request not accepted")
                return@post
            }

            val friendRequest = friendRequestDataSource.getFriendRequestById(request)
            if (friendRequest == null) {
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

            val messageColletion = messageDataSource.createMessageCollection()

            val senderAddFriend = friendDataSource.addFriend(
                sender.id.toString(),
                Friend(
                    username = receiver.username,
                    id = receiver.id,
                    collectionId = messageColletion
                )
            )
            val receiverAddFriend = friendDataSource.addFriend(
                receiver.id.toString(),
                Friend(
                    username = sender.username,
                    id = sender.id,
                    collectionId = messageColletion
                )
            )
            if (!senderAddFriend || !receiverAddFriend) {
                call.respond(HttpStatusCode.Conflict, "Friend request not accepted")
                return@post
            }

            val friendRequestWS = friendRequestDataSource.getFriendRequestById(request)
            val parsedFriendRequest = Json.encodeToString(friendRequestWS)
            if (wsController.connections[friendRequest.receiverId] != null) {
                wsController.connections[friendRequest.receiverId]?.socket?.send(Frame.Text(parsedFriendRequest))
            }

            call.respond(
                status = HttpStatusCode.OK,
                message = "Friend request accepted"
            )
        }
    }
}

fun Route.declineFriendRequest(
    friendRequestDataSource: FriendRequestDataSource
) {
    authenticate {
        post("friendRequests/{id}/decline") {
            val request = call.parameters["id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val result = friendRequestDataSource.declineFriendRequest(request)
            if (!result) {
                call.respond(HttpStatusCode.Conflict, "Friend request not declined")
                return@post
            }
            call.respond(
                status = HttpStatusCode.OK,
                message = "Friend request declined"
            )
        }
    }
}

fun Route.getAllSentFriendRequests(
    friendRequestDataSource: FriendRequestDataSource
) {
    authenticate {
        get("friendRequests/{id}/sent") {
            val id = call.parameters["id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val friendRequests = friendRequestDataSource.getAllSentFriendRequests(id)
            if (friendRequests.isEmpty()) {
                call.respond(HttpStatusCode.OK, "No friend requests sent")
                return@get
            }
            call.respond(
                status = HttpStatusCode.OK,
                message = FriendReqResponse(friendRequests)
            )
        }
    }
}

fun Route.getAllReceivedFriendRequests(
    friendRequestDataSource: FriendRequestDataSource
) {
    authenticate {
        get("friendRequests/{id}/received") {
            val id = call.parameters["id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val friendRequests = friendRequestDataSource.getAllReceivedFriendRequests(id)
            if (friendRequests.isEmpty()) {
                call.respond(HttpStatusCode.OK, "No friend requests received")
                return@get
            }
            call.respond(
                status = HttpStatusCode.OK,
                message = FriendReqResponse(friendRequests)
            )
        }
    }
}

fun Route.getFriends(
    friendDataSource: FriendDataSource
) {
    authenticate {
        get("user/{id}/friends") {
            val id = call.parameters["id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val friends = friendDataSource.getAllFriends(id)
            if (friends.isEmpty()) {
                call.respond(HttpStatusCode.OK, "No friends found")
                return@get
            }
            call.respond(
                status = HttpStatusCode.OK,
                message = FriendListResponse(friends)
            )
        }
    }
}
package com.ikuzMirel.routes

import com.ikuzMirel.data.friends.Friend
import com.ikuzMirel.data.friends.FriendDataSource
import com.ikuzMirel.data.friends.FriendRequest
import com.ikuzMirel.data.friends.FriendRequestDataSource
import com.ikuzMirel.data.message.MessageDataSource
import com.ikuzMirel.data.requests.FriendReqRequest
import com.ikuzMirel.data.responses.FriendListResponse
import com.ikuzMirel.data.responses.FriendReqResponse
import com.ikuzMirel.data.responses.UserListResponse
import com.ikuzMirel.data.user.UserDataSource
import com.ikuzMirel.data.user.UserSearchResult
import com.ikuzMirel.websocket.WebSocketHandler
import com.ikuzMirel.websocket.WebSocketMessage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.types.ObjectId

// Many of conditions in these routes are not directly necessary, but they are here to prevent any possible problems
// like preventing people abusing the API if the server was leaked or something like that.

fun Route.sendFriendRequest(
    friendRequestDataSource: FriendRequestDataSource,
    friendDataSource: FriendDataSource,
    webSocketHandler: WebSocketHandler
) {

    post("friendRequests/send") {
        val request = call.receiveNullable<FriendReqRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val requestUserId = call.principal<JWTPrincipal>()?.getClaim("userId", String::class)
        if (requestUserId != request.senderId) {
            call.respond(HttpStatusCode.Conflict, "Friend request sender id does not match with token")
            return@post
        }

        val friendAlreadyExists =
            friendDataSource.getAllFriends(request.senderId).find { it.userId == ObjectId(request.receiverId) }
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

        val webSocketMessage = WebSocketMessage(
            type = "friendRequest",
            data = friendRequest
        )
        val parsedMessage = Json.encodeToString(webSocketMessage)
        if (webSocketHandler.connections[request.receiverId]?.socket != null) {
            webSocketHandler.connections[request.receiverId]?.socket?.send(Frame.Text(parsedMessage))
        }

        call.respond(
            status = HttpStatusCode.OK,
            message = "Friend request sent"
        )
    }
}

fun Route.cancelFriendRequest(
    friendRequestDataSource: FriendRequestDataSource
) {
    post("friendRequests/cancel") {
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
    messageDataSource: MessageDataSource,
    webSocketHandler: WebSocketHandler
) {
    post("friendRequests/accept") {
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

        val messageCollection = messageDataSource.createMessageCollection()
        val senderAddFriend = friendDataSource.addFriend(
            sender.id.toString(),
            Friend(
                username = receiver.username,
                userId = receiver.id,
                collectionId = messageCollection
            )
        )
        val receiverAddFriend = friendDataSource.addFriend(
            receiver.id.toString(),
            Friend(
                username = sender.username,
                userId = sender.id,
                collectionId = messageCollection
            )
        )
        if (!senderAddFriend || !receiverAddFriend) {
            call.respond(HttpStatusCode.Conflict, "Friend request not accepted")
            return@post
        }

        val webSocketMessage = WebSocketMessage(
            type = "friendRequest",
            data = friendRequest
        )
        val parsedFriendRequest = Json.encodeToString(webSocketMessage)
        if (webSocketHandler.connections[friendRequest.senderId]?.socket != null) {
            webSocketHandler.connections[friendRequest.senderId]?.socket?.send(Frame.Text(parsedFriendRequest))
        }

        call.respond(
            status = HttpStatusCode.OK,
            message = "Friend request accepted"
        )
    }
}

fun Route.declineFriendRequest(
    friendRequestDataSource: FriendRequestDataSource
) {
    post("friendRequests/decline") {
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

fun Route.getAllSentFriendRequests(
    friendRequestDataSource: FriendRequestDataSource
) {
    get("friendRequests/sent") {
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

fun Route.getAllReceivedFriendRequests(
    friendRequestDataSource: FriendRequestDataSource
) {
    get("friendRequests/received") {
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

fun Route.searchForFriends(
    userDataSource: UserDataSource,
    friendDataSource: FriendDataSource
) {
    get("user/search") {
        val username = call.parameters["username"] ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val requestUserId = call.principal<JWTPrincipal>()?.getClaim("userId", String::class)!!

        val users = userDataSource.getUsersByName(username)
        if (users.isEmpty()) {
            call.respond(HttpStatusCode.OK, UserListResponse(emptyList()))
            return@get
        }

        val friends = friendDataSource.getAllFriends(requestUserId)
        val filteredUser = users.filter {
            it.id.toString() != requestUserId
        }

        val result = filteredUser.map {
            val friendWithMe = friends.any { friend ->
                friend.userId == it.id
            }
            UserSearchResult(
                userId = it.id.toString(),
                username = it.username,
                friendWithMe = friendWithMe
            )
        }

        call.respond(
            status = HttpStatusCode.OK,
            message = UserListResponse(result)
        )
    }
}

fun Route.getFriends(
    friendDataSource: FriendDataSource
) {
    get("user/friends") {
        val id = call.parameters["id"] ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }
        val friends = friendDataSource.getAllFriends(id)
        if (friends.isEmpty()) {
            call.respond(HttpStatusCode.OK, FriendListResponse(emptyList()))
            return@get
        }
        println(FriendListResponse(friends))
        call.respond(
            status = HttpStatusCode.OK,
            message = FriendListResponse(friends)
        )
    }
}
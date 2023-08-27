package com.ikuzMirel.plugins

import com.ikuzMirel.data.auth.AuthSource
import com.ikuzMirel.data.chatMessage.ChatMessageDataSource
import com.ikuzMirel.data.friends.FriendDataSource
import com.ikuzMirel.data.friends.FriendRequestDataSource
import com.ikuzMirel.data.user.UserDataSource
import com.ikuzMirel.mq.Publisher
import com.ikuzMirel.routes.*
import com.ikuzMirel.security.hashing.HashingService
import com.ikuzMirel.security.token.TokenConfig
import com.ikuzMirel.security.token.TokenService
import com.ikuzMirel.websocket.WebSocketConnection
import com.ikuzMirel.websocket.WebSocketHandler
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject
import java.util.concurrent.ConcurrentHashMap

fun Application.configureRouting(
    hashingService: HashingService,
    tokenConfig: TokenConfig,
    tokenService: TokenService
) {
    val webSocketHandler by inject<WebSocketHandler>()
    val authSource by inject<AuthSource>()
    val userDataSource by inject<UserDataSource>()
    val friendDataSource by inject<FriendDataSource>()
    val friendRequestDataSource by inject<FriendRequestDataSource>()
    val chatMessageDataSource by inject<ChatMessageDataSource>()
    val connections by inject<ConcurrentHashMap<String, WebSocketConnection>>()
    val friendRequestPublisher by inject<Publisher>(named("friendRequestPublisher"))

    routing {
        //Authentication
        signIn(authSource, hashingService, tokenService, tokenConfig)
        signUp(hashingService, authSource, userDataSource)

        authenticate("auth-jwt") {
            authenticate()

            //User info
            getUserInfo(userDataSource)

            //Friend
            getFriends(friendDataSource)
            getFriend(friendDataSource)

            //FriendRequest
            getAllSentFriendRequests(friendRequestDataSource)
            getAllReceivedFriendRequests(friendRequestDataSource)
            sendFriendRequest(userDataSource, friendRequestDataSource, friendDataSource, friendRequestPublisher)
            cancelFriendRequest(friendRequestDataSource, friendRequestPublisher)
            acceptFriendRequest(
                friendRequestDataSource,
                friendDataSource,
                userDataSource,
                chatMessageDataSource,
                friendRequestPublisher
            )
            rejectFriendRequest(friendRequestDataSource, friendRequestPublisher)
            searchForFriends(userDataSource, friendDataSource)

            //Websocket
            connectToWebsocket(webSocketHandler)
            showAllConnections(connections)

            //Message
            getAllMessages(chatMessageDataSource)
        }
    }
}

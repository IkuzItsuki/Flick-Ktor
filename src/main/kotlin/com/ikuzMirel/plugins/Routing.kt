package com.ikuzMirel.plugins

import com.ikuzMirel.data.auth.AuthSource
import com.ikuzMirel.data.friends.FriendDataSource
import com.ikuzMirel.data.friends.FriendRequestDataSource
import com.ikuzMirel.data.message.MessageDataSource
import com.ikuzMirel.data.user.UserDataSource
import com.ikuzMirel.WebSocket.WSController
import com.ikuzMirel.routes.*
import com.ikuzMirel.security.hashing.HashingService
import com.ikuzMirel.security.token.TokenConfig
import com.ikuzMirel.security.token.TokenService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting(
    hashingService: HashingService,
    tokenConfig: TokenConfig,
    tokenService: TokenService
) {
    val WSController by inject<WSController>()
    val authSource by inject<AuthSource>()
    val userDataSource by inject<UserDataSource>()
    val friendDataSource by inject<FriendDataSource>()
    val friendRequestDataSource by inject<FriendRequestDataSource>()
    val messageDataSource by inject<MessageDataSource>()
    routing {
        //Authentication
        signIn(authSource, hashingService, tokenService, tokenConfig)
        signUp(hashingService, authSource, userDataSource)
        authenticate()
        getSecretInfo()

        //User info
        getUserInfo(userDataSource)

        //Friend
        getFriends(friendDataSource)

        //FriendRequest
        getAllSentFriendRequests(friendRequestDataSource)
        getAllReceivedFriendRequests(friendRequestDataSource)
        sendFriendRequest(friendRequestDataSource, friendDataSource, WSController)
        acceptFriendRequest(friendRequestDataSource, friendDataSource, userDataSource, messageDataSource, WSController)
        declineFriendRequest(friendRequestDataSource)

        //Message
        WebSocket(WSController)
        getAllMessages(WSController)

    }
}

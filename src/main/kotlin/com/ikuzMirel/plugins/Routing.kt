package com.ikuzMirel.plugins

import com.ikuzMirel.authenticate
import com.ikuzMirel.data.user.UserDataSource
import com.ikuzMirel.getSecretInfo
import com.ikuzMirel.room.RoomController
import com.ikuzMirel.routes.chatSocket
import com.ikuzMirel.routes.getAllMessages
import com.ikuzMirel.security.hashing.HashingService
import com.ikuzMirel.security.token.TokenConfig
import com.ikuzMirel.security.token.TokenService
import com.ikuzMirel.signIn
import com.ikuzMirel.signUp
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenConfig: TokenConfig,
    tokenService: TokenService
) {
    val roomController by inject<RoomController>()
    routing {
        signIn(userDataSource, hashingService, tokenService, tokenConfig)
        signUp(hashingService, userDataSource)
        authenticate()
        getSecretInfo()
        chatSocket(roomController)
        getAllMessages(roomController)
    }
}

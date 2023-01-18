package com.ikuzMirel.plugins

import com.ikuzMirel.authenticate
import com.ikuzMirel.data.user.UserDataSouce
import com.ikuzMirel.getSecretInfo
import com.ikuzMirel.security.hashing.HashingService
import com.ikuzMirel.security.token.TokenConfig
import com.ikuzMirel.security.token.TokenService
import com.ikuzMirel.signIn
import com.ikuzMirel.signUp
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun Application.configureRouting(
    userDataSouce: UserDataSouce,
    hashingService: HashingService,
    tokenConfig: TokenConfig,
    tokenService: TokenService
) {

    routing {
        signIn(userDataSouce, hashingService, tokenService, tokenConfig)
        signUp(hashingService, userDataSouce)
        authenticate()
        getSecretInfo()
    }
}

package com.ikuzMirel.plugins

import com.ikuzMirel.*
import com.ikuzMirel.data.user.UserDataSource
import com.ikuzMirel.security.hashing.HashingService
import com.ikuzMirel.security.token.TokenConfig
import com.ikuzMirel.security.token.TokenService
import io.ktor.server.routing.*
import io.ktor.server.application.*

fun Application.configureRouting(
    userDataSouce: UserDataSource,
    hashingService: HashingService,
    tokenConfig: TokenConfig,
    tokenService: TokenService
) {

    routing {
        signIn(userDataSouce, hashingService, tokenService, tokenConfig)
        signUp(hashingService, userDataSouce)
        authenticate()
        getSecretInfo()
        getUserInfo(userDataSouce)
    }
}

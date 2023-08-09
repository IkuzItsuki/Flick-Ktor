package com.ikuzMirel.routes

import com.ikuzMirel.data.auth.Auth
import com.ikuzMirel.data.auth.AuthSource
import com.ikuzMirel.data.requests.AuthRequest
import com.ikuzMirel.data.responses.AuthResponse
import com.ikuzMirel.data.user.User
import com.ikuzMirel.data.user.UserDataSource
import com.ikuzMirel.security.hashing.HashingService
import com.ikuzMirel.security.hashing.SaltedHash
import com.ikuzMirel.security.token.TokenClaim
import com.ikuzMirel.security.token.TokenConfig
import com.ikuzMirel.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.signUp(
    hashingService: HashingService,
    authSource: AuthSource,
    userDataSource: UserDataSource
) {
    post("signUp") {
        val request = call.receiveNullable<AuthRequest>() ?: run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val isUserAlreadyExists = authSource.getAuthByUsername(request.username)
        if (isUserAlreadyExists != null) {
            call.respond(HttpStatusCode.Conflict, "User already exists")
            return@post
        }

        val areFieldsBlank = request.username.isBlank() || request.password.isBlank()
        val isPwTooShort = request.password.length < 8
        if (areFieldsBlank || isPwTooShort) {
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)

        val auth = Auth(
            username = request.username,
            password = saltedHash.hash,
            salt = saltedHash.salt
        )
        val userData = User(
            username = request.username,
            email = request.email,
            _id = auth._id
        )

        val authWasAcknowledged = authSource.insertAuth(auth)
        val userWasAcknowledged = userDataSource.insertUser(userData)
        if (!authWasAcknowledged || !userWasAcknowledged) {
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        call.respond(HttpStatusCode.OK)
    }
}

fun Route.signIn(
    authSource: AuthSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("signIn") {
        val request = call.receiveNullable<AuthRequest>() ?: run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = authSource.getAuthByUsername(request.username)
        if (user == null) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }

        val isValidPassword = hashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )
        if (!isValidPassword) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user._id.toString()
            )
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = AuthResponse(
                token = token,
                username = user.username,
                userId = user._id.toString()
            )
        )
    }
}

fun Route.authenticate() {
    get("authenticate") {
        call.respond(HttpStatusCode.OK)
    }
}

fun Route.getSecretInfo() {
    get("secret") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.getClaim("userId", String::class)
        call.respond(HttpStatusCode.OK, "UserId is $userId")
    }
}
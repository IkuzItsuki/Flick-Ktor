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
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.signUp(
    hashingService: HashingService,
    authSource: AuthSource,
    userDataSource: UserDataSource
) {
    post("signUp", {
        tags = listOf("Auth")
        description = "Sign up a new user."
        request {
            body<AuthRequest> {
                example(
                    "Default",
                    AuthRequest(
                        "DemoUser",
                        "Aa123456",
                        "abc@abc.com"
                    )
                )
                required = true
            }
        }
        response {
            HttpStatusCode.BadRequest to {
                description = "Invalid request."
            }
            HttpStatusCode.Conflict to {
                body<String> {
                    example("User already exists", "User already exists")
                    example("Incorrect credentials", "Username or password is incorrect")
                    example("Write operation error", "The data could not be saved")
                }
            }
            HttpStatusCode.OK to {
            }
        }
    }) {

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
            call.respond(HttpStatusCode.Conflict, "Username or password is incorrect")
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
            call.respond(HttpStatusCode.Conflict, "The data could not be saved")
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
    post("signIn", {
        tags = listOf("Auth")
        description = "Sign in a user."
        request {
            body<AuthRequest> {
                example(
                    "Default",
                    AuthRequest(
                        "DemoUser",
                        "Aa123456",
                        ""
                    )
                )
                required = true
            }
        }
        response {
            HttpStatusCode.BadRequest to {
                description = "Invalid request."
            }
            HttpStatusCode.Conflict to {
                description = "Incorrect username or password."
            }
            HttpStatusCode.OK to {
                body<AuthResponse> {
                    example(
                        "Default",
                        AuthResponse(
                            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
                            "DemoUser",
                            "64d3f88564bb17218acf795a"
                        )
                    )
                }
            }
        }
    }) {
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
    get("authenticate", {
        tags = listOf("Auth")
        description = "Authenticate a user."
        securitySchemeName = "FlickJWTAuth"
        response {
            HttpStatusCode.Unauthorized to {
                description = "Unauthorized."
            }
            HttpStatusCode.OK to {
            }
        }
    }) {
        call.respond(HttpStatusCode.OK)
    }
}
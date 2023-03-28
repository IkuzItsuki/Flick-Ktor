package com.ikuzMirel.routes

import com.ikuzMirel.data.responses.InfoResponse
import com.ikuzMirel.data.user.UserDataSource
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.getUserInfo(
    userDataSource: UserDataSource
) {
    authenticate {
        get("user/{id}") {
            val request = call.parameters["id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadGateway)
                return@get
            }
            val userData = userDataSource.getUserById(request)
            if (userData == null){
                call.respond(HttpStatusCode.OK, "User not found")
                return@get
            }
            call.respond(
                status = HttpStatusCode.OK,
                message = InfoResponse(
                    username = userData.username,
                    email = userData.email
                )
            )
        }
    }
}
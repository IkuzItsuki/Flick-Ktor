package com.ikuzMirel.routes

import com.ikuzMirel.data.friends.Friend
import com.ikuzMirel.data.friends.FriendDataSource
import com.ikuzMirel.data.responses.FriendListResponse
import com.ikuzMirel.data.responses.InfoResponse
import com.ikuzMirel.data.responses.UserListResponse
import com.ikuzMirel.data.user.UserDataSource
import com.ikuzMirel.data.user.UserSearchResult
import io.github.smiley4.ktorswaggerui.dsl.get
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId

fun Route.getUserInfo(
    userDataSource: UserDataSource
) {
    get("user", {
        tags = listOf("User")
        description = "Get user info"
        securitySchemeName = "FlickJWTAuth"
        request {
            queryParameter<String>("id") {
                description = "User id"
                required = true
                example = "64d3fa5564bb17218acf795e"
            }
        }
        response {
            HttpStatusCode.Conflict to {
                description = "User not found"
            }
            HttpStatusCode.OK to {
                body<InfoResponse> {
                    example(
                        "Default",
                        InfoResponse(
                            "DemoUser",
                            "abc@abc.com"
                        )
                    )
                }
            }
        }
    }) {
        val request = call.parameters["id"] ?: run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val userData = userDataSource.getUserById(request)
        if (userData == null) {
            call.respond(HttpStatusCode.Conflict, "User not found")
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

fun Route.searchForFriends(
    userDataSource: UserDataSource,
    friendDataSource: FriendDataSource
) {
    get("user/search", {
        tags = listOf("User")
        description = "Search for users"
        securitySchemeName = "FlickJWTAuth"
        request {
            queryParameter<String>("username") {
                required = true
                example = "DemoUser"
            }
        }
        response {
            HttpStatusCode.BadRequest to {
                description = "The request was not valid"
            }
            HttpStatusCode.OK to {
                body<UserListResponse> {
                    example(
                        "Default",
                        UserListResponse(
                            listOf(
                                UserSearchResult(
                                    userId = "64d3fa5564bb17218acf795e",
                                    username = "DemoUser",
                                    friendWithMe = false,
                                    collectionId = ""
                                )
                            )
                        )
                    )
                    example(
                        "No results",
                        UserListResponse(emptyList())
                    )
                }
            }
        }
    }) {
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
        val userListWithoutRequester = users.filter {
            it._id.toString() != requestUserId
        }

        val result = userListWithoutRequester.map {
            val friendWithMe = friends.any { friend ->
                friend._id == it._id
            }

            UserSearchResult(
                userId = it._id.toString(),
                username = it.username,
                friendWithMe = friendWithMe,
                collectionId = if (friendWithMe) {
                    friends.first { friend ->
                        friend._id == it._id
                    }.collectionId
                } else {
                    ""
                }
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
    get("user/friends", {
        tags = listOf("User")
        description = "Get all friends of user"
        securitySchemeName = "FlickJWTAuth"
        response {
            HttpStatusCode.OK to {
                body<FriendListResponse> {
                    example(
                        "Default",
                        FriendListResponse(
                            listOf(
                                Friend(
                                    _id = ObjectId("64d3fa5564bb17218acf795e"),
                                    username = "DemoUser",
                                    collectionId = "64d3fa5564bb17218acf795e"
                                )
                            )
                        )
                    )
                    example(
                        "No friends",
                        FriendListResponse(emptyList())
                    )
                }
            }
        }
    }) {
        val id = call.principal<JWTPrincipal>()?.getClaim("userId", String::class)!!

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

fun Route.getFriend(
    friendDataSource: FriendDataSource
) {
    get("user/friend", {
        tags = listOf("User")
        description = "Get friend by id"
        securitySchemeName = "FlickJWTAuth"
        request {
            queryParameter<String>("friendId") {
                required = true
                example = "64d3fa5564bb17218acf795e"
            }
        }
        response {
            HttpStatusCode.BadRequest to {
                description = "The request was not valid"
            }
            HttpStatusCode.Conflict to {
                description = "Friend not found"
            }
            HttpStatusCode.OK to {
                body<Friend> {
                    example(
                        "Default",
                        Friend(
                            _id = ObjectId("64d3fa5564bb17218acf795e"),
                            username = "DemoUser",
                            collectionId = "64d3fa5564bb17218acf795e"
                        )
                    )
                }
            }
        }
    }) {
        val id = call.principal<JWTPrincipal>()?.getClaim("userId", String::class)!!

        val friendId = call.parameters["friendId"] ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }
        val friend = friendDataSource.getFriendById(id, friendId) ?: run {
            call.respond(HttpStatusCode.Conflict, "Friend not found")
            return@get
        }

        call.respond(
            status = HttpStatusCode.OK,
            message = friend
        )
    }
}
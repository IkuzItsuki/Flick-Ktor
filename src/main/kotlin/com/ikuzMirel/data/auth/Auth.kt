package com.ikuzMirel.data.auth

import org.bson.types.ObjectId

data class Auth(
    val username: String,
    val password: String,
    val salt: String,
    val _id: ObjectId = ObjectId()
)

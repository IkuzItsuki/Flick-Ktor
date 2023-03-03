package com.ikuzMirel.data.user

interface UserDataSource {
    suspend fun getUserByUsername(username: String): User?
    suspend fun getUserByUserId(id: String): User?
    suspend fun insertUser(user: User): Boolean
}
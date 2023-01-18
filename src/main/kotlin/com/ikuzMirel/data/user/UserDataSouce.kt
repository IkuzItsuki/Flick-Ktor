package com.ikuzMirel.data.user

interface UserDataSouce {
    suspend fun getUserByUsername(username: String): User?
    suspend fun insertUser(user: User): Boolean
}
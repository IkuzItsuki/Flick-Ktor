package com.ikuzMirel.data.user

interface UserDataSource {
    suspend fun getUserById(id: String): User?
    suspend fun insertUser(user: User): Boolean
}
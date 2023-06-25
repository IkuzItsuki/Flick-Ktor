package com.ikuzMirel.data.user

interface UserDataSource {
    suspend fun getUserById(id: String): User?
    suspend fun getUsersByName(username: String): List<User>
    suspend fun insertUser(user: User): Boolean
}
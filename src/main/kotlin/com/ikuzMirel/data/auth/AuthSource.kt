package com.ikuzMirel.data.auth

interface AuthSource {
    suspend fun getAuthByUsername(username: String): Auth?
    suspend fun getAuthByUserId(id: String): Auth?
    suspend fun insertAuth(auth: Auth): Boolean
}
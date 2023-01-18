package com.ikuzMirel.security.hashing

data class SaltedHash(
    val hash: String,
    val salt: String
)

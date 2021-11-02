package com.geek.mrguard.data

data class signInResponse(
    val status: String,
    val success: Boolean,
    val token: String,
    val user: User
)
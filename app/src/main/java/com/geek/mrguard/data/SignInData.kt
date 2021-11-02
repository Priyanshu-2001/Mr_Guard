package com.geek.mrguard.data

data class SignInData(
    val hash: String,
    val otp: String,
    val phone: String,
    val user_type: String
)
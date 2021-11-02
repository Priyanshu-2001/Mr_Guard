package com.geek.mrguard.retrofit

import com.geek.mrguard.data.OtpResponse
import com.geek.mrguard.data.SignInData
import com.geek.mrguard.data.signInResponse
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Body

interface LoginServiceApi {
    @POST("users/sendOtp")
    suspend fun getOtp(@Body params : params) : Response<OtpResponse>

    @POST("users/signin")
    suspend fun login(@Body data: SignInData): Response<signInResponse>
}

class params(phone : String) {
    var phone: String? = phone
}
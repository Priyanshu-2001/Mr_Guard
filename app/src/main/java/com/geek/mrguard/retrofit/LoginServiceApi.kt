package com.geek.mrguard.retrofit

import com.geek.mrguard.Globals
import com.geek.mrguard.data.OtpResponse
import com.geek.mrguard.data.SignInData
import com.geek.mrguard.data.signInResponse
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PUT

interface LoginServiceApi {

    @POST("users/sendOtp")
    suspend fun getOtp(@Body params : params) : Response<OtpResponse>

    @POST("users/signin")
    suspend fun login(@Body data: SignInData): Response<signInResponse>

    @PUT(Globals.uploadTokenToProfile)
    suspend fun uploadToken(@Header("Authorization") token : String , @Body obj : tokenData)
}

class params(phone : String) {
    var phone: String? = phone
}
data class tokenData(val firebaseToken:String)
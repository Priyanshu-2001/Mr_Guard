package com.geek.mrguard.services

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.geek.mrguard.data.SignInData
import com.geek.mrguard.data.signInResponse
import com.geek.mrguard.retrofit.LoginServiceApi
import com.geek.mrguard.retrofit.params

class LoginService(private val service: LoginServiceApi) {

    suspend fun sendOTP(phone : Any): MutableLiveData<String> {
        val hash = MutableLiveData<String>()
        Log.e("service/repo", "sendOTP: $phone" )
        val result = service.getOtp(params(phone.toString()))
        Log.e(Companion.TAG, "sendOTP: ", )
        if(result.body()!=null){
            hash.postValue(result.body()?.secretHash.toString())
            Log.e("TAG", "sendOTP hash: ${result.body()?.secretHash.toString()}" )
        }
        return hash
    }

    suspend fun login(hash: String?, phoneNumber : String? , otp : String) : MutableLiveData<signInResponse> {
        val obj = SignInData(hash!!,
            otp,
            phoneNumber!!,
            "normal_user")
        val result = service.login(obj)
//        Log.e("TAG", "login: ${result.body()}", )
        return MutableLiveData(result.body())
    }

    companion object {
        const val TAG = "SERVICE CALL"
    }
}
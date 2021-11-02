package com.geek.mrguard

import android.app.Application
import com.geek.mrguard.retrofitHelper.RetrofitHelper
import com.geek.mrguard.services.LoginService
import com.geek.mrguard.retrofit.LoginServiceApi

class guardApplication : Application() {
    lateinit var loginRepo : LoginService
    override fun onCreate() {
        super.onCreate()
        val service  = RetrofitHelper().getInstance().create(LoginServiceApi::class.java)
        loginRepo = LoginService(service)
    }

}
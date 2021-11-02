package com.geek.mrguard.retrofitHelper

import com.geek.mrguard.Globals
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class RetrofitHelper {
    val BASE_URL = Globals.Endpoint

    fun getInstance() : Retrofit {
        return Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
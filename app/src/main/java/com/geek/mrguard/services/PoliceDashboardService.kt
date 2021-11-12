package com.geek.mrguard.services

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.geek.mrguard.Globals
import com.geek.mrguard.data.policeAnalytics.PoliceAnalyticsData
import com.google.gson.Gson
import java.util.*


class PoliceDashboardService(private val req_id: String) {
    fun getPoliceHistory(context: Context): MutableLiveData<PoliceAnalyticsData> {
        val data = MutableLiveData<PoliceAnalyticsData>()
        val url: String = if (req_id == "null") {
            Globals.Endpoint + Globals.requestPoliceData + "?_id=" + req_id
        } else {
            Globals.Endpoint + Globals.requestPoliceData
        }
        val requestQueue = Volley.newRequestQueue(context)
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET,
            url,
            null,
            {
                val gson = Gson()
                val obj = gson.fromJson(it.toString(), PoliceAnalyticsData::class.java)
                data.value = obj
            },
            {
                Toast.makeText(context, "Failed TO Load Data \n Server Error", Toast.LENGTH_SHORT).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val map = HashMap<String, String>()
                val token = context.getSharedPreferences("tokenFile", Context.MODE_PRIVATE)
                    .getString("token", "0")
                Log.e("TAG", "getHeaders:tokem $token")
                map["Authorization"] = "Bearer $token"
                return map
            }
        }
        requestQueue.add(jsonObjectRequest)
        return data
    }

}
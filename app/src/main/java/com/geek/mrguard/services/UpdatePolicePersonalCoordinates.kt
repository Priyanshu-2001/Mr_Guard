package com.geek.mrguard.services

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.geek.mrguard.Globals
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import java.util.*

class UpdatePolicePersonnelCoordinates {
    fun updateCoordinate(latLng: LatLng, context: Context) {
        val locationObj = JSONObject()
        val obj = JSONObject()
        locationObj.put("lat", latLng.latitude)
        locationObj.put("lon", latLng.longitude)
        obj.put("location", locationObj)
        val requestQueue = Volley.newRequestQueue(context)
        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.PUT,
            Globals.Endpoint + Globals.updatePoliceCoordinates, obj,
            {
                val result = it as JSONObject
                if (result.get("success") as Boolean) {
                    Toast.makeText(context, "Coordinates Updated SuccessFully", Toast.LENGTH_LONG)
                        .show()
                } else {
                    Toast.makeText(context, "Failed Try Again", Toast.LENGTH_SHORT).show()
                }
                Log.e("TAG", "updateCoordinate: ${result.get("success")}")

            },
            {
                Log.e("TAG", "updateCoordinate: $it")
                Toast.makeText(context, "Try Again", Toast.LENGTH_SHORT).show()
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
    }
}
package com.geek.mrguard.UI

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.geek.mrguard.R
import com.geek.mrguard.UI.dashBoard.Police.PoliceDashBoard
import com.geek.mrguard.UI.dashBoard.commonUser.NormalUserDashBoard
import com.geek.mrguard.services.getUserLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {
    private val bundle = Bundle()
    lateinit var getuserLocation: getUserLocation
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    lateinit var locationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        initializeLocationVariables()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try{
            if (intent.extras != null) {
                val i = intent.extras
                Log.d("TAG", "roomID : ${i?.get("roomId")}")
                Log.d("TAG", "notification from : ${i?.get("from")}")
                Log.d("TAG", "victimProfile : ${i?.get("victimProfile")}")
                bundle.putString("roomID", i?.get("roomId") as String?)
                bundle.putString("victimContact", i?.get("victimProfile") as String?)
//                bundle.putString("roomID", i?.get("roomId") as String?)
            }
//            val data = intent.extras to
//            Log.e("TAG", "onCreate: notification data " + )
        }catch (e : Exception){
            Log.e(":notification failed", "onCreate: $e")
        }
        try{
            val pref = getSharedPreferences("tokenFile", MODE_PRIVATE)
            val con = this
            pref?.apply {
                if(contains("token")){
                    val i : Intent
                    if(!getBoolean("isPolice",false))
                        i = Intent(con, NormalUserDashBoard::class.java)
                    else {
                        i = Intent(con, PoliceDashBoard::class.java)
                        if (intent.extras != null) {
                            val i1 = intent.extras
                            i.putExtra("roomID", i1?.get("roomId") as String?)
                            i.putExtra("victimContact", i1?.get("victimProfile") as String?)
                        }
                    }
                    startActivity(i)
                    finishAffinity()
                }
            }
        }catch (e : Exception){
            Log.e("TAG", "onCreate: Error while Loading Last Login")
        }
    }
    private fun initializeLocationVariables() {
        locationRequest = LocationRequest.create()
        locationRequest.numUpdates = 1
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getuserLocation = getUserLocation(this)
        getuserLocation.checkSettingsAndStartLocationUpdates(
            locationRequest,
            fusedLocationProviderClient!!
        )
    }
}
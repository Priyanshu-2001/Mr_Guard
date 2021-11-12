package com.geek.mrguard.UI.dashBoard.commonUser

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import com.geek.mrguard.R
import com.geek.mrguard.SocketIOClient
import com.geek.mrguard.UI.MainActivity
import com.geek.mrguard.UI.dashBoard.Police.PoliceDashBoard.Companion.checkPermissionsAvailablity
import com.geek.mrguard.databinding.ActivityNormalUserDashBoardBinding
import com.geek.mrguard.services.getUserLocation
import com.geek.mrguard.utils.PermissionCheck
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.header.view.*
import org.json.JSONException
import org.json.JSONObject


class NormalUserDashBoard : AppCompatActivity() {
    lateinit var binding: ActivityNormalUserDashBoardBinding
    private var mSocket: Socket? = null
    lateinit var pref: SharedPreferences
    lateinit var progressDialog: ProgressDialog
    private lateinit var roomId: String
    lateinit var locationRequest: LocationRequest
    lateinit var getuserLocation: getUserLocation
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var currentLatLng: LatLng? = null

    private fun initializeSocket() {
        try {
//            mSocket = IO.socket("https://police-backend-deploy.herokuapp.com/")
            mSocket = SocketIOClient.getInstance(this)
        } catch (e: Exception) {
            Log.e(TAG, "initializeSocket: ${e.printStackTrace()}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_normal_user_dash_board)
        pref = getSharedPreferences("tokenFile", MODE_PRIVATE)
        val currentphoneNumber = pref.getString("phoneNumber","Currently Unavailable")
        binding.navLayout.getHeaderView(0).pName.text = currentphoneNumber
        initializeSocket()
        mSocket?.connect()
        initializeLocationVariables()
        getCoordinates()
        binding.raiseAlert.setOnStateChangeListener {
            if (it) {
                mSocket?.run {
                    // generates random roomID
                    roomId = FirebaseDatabase.getInstance().reference.push().key.toString()
                    val obj = JSONObject()
                    obj.put("roomId", roomId)
                    obj.put("lat", currentLatLng?.latitude.toString())
                    obj.put("long", currentLatLng?.longitude.toString())
                    obj.put("phone_number", currentphoneNumber)
                    Log.e(TAG, "onCreate: roomID send $obj")
                    emit("victimJoin", obj)
                }
                if (mSocket!!.connected()) {
                    mSocket?.on("policeManJoin", onPoliceJoined)
                    mSocket?.connect()
                }
                progressDialog = ProgressDialog(this, R.style.AlertDialog)
                    .apply {
                        setMessage("Searching For Police Man")
                        setButton("CANCEL") { dialog, _ ->
                            dialog.dismiss()
                            mSocket?.emit("cancelSearch", {})
                        }
                        setCancelable(true)
                        setCanceledOnTouchOutside(false)
                        show()
                    }
            } else {
                print("Won't know")
            }
        }
        binding.navLayout.setNavigationItemSelectedListener {
            when (it.title) {
                "Log Out" -> {
                    pref.edit().clear().apply()
                    finishAffinity()
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                "History"->{
                    openDashboard()
                    binding.drawerLayout.close()
                    true
                }
                else -> {
                    print("Nothing to show")
                    false
                }
            }
        }
        binding.HamburgerIcon.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun getCoordinates() {
        if(checkPermissionsAvailablity(this)){
            getCurrentLocationCoordinates()
        }else{
            PermissionCheck().requestPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            getCurrentLocationCoordinates()
        }
    }
    fun getCurrentLocationCoordinates(){
        getuserLocation.checkSettingsAndStartLocationUpdates(
            locationRequest,
            fusedLocationProviderClient!!
        )

        getuserLocation.userLoc.observe(this, {loc->
            currentLatLng = LatLng(loc.latitude, loc.longitude)
        })
    }

    private fun openDashboard(){
        startActivity(Intent(this,AnalyticsDashboard::class.java))
    }
    companion object {
        const val TAG = "Normal User DashBoard"
    }
    private fun initializeLocationVariables() {
        getuserLocation = getUserLocation(this)
        locationRequest = LocationRequest.create()
        locationRequest.numUpdates = 1
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }
    private val onPoliceJoined =
        Emitter.Listener { args ->
            this.runOnUiThread(Runnable {
                val data = args[0] as JSONObject
                Log.e(TAG, "police joined = $data: ")
                progressDialog.dismiss()
                val intent = Intent(this, VictimPoliceInteraction::class.java)
                intent.putExtra("roomID", roomId)
                startActivity(intent)
                try {
                    val username = data.getString("username")
                    val message = data.getString("message")
                    Log.e(TAG, "$username: $message")
                } catch (e: JSONException) {
                    return@Runnable
                }
            })
        }
}
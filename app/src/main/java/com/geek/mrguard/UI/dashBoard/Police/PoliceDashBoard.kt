package com.geek.mrguard.UI.dashBoard.Police

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Point
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.geek.mrguard.R
import com.geek.mrguard.UI.MainActivity
import com.geek.mrguard.UI.dashBoard.commonUser.NormalUserDashBoard
import com.geek.mrguard.adapters.ChatAdapter
import com.geek.mrguard.databinding.ActivityDashBoardBinding
import com.geek.mrguard.services.UpdatePolicePersonnelCoordinates
import com.geek.mrguard.services.getUserLocation
import com.geek.mrguard.utils.PermissionCheck
import com.geek.mrguard.viewModel.chatViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_dash_board.*
import kotlinx.android.synthetic.main.activity_normal_user_dash_board.*
import kotlinx.android.synthetic.main.chat_fragment.*
import kotlinx.android.synthetic.main.chat_fragment.view.*
import kotlinx.android.synthetic.main.header.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class PoliceDashBoard : AppCompatActivity(), OnMapReadyCallback {

    private var mSocket: Socket? = null
    private lateinit var gMap: GoogleMap
    lateinit var binding: ActivityDashBoardBinding
    var currentMarker: Marker? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var currentLocation: Location? = null
    private lateinit var pref: SharedPreferences
    lateinit var viewModel: chatViewModel
    private var roomID = "3002"
    private var victimPhoneNumber = "1234567890"
    lateinit var adapter: ChatAdapter
    lateinit var alertDialog: AlertDialog.Builder
    lateinit var locationRequest: LocationRequest
    lateinit var getuserLocation: getUserLocation
    lateinit var previousLatLng : LatLng

    override fun onStart() {
        super.onStart()
        val i = intent.extras
        if(!checkPermissionsAvailablity(this)){
            PermissionCheck().requestPermissions(this)
        }
        if (i != null) {
            roomID = i.getString("roomID", "-4")
            victimPhoneNumber = i.getString("victimContact", "-5")
            Log.e("TAG", "onStart: $roomID context $victimPhoneNumber")
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dash_board)
        viewModel = ViewModelProvider(this).get(chatViewModel::class.java)
        viewModel.message.observeForever {
            adapter = ChatAdapter(this, it)
            Log.e("TAG", "onCreate: pulici msg viewmodel$it")
            binding.bottomsheet.recyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            window.statusBarColor = Color.TRANSPARENT
        }
        binding.bottomsheet.chatBottomSheetLayout.visibility = View.GONE
        binding.bottomsheet.chatBottomSheetLayout.animate().translationY((180).toFloat())
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.frag_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        initializeAlertDialog()
        initializeLocationVariables()
        initializeSocket()
        mSocket?.connect()
        pref = getSharedPreferences("tokenFile", MODE_PRIVATE)
        val currentPhoneNumber = pref.getString("phoneNumber", "Currently Unavailable")
        binding.navLayout.getHeaderView(0).pName.text = currentPhoneNumber
        val drawer = binding.drawerIcon
        drawer.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        getuserLocation = getUserLocation(this)
        getuserLocation.checkSettingsAndStartLocationUpdates(
            locationRequest,
            fusedLocationProviderClient!!
        )
        binding.navLayout.setNavigationItemSelectedListener {
            when (it.title) {
                "Log Out" -> {
                    pref.edit().clear().apply()
                    finishAffinity()
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                "Current Location" -> {
                    updateCoordinate()
                    true
                }
                else -> {
                    print("Nothing to show")
                    false
                }
            }
        }
        binding.bottomsheet.sendBtn.setOnClickListener {
            val s = binding.bottomsheet.messageBox.text
            if (s?.trim()?.length != 0) {
                attemptSend(s)
            }
        }
        mSocket?.on("joinMessage", onPoliceJoinRoom)

        fetchLoc()
    }

    private fun initializeLocationVariables() {
        locationRequest = LocationRequest.create()
        locationRequest.numUpdates = 1
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun updateCoordinate() {
        if (checkPermissionsAvailablity(this)) {
            binding.drawerLayout.close()
            Toast.makeText(
                this,
                "Hold on !! \n we fetching your current coordinates",
                Toast.LENGTH_SHORT
            ).show()
            if (currentLocation == null) {
                getuserLocation.userLoc.observe(this, { loc ->
                    val latlong = LatLng(loc.latitude, loc.longitude)
                    UpdatePolicePersonnelCoordinates().updateCoordinate(
                        latlong,
                        this
                    )
                })
            } else {
                val latlng =
                    LatLng(currentLocation?.latitude!!, currentLocation?.longitude!!)
                UpdatePolicePersonnelCoordinates().updateCoordinate(latlng, this)
            }

        } else {
            PermissionCheck().requestPermissions(this)
        }
    }

    private fun initializeAlertDialog() {
        alertDialog = AlertDialog.Builder(this).apply {
            setTitle("Alert Raised")
            setMessage("Some Needs Your Help at location")
            setPositiveButton("Accept") { dialog, which ->
                acceptRequest(roomID)
                dialog.dismiss()
                setCancelable(false)
                setIcon(R.drawable.cap)
                intent.removeExtra("roomID")
                intent.removeExtra("victimContact")
            }
        }
    }

    private val onNewMessage =
        Emitter.Listener { args ->
            runOnUiThread(Runnable {
                val data = args[0] as JSONObject
                val message: String
                try {
                    message = data.getString("msg")
                    if (message.equals("Victim has left the room")) {
                        viewModel.victimLeftTheChat()
                        Toast.makeText(this, "VICTIM LEFT THE CHAT", Toast.LENGTH_SHORT).show()
                        Log.e("TAG", "left the room msg : $message")
                    } else {
                        Toast.makeText(this, "New Msg Received", Toast.LENGTH_SHORT).show()
                        viewModel.addMessageToReceiverList(message)
                    }
                } catch (e: JSONException) {
                    return@Runnable
                }

            })
        }

    private fun attemptSend(msg: CharSequence?) {
        val obj = JSONObject()
        obj.put("roomId", roomID)
        obj.put("victimProfile", "phoneNumber")
        obj.put("message", msg)
        mSocket?.emit("chat_message", obj)
        viewModel.addMessageToSenderList(msg.toString())
        binding.bottomsheet.messageBox.text.clear()
    }


    @SuppressLint("MissingPermission")
    private fun fetchLoc() {
        if (!checkPermissionsAvailablity(this)) {
            PermissionCheck().requestPermissions(this)
            return
        }
        getuserLocation.userLoc.observe(this, { location ->
            if (location != null) {
                this.currentLocation = location
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            1000 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
//                fetchLoc()
//        }
        if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
            getuserLocation.checkSettingsAndStartLocationUpdates(
                locationRequest,
                fusedLocationProviderClient!!
            )

            getuserLocation.userLoc.observe(this, {
                val latlong = LatLng(currentLocation?.latitude!!, currentLocation?.longitude!!)
                Log.e("TAG", "onMapReady: $latlong")
//                drawMarker(latlong)
            })
        }
    }
//    4WiFTo
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap
        if (checkPermissionsAvailablity(this)) {
            if (currentLocation == null)
                getuserLocation.checkSettingsAndStartLocationUpdates(
                    locationRequest,
                    fusedLocationProviderClient!!
                )

            getuserLocation.userLoc.observe(this, {
                val latlong = LatLng(currentLocation?.latitude!!, currentLocation?.longitude!!)
                Log.e("TAG", "onMapReady: $latlong")
//                drawMarker(latlong)
            })

            googleMap.isMyLocationEnabled = true
        } else
            PermissionCheck().requestPermissions(this)
    }

    private fun drawMarker(latLong: LatLng) {
        val markerOption = MarkerOptions().position(latLong).title("Your Location")
            .snippet(getAddress(latLong.latitude, latLong.longitude)).draggable(true)
        gMap.animateCamera(CameraUpdateFactory.newLatLng(latLong))
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLong, 15f))
        currentMarker = gMap.addMarker(markerOption)
        currentMarker?.showInfoWindow()
    }

    private fun getAddress(lat: Double, lon: Double): String {
        val geoCoder = Geocoder(this, Locale.getDefault())
        val addresses = geoCoder.getFromLocation(lat, lon, 1)
        return addresses[0].getAddressLine(0).toString()
    }

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val msgBody = intent.getStringExtra("body")
            roomID = intent.getStringExtra("roomID").toString()
            victimPhoneNumber = intent.getStringExtra("victimPhoneNumber").toString()
            alertDialog.show()
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mMessageReceiver,
            IntentFilter("myFunction")
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
    }

    private fun acceptRequest(roomID: String) {
        mSocket?.run {
            val obj = JSONObject()
            val victimProfile = JSONObject()
            val policeProfile = JSONObject()
            victimProfile.put("phone", victimPhoneNumber)
            policeProfile.put("phone", "9877371590")
            obj.put("roomId", roomID)
            obj.put("victimProfile", victimProfile)
            obj.put("policeProfile", policeProfile)
            Log.e("TAG", "chat_message: " + on("chat_message", onNewMessage))
            on("joinMessage", onPoliceJoinRoom)
            on("victimNewCoordinates", onReceivingCoordinates)
            emit("policeManJoin", obj)
            Log.e("TAG", "isConnected" + mSocket?.connected() + mSocket?.isActive)
        }
    }

    private fun initializeSocket() {
        try {
            mSocket = IO.socket("https://police-backend-deploy.herokuapp.com/")
        } catch (e: Exception) {
            Log.e(NormalUserDashBoard.TAG, "initializeSocket: ${e.printStackTrace()}")
        }
    }

    private val participantListener =
        Emitter.Listener { args ->
            this.runOnUiThread(Runnable {
                val data = args[0] as JSONObject
                Log.e(NormalUserDashBoard.TAG, "message participants = $data: ")
            })
        }

    private val onPoliceJoinRoom =
        Emitter.Listener { args ->
            this.runOnUiThread {
                val data = args[0] as JSONObject
                Log.e("POLICE DASHBOARD", "Chat JOINED = $data: ")
                binding.bottomsheet.chatBottomSheetLayout.visibility = View.VISIBLE
                binding.bottomsheet.chatBottomSheetLayout.animate().translationY((0).toFloat())
                binding.bottomsheet.header.text = "Chat with Victim\n($victimPhoneNumber)"
            }
        }
    private val onReceivingCoordinates =
        Emitter.Listener { args ->
            this.runOnUiThread {
                val data = args[0] as JSONObject
                Log.e("TAG", "coordinates =>> ${data.get("updatedCord")}: ")
                val locObj : JSONObject = data.get("updatedCord") as JSONObject
                val latlng = LatLng(locObj.get("lat") as Double, locObj.get("lon") as Double)
//                if(previousLatLng==null){
//                    previousLatLng = latlng
//                }
                currentMarker?.let { animateMarker(it,latlng,true) }
                drawMarker(latlng)
            }
        }

    companion object {
        fun checkPermissionsAvailablity(context: Context): Boolean {
            return (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
                    == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
                    == PackageManager.PERMISSION_GRANTED)
        }
    }

    fun animateMarker(
        marker: Marker, toPosition: LatLng,
        hideMarker: Boolean
    ) {
        val handler = Handler()
        val start: Long = SystemClock.uptimeMillis()
        val proj: Projection = gMap.projection
        val startPoint: Point = proj.toScreenLocation(marker.position)
        val startLatLng = proj.fromScreenLocation(startPoint)
        val duration: Long = 500
        val interpolator: Interpolator = LinearInterpolator()
        handler.post(object : Runnable {
            override fun run() {
                val elapsed: Long = SystemClock.uptimeMillis() - start
                val t: Float = interpolator.getInterpolation(
                    elapsed.toFloat()
                            / duration
                )
                val lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude
                val lat = t * toPosition.latitude + (1 - t)* startLatLng.latitude
                marker.position = LatLng(lat, lng)
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16)
                } else {
                    marker.isVisible = !hideMarker
                }
            }
        })
    }
}
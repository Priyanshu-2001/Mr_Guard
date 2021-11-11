package com.geek.mrguard.UI.dashBoard.Police

import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.geek.mrguard.utils.PermissionCheck
import com.geek.mrguard.viewModel.chatViewModel
import com.google.android.gms.location.FusedLocationProviderClient
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

    override fun onStart() {
        super.onStart()
        val i = intent.extras
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
        initializeAlertDialog()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        initializeSocket()
        mSocket?.connect()
        pref = getSharedPreferences("tokenFile", MODE_PRIVATE)
        val currentphoneNumber = pref.getString("phoneNumber","Currently Unavailable")
        binding.navLayout.getHeaderView(0).pName.text = currentphoneNumber
        val drawer = binding.drawerIcon
        drawer.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navLayout.setNavigationItemSelectedListener {
            when (it.title) {
                "Log Out" -> {
                    pref.edit().clear().apply()
                    finishAffinity()
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                "Current Location" -> {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        binding.drawerLayout.close()
                        Toast.makeText(
                            this,
                            "Hold on !! \n we fetching your current coordinates",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (currentLocation == null) {
                            val task = fusedLocationProviderClient?.lastLocation
                            task?.addOnCompleteListener { loc ->
                                if (loc.isSuccessful) {
                                    val latlong = LatLng(loc.result.latitude, loc.result.longitude)
                                    UpdatePolicePersonnelCoordinates().updateCoordinate(
                                        latlong,
                                        this
                                    )
                                }
                            }
                        } else {
                            val latlong =
                                LatLng(currentLocation?.latitude!!, currentLocation?.longitude!!)
                            UpdatePolicePersonnelCoordinates().updateCoordinate(latlong, this)
                        }

                    } else {
                        PermissionCheck().requestPermissions(this)
                    }
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
        mSocket?.on("joinMessage", messageListener)

        fetchLoc()
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


    private fun fetchLoc() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1000
            )
            return
        }
        val task = fusedLocationProviderClient?.lastLocation
        task?.addOnSuccessListener { location ->
            if (location != null) {
                this.currentLocation = location
//                val mapFragment = supportFragmentManager.findFragmentById(
//                    R.id.frag_map
//                ) as SupportMapFragment
//                mapFragment.getMapAsync(this)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1000 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                fetchLoc()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap

        val latlong = LatLng(currentLocation?.latitude!!, currentLocation?.longitude!!)
        drawMarker(latlong)

        gMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDrag(p0: Marker) {

            }

            override fun onMarkerDragEnd(p0: Marker) {
                if (currentMarker != null)
                    currentMarker?.remove()

                val newLatLng = LatLng(p0.position.latitude, p0.position.longitude)
                drawMarker(newLatLng)
            }

            override fun onMarkerDragStart(p0: Marker) {

            }
        })

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
            on("joinMessage", messageListener)
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

    private val messageListener =
        Emitter.Listener { args ->
            this.runOnUiThread {
                val data = args[0] as JSONObject
                Log.e("POLICE DASHBOARD", "Chat JOINED = $data: ")
                binding.bottomsheet.chatBottomSheetLayout.visibility = View.VISIBLE
                binding.bottomsheet.chatBottomSheetLayout.animate().translationY((0).toFloat())
                binding.bottomsheet.header.text = "Chat with Victim\n($victimPhoneNumber)"
            }
        }
}
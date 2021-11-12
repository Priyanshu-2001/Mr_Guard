package com.geek.mrguard.UI.dashBoard.commonUser

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
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
import com.geek.mrguard.databinding.ActivityNormalUserDashBoardBinding
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
        binding.raiseAlert.setOnStateChangeListener {
            if (it) {
//                initializeSocket()

                mSocket?.run {
                    // generates random roomID
                    roomId = FirebaseDatabase.getInstance().reference.push().key.toString()
                    Log.e(TAG, "onCreate: roomID send $roomId")
                    val obj = JSONObject()
                    obj.put("roomId", roomId)
                    obj.put("lat", "30.722") //TODO need real data
                    obj.put("long", "76.85")
                    obj.put("phone_number", "8872365433")
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
    private fun openDashboard(){
        startActivity(Intent(this,AnalyticsDashboard::class.java))
    }
    companion object {
        const val TAG = "Normal User DashBoard"
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
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
import com.geek.mrguard.Globals
import com.geek.mrguard.R
import com.geek.mrguard.SocketIOClient
import com.geek.mrguard.UI.MainActivity
import com.geek.mrguard.data.raiseAlert
import com.geek.mrguard.databinding.ActivityNormalUserDashBoardBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.JsonObject
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException
import org.json.JSONException

import org.json.JSONObject

import io.socket.emitter.Emitter
import java.lang.Exception


class NormalUserDashBoard : AppCompatActivity() {
    lateinit var binding: ActivityNormalUserDashBoardBinding
    private var mSocket: Socket? = null
    lateinit var pref: SharedPreferences
    lateinit var progressDialog : ProgressDialog
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
        initializeSocket()
        mSocket?.connect()
        binding.raiseAlert.setOnStateChangeListener {
            if (it) {
//                initializeSocket()

                mSocket?.run {
//                    val roomId = FirebaseDatabase.getInstance().reference.push().key // generates random roomID
                    val roomId  = 3002
                    val obj  = JSONObject()
                    obj.put("roomId",roomId)
                    obj.put("lat","30.722")
                    obj.put("long","76.85")
                    obj.put("phone_number","8872365433")
                    emit("victimJoin",obj)
                }
                if(mSocket!!.connected()){
                    mSocket?.on("policeManJoin", onPoliceJoined);
                    mSocket?.connect();
                }
                progressDialog = ProgressDialog(this, R.style.AlertDialog)
                    .apply {
                        setMessage("Searching For Police Man")
                        setButton("CANCEL") { dialog, _ ->
                            dialog.dismiss()
                            mSocket?.emit("cancelSearch",{})
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

    companion object {
        const val TAG = "Normal User DashBoard"
    }

    private val onPoliceJoined =
        Emitter.Listener { args ->
            this.runOnUiThread(Runnable {
                val data = args[0] as JSONObject
                Log.e(TAG, "police joined = $data: ", )
                progressDialog.dismiss()
                startActivity(Intent(this,VictimPoliceInteraction::class.java))
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
package com.geek.mrguard.UI.dashBoard.commonUser

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.geek.mrguard.R
import com.geek.mrguard.SocketIOClient
import com.geek.mrguard.adapters.ChatAdapter
import com.geek.mrguard.databinding.ActivityVictimPoliceInteractionBinding
import com.geek.mrguard.viewModel.chatViewModel
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_victim_police_interaction.*
import org.json.JSONException
import org.json.JSONObject

class VictimPoliceInteraction : AppCompatActivity() {

    lateinit var binding: ActivityVictimPoliceInteractionBinding
    lateinit var viewModel: chatViewModel
    private var mSocket: Socket? = null
    private val roomID = 3002
    private val phoneNumber = 9877371590
    lateinit var adapter: ChatAdapter


    init{
        try {
            mSocket = SocketIOClient.getInstance(this)
            mSocket?.connect()
            Log.e(
                "TAG", "initializeSocket: " + mSocket?.connect()
            )
        } catch (e: Exception) {
            Log.e("victimTAG", "initializeSocket: ${e.printStackTrace()}")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(chatViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_victim_police_interaction)
        binding.chatLayout.sendBtn.setOnClickListener {
            Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show()
            val s = binding.chatLayout.messageBox.text
            if (s?.trim()?.length != 0) {
                attemptSend(s)
            }
        }
        Handler().postDelayed({
            mSocket?.apply {
                connect()
                if(mSocket!!.connected()){
                    mSocket!!.on("chat_message", onNewMessage)
                    mSocket?.connect();
                }
//                Log.e("Victim socket ", "onCreate: " + once("chat_message", onNewMessage))
                Log.e("TAG", "initializeSocket: active " + mSocket?.isActive )
                Log.e("TAG", "initializeSocket: connected " + mSocket?.connected() )
                Log.e("victim socker", "onCreate: " + on("joinMessage", joinMessage))
            }
        },5000)

        viewModel.message.observeForever {
            adapter = ChatAdapter(this, it)
            Log.e("TAG", "onCreate: victim msg viewmodel$it")
            binding.chatLayout.recyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
        }
    }

    private val joinMessage =
        Emitter.Listener { args ->
            this.runOnUiThread {
                val data = args[0] as JSONObject
                Log.e("POLICE DASHBOARD2", "vitctim joined = $data: ")
            }
        }


    private val onNewMessage =
        Emitter.Listener { args ->
            runOnUiThread(Runnable {
                Toast.makeText(this, "received in vicitm", Toast.LENGTH_SHORT).show()
                val data = args[0] as JSONObject
                Log.e("victim got msg ", ": ${data.getString("msg")} ")
                Log.e("TAG", "msg received in victim  $data")
                val message: String
                try {
                    message = data.getString("msg")
                    viewModel.addMessageToReceiverList(message)
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
        binding.chatLayout.messageBox.text.clear()
    }
}
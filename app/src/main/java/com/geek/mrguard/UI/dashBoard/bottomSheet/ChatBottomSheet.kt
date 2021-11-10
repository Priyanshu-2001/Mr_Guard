package com.geek.mrguard.UI.dashBoard.bottomSheet

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.geek.mrguard.R
import com.geek.mrguard.databinding.ChatFragmentBinding
import com.geek.mrguard.viewModel.chatViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.geek.mrguard.adapters.ChatAdapter
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONException
import org.json.JSONObject


class ChatBottomSheet : BottomSheetDialogFragment() {
    lateinit var binding: ChatFragmentBinding
    lateinit var viewModel: chatViewModel
    lateinit var mSocket: Socket
    private val roomID = 3001
    private val phoneNumber = 9877371590
    lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.chat_fragment, container, false)
        viewModel = ViewModelProvider(this).get(chatViewModel::class.java)
        Log.e(TAG, "onCreateView: BottomSheetCrested", )
        binding.sendBtn.setOnClickListener {
            Toast.makeText(requireContext(), "clicked", Toast.LENGTH_SHORT).show()
            binding.messageBox.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    if (s?.trim()?.length != 0) {
                        attemptSend(s)
                    }
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.trim()?.length != 0) {
                        attemptSend(s)
                    }

                }

                override fun afterTextChanged(s: Editable?) {
                    if (s?.trim()?.length != 0) {
                        attemptSend(s)
                    }
                }

            })
        }
        viewModel.message.observeForever {
            adapter = ChatAdapter(requireContext(), it)
            binding.recyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
        }
        listenToIncomingMsg()
        return binding.root
    }

    private fun listenToIncomingMsg() {
        mSocket.on("chat_message", onNewMessage);
        mSocket.connect();
    }


    private val onNewMessage =
        Emitter.Listener { args ->
            requireActivity().runOnUiThread(Runnable {
                val data = args[0] as JSONObject
                val message: String
                try {
                    message = data.getString("message")
                } catch (e: JSONException) {
                    return@Runnable
                }
                viewModel.addMessageToReceiverList(message)
            })
        }


    private fun attemptSend(msg: CharSequence?) {
        val obj  = JSONObject()
        obj.put("roomId",roomID)
        obj.put("lat","76.85")
        obj.put("long","30.72")
        obj.put("phone_number",phoneNumber)
        mSocket.emit("chat_message", obj)
        binding.messageBox.setText("")
        viewModel.addMessageToSenderList(msg.toString())
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}
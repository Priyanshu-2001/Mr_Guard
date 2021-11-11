package com.geek.mrguard.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.geek.mrguard.R
import com.geek.mrguard.data.Message
import com.geek.mrguard.databinding.ItemChatLeftBinding
import com.geek.mrguard.databinding.ItemReceiveBinding
import com.geek.mrguard.databinding.ItemSentBinding
import java.util.*

class ChatAdapter(
    private var context: Context,
    private val messages: ArrayList<Message>,
) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view: View
        Log.e("TAG", "onCreateViewHolder: $viewType", )
        if (viewType == ITEM_EXIT) {
            view =
                LayoutInflater.from(context).inflate(R.layout.item_chat_left, parent, false)
        } else {
            if (viewType == ITEM_SENT) {
                view =
                    LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false)
            } else {
                view =
                    LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false)

            }
        }
        return ChatViewHolder(view)
    }


    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message: Message = messages[position]
        if (message.exitNotification) {
            Log.e("TAG", "onBindViewHolder:exit true")
            holder.bindLeftTheChat()
            holder.leftTheChatBinding.message.text = message.message
        } else {
            if (message.isReceived) {
                holder.bindReceiveViewHolder()
                holder.receiveBinding.message.text = message.message
            } else {
                holder.bindSendViewHolder()
                holder.sendBinding.message.text = message.message
            }
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val message: Message = messages[position]
        return if (message.exitNotification) {
            ITEM_EXIT
        } else {
            if (!message.isReceived) {
                ITEM_SENT
                Log.e("TAG", "getItemViewType: ITEM_EXIT")
            } else {
                ITEM_RECEIVE
            }
        }

    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var sendBinding: ItemSentBinding
        lateinit var receiveBinding: ItemReceiveBinding
        lateinit var leftTheChatBinding: ItemChatLeftBinding
        fun bindSendViewHolder() {
            sendBinding = ItemSentBinding.bind(itemView)
        }

        fun bindReceiveViewHolder() {
            receiveBinding = ItemReceiveBinding.bind(itemView)
        }

        fun bindLeftTheChat() {
            leftTheChatBinding = ItemChatLeftBinding.bind(itemView)
        }
    }

    companion object {
        const val ITEM_RECEIVE = 2
        const val ITEM_SENT = 1
        const val ITEM_EXIT = 3
    }
}
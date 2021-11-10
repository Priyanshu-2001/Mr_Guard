package com.geek.mrguard.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.geek.mrguard.R
import com.geek.mrguard.data.Message
import com.geek.mrguard.databinding.ItemReceiveBinding
import com.geek.mrguard.databinding.ItemSentBinding
import java.util.*

class ChatAdapter(
    private var context: Context,
    private val messages: ArrayList<Message>,
) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return if (viewType == ITEM_SENT) {
            val view: View = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false)
            ChatViewHolder(view)
        } else {
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false)
            ChatViewHolder(view)
        }
    }


    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message: Message = messages[position]
        if(message.isReceived){
            holder.bindReceiveViewHolder()
            holder.receiveBinding.message.text = message.message
        }else{
            holder.bindSendViewHolder()
            holder.sendBinding.message.text = message.message
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val message: Message = messages[position]
        return if (!message.isReceived) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var sendBinding: ItemSentBinding
        lateinit var receiveBinding: ItemReceiveBinding
        fun bindSendViewHolder() {
            sendBinding = ItemSentBinding.bind(itemView)
        }

        fun bindReceiveViewHolder() {
            receiveBinding = ItemReceiveBinding.bind(itemView)
        }
    }

    companion object {
        const val ITEM_RECEIVE = 2
        const val ITEM_SENT = 1
    }
}
package com.geek.mrguard.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.geek.mrguard.data.Message

class chatViewModel : ViewModel() {
    val message = MutableLiveData<ArrayList<Message>>()
    val list = ArrayList<Message>()
    fun addMessageToSenderList(msg : String){
        list.add(Message(msg,false))
        message.value = list
    }
    fun addMessageToReceiverList(msg : String){
        list.add(Message(msg,true))
        message.value = list
    }
}
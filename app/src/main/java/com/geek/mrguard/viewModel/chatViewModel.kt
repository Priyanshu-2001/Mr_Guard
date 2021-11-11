package com.geek.mrguard.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.geek.mrguard.data.Message

class chatViewModel : ViewModel() {
    val message = MutableLiveData<ArrayList<Message>>()
    private val list = ArrayList<Message>()
    fun addMessageToSenderList(msg : String){
        list.add(Message(msg,isReceived = false,exitNotification = false))
        message.value = list
    }
    fun addMessageToReceiverList(msg : String){
        list.add(Message(msg,isReceived = true,exitNotification = false))
        message.value = list
    }
    fun policeLeftTheChat(){
        val msg = "Police Left"
        list.add(Message(msg,isReceived = false,exitNotification = true))
        message.value = list
    }
    fun victimLeftTheChat(){
        val msg = "Victim Left"
        list.add(Message(msg,isReceived = false,exitNotification = true))
        message.value = list
    }
}
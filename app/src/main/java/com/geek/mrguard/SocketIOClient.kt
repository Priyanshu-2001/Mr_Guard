package com.geek.mrguard

import android.app.Activity

import io.socket.client.IO
import io.socket.client.Socket
import java.lang.RuntimeException
import java.net.URISyntaxException


object SocketIOClient {
    private var mSocket: Socket? = null
    private fun initSocket(activity: Activity) {
        try {
            mSocket = IO.socket(Globals.Endpoint)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            throw RuntimeException(e)
        }
    }

    fun getInstance(activity: Activity): Socket? {
        return if (mSocket != null) {
            mSocket
        } else {
            initSocket(activity)
            mSocket
        }
    }
}
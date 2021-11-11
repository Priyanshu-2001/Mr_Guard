package com.geek.mrguard.viewModel

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.geek.mrguard.UI.dashBoard.Police.PoliceDashBoard
import com.geek.mrguard.UI.dashBoard.commonUser.NormalUserDashBoard
import com.geek.mrguard.data.signInResponse
import com.geek.mrguard.services.LoginService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class LoginViewModel(val service: LoginService, var context123: Context?) : ViewModel() {
    val phoneNumber = MutableLiveData<String>()
    private var hash = MutableLiveData<String>()
    private val otp = MutableLiveData<String>()
    val res = MutableLiveData<signInResponse>()
    fun getOTP() {
        GlobalScope.launch(Dispatchers.IO) {
            val temp = service.sendOTP(phoneNumber.value as String)
            hash = temp
            Log.e("TAG", "getOTP: ${hash.value}")
        }
    }

    fun login():
            MutableLiveData<signInResponse> {
        GlobalScope.launch(Dispatchers.Main) {
            Log.e("TAG", "Login: ${hash.value}")
            Log.e("TAG", "Login: ${phoneNumber.value}")
            Log.e("TAG", "Login: ${otp.value}")
            res.postValue(
                service.login(
                    hash.value,
                    phoneNumber.value,
                    otp.value!!
                ).value
            )
            Log.e("TAG", "Login: result $res")
            verifyCred()
        }
        return res
    }

    fun setOTP(otp: String) {
        this.otp.value = otp
    }

    fun verifyCred() {

        res.observeForever {
            it.let {
                Log.e("TAG", "signInREPONSE : $it")
                if (it != null) {
                    if (it.success) {
                        nextScreenIntent(it)
                    } else {
                        Toast.makeText(
                            context123,
                            "There is some error\nplease resend the code",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        context123, "There is some error\n" +
                                "please resend the code", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun nextScreenIntent(signInResponse: signInResponse) {
        lateinit var intent: Intent
        context123?.apply {
            val edit = this.getSharedPreferences("tokenFile", MODE_PRIVATE)?.edit()
            edit?.putString("token", signInResponse.token)
            edit?.putString("phoneNumber",signInResponse.user.phone)
            if (signInResponse.user.user_type == "normal_user") {
                edit?.putBoolean("isPolice", false)
                intent = Intent(context123, NormalUserDashBoard::class.java)
            } else {
                intent = Intent(context123, PoliceDashBoard::class.java)
                edit?.putBoolean("isPolice", true)
            }
            edit?.apply()
        }
        getAndUploadFCMtoken(signInResponse.token)

        context123?.startActivity(intent)

    }

    private fun getAndUploadFCMtoken(userToken: String) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }
                // Get new FCM registration token
                val DeviceToken = task.result
                CoroutineScope(Dispatchers.IO).launch {
                    service.uploadToken(DeviceToken, userToken)
                }
            })
    }

    override fun onCleared() {
        super.onCleared()
        context123 = null
    }
}

class LoginViewModelFactory(private val service: LoginService, private val context: Context) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LoginViewModel(service, context) as T
    }
}
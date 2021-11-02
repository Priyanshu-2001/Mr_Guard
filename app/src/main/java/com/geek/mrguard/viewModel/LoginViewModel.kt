package com.geek.mrguard.viewModel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.geek.mrguard.UI.dashBorad.DashBoard
import com.geek.mrguard.data.signInResponse
import com.geek.mrguard.services.LoginService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginViewModel(val service: LoginService, var context123 : Context?) : ViewModel() {
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
    fun login() :
            MutableLiveData<signInResponse>{
        GlobalScope.launch(Dispatchers.Main) {
            Log.e("TAG", "Login: ${hash.value}")
            Log.e("TAG", "Login: ${phoneNumber.value}")
            Log.e("TAG", "Login: ${otp.value}")
//            val data = service.login(hash.value,
//                phoneNumber.value,
//                otp.value!!).value
//            Log.e("TAG", "Login: data $data")
            res.postValue(service.login(hash.value,
                phoneNumber.value,
                otp.value!!).value)
            Log.e("TAG", "Login: result $res", )
            verifyCred()
        }
        return res
    }
    fun setOTP(otp :String){
        this.otp.value = otp
    }
    fun verifyCred(){

        res.observeForever {
            it.let{
                Log.e("TAG", "signInREPONSE : $it")
                if(it!=null){
                    if (it.success) {
                        nextScreenIntent()
                    } else {
                        Toast.makeText(context123, "There is some error\nplease resend the code", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(context123, "There is some error\n" +
                            "please resend the code", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

     private fun nextScreenIntent() {
        context123?.startActivity(Intent(context123, DashBoard::class.java))
        (context123 as Activity).finishAffinity()
    }

    override fun onCleared() {
        super.onCleared()
        context123 = null
    }
}

class LoginViewModelFactory(private val service: LoginService, private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LoginViewModel(service,context) as T
    }
}
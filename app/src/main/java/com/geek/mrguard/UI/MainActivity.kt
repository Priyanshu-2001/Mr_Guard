package com.geek.mrguard.UI

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.geek.mrguard.R
import com.geek.mrguard.UI.dashBoard.Police.PoliceDashBoard
import com.geek.mrguard.UI.dashBoard.commonUser.NormalUserDashBoard
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try{
            val pref = getSharedPreferences("tokenFile", MODE_PRIVATE)
            val con = this
            pref?.apply {
                if(contains("token")){
                    if(!getBoolean("isPolice",false))
                        startActivity(Intent(con, NormalUserDashBoard::class.java))
                    else
                        startActivity(Intent(con, PoliceDashBoard::class.java))
                    finishAffinity()
                }
            }
        }catch (e : Exception){
            Log.e("TAG", "onCreate: Error while Loading Last Login")

        }

    }
}
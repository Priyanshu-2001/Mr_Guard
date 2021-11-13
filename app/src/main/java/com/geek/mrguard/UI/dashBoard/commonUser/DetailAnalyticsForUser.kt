package com.geek.mrguard.UI.dashBoard.commonUser

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.geek.mrguard.R
import com.geek.mrguard.databinding.PoliceRequestDetailsBinding
import com.geek.mrguard.services.PoliceDashboardService
import com.geek.mrguard.viewModel.PoliceAnalyticsViewModel
import com.geek.mrguard.viewModel.PoliceAnalyticsViewModelFactory
import kotlinx.android.synthetic.main.police_request_details.*

class DetailAnalyticsForUser : AppCompatActivity() {
    lateinit var viewModel: PoliceAnalyticsViewModel
    lateinit var service: PoliceDashboardService
    lateinit var binding: PoliceRequestDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.police_request_details)
        if (intent.extras != null) {
            val req_id = intent.extras!!["req_id"].toString()
            service = PoliceDashboardService(req_id)
            viewModel = ViewModelProvider(
                this,
                PoliceAnalyticsViewModelFactory(service, application)
            ).get(PoliceAnalyticsViewModel::class.java)
            viewModel.analyticsData().observe(this, {

                tv_police_profile.text = it.status[0].police.phone
                tv_loc.text = it.status[0].police.location.lat.toString()
                tv_status.text = it.status[0].status
                tv_date.text = it.status[0].date
                tv_time.text = it.status[0].time
                tv_reqID.text = req_id
            })
        } else {
            Toast.makeText(this, "THere is Some Error", Toast.LENGTH_SHORT).show()
        }
        binding.backButton.setOnClickListener {
            finish()
            Log.e("TAG", "onCreate: finish")
        }
    }
}
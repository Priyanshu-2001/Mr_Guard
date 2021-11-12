package com.geek.mrguard.UI.dashBoard.commonUser

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.geek.mrguard.R
import com.geek.mrguard.adapters.AnalyticsAdapter
import com.geek.mrguard.databinding.PoliceDashboardBinding
import com.geek.mrguard.services.PoliceDashboardService
import com.geek.mrguard.viewModel.PoliceAnalyticsViewModel
import com.geek.mrguard.viewModel.PoliceAnalyticsViewModelFactory

class  AnalyticsDashboard : AppCompatActivity() {
    lateinit var binding :PoliceDashboardBinding
    lateinit var viewModel : PoliceAnalyticsViewModel
    lateinit var service : PoliceDashboardService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this , R.layout.police_dashboard)
        val req_id = "null"
        service = PoliceDashboardService(req_id)
        val pref = getSharedPreferences("tokenFile", MODE_PRIVATE)
        val isPolice = pref.getBoolean("isPolice",false)
        viewModel = ViewModelProvider(this,PoliceAnalyticsViewModelFactory(service,application)).get(PoliceAnalyticsViewModel::class.java)
        viewModel.analyticsData().observe(this,{
            binding.tvRequests.text = it.status.size.toString()
            Log.e("TAG", "onCreate: $it")
            val adapter  = AnalyticsAdapter(it,isPolice)
            binding.rvRequests.adapter = adapter
            binding.progressBar.visibility = View.GONE
        })
    }
}
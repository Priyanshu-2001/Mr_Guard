package com.geek.mrguard.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.geek.mrguard.data.policeAnalytics.PoliceAnalyticsData
import com.geek.mrguard.services.PoliceDashboardService

class PoliceAnalyticsViewModel(val service: PoliceDashboardService, val app: Application) :
    AndroidViewModel(
        app
    ) {
    lateinit var data : MutableLiveData<PoliceAnalyticsData>
    fun analyticsData(): MutableLiveData<PoliceAnalyticsData> {
        data = service.getPoliceHistory(app.applicationContext)
        return data
    }
}

class PoliceAnalyticsViewModelFactory(val service: PoliceDashboardService, val app : Application) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PoliceAnalyticsViewModel(service,app) as T
    }

}
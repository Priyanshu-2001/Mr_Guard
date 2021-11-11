package com.geek.mrguard.services

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import com.geek.mrguard.utils.PermissionCheck
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

class getUserLocation(private val mContext: Context) {
    var userLoc = MutableLiveData<Location>()

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            for (location in locationResult.locations) {
                userLoc.value = location
            }
        }

        override fun onLocationAvailability(locationAvailability: LocationAvailability) {
            super.onLocationAvailability(locationAvailability)
        }
    }


    fun checkSettingsAndStartLocationUpdates(
        locationRequest: LocationRequest,
        fusedLocationProviderClient: FusedLocationProviderClient
    ) {
        val request = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest).build()
        val client = LocationServices.getSettingsClient(mContext)
        val locationSettingsResponseTask = client.checkLocationSettings(request)
        locationSettingsResponseTask.addOnSuccessListener {
            startLocationUpdate(
                fusedLocationProviderClient,
                locationRequest
            )
        }.addOnFailureListener { e ->
            Toast.makeText(mContext, e.localizedMessage, Toast.LENGTH_SHORT).show()
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(mContext as Activity, 1001)
                } catch (sendIntentException: SendIntentException) {
                    sendIntentException.printStackTrace()
                }
            }
        }
    }

    private fun startLocationUpdate(
        fusedLocationProviderClient: FusedLocationProviderClient,
        locationRequest: LocationRequest
    ) {
        if (ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                mContext, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            PermissionCheck().requestPermissions(mContext as Activity)
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates(fusedLocationProviderClient: FusedLocationProviderClient) {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

}
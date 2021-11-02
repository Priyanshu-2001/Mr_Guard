package com.geek.mrguard.UI.dashBorad

import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.geek.mrguard.R
import com.geek.mrguard.databinding.ActivityDashBoardBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.util.*

class DashBoard : AppCompatActivity(),OnMapReadyCallback{


    lateinit var gMap: GoogleMap
    lateinit var binding: ActivityDashBoardBinding
    lateinit var bottomSheet: LinearLayout
    lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    var currentMarker: Marker? = null
    var fusedLocationProviderClient: FusedLocationProviderClient?=null
    var currentLocation: Location ?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
     binding = DataBindingUtil.setContentView(this,R.layout.activity_dash_board)
//        val mapFragment = supportFragmentManager.findFragmentById(R.id.frag_map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
     fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
     fetchLoc()


    }

    private fun fetchLoc() {
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)
         != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1000)
            return
        }
            val task = fusedLocationProviderClient?.lastLocation
        task?.addOnSuccessListener {location->
        if(location != null){
            this.currentLocation = location
            val mapFragment = supportFragmentManager.findFragmentById(R.id.
            frag_map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            1000 -> if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                fetchLoc()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap

        val latlong = LatLng(currentLocation?.latitude!!,currentLocation?.longitude!!)
        drawMarker(latlong)

        gMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDrag(p0: Marker?) {

            }

            override fun onMarkerDragEnd(p0: Marker?) {
                if(currentMarker != null)
                    currentMarker?.remove()

                val newLatLng = LatLng(p0?.position!!.latitude, p0.position.longitude)
                drawMarker(newLatLng)
                }

            override fun onMarkerDragStart(p0: Marker?) {

            }
        })

    }
    private fun drawMarker(latLong: LatLng){
       val markerOption =  MarkerOptions().position(latLong).title("Your Location")
            .snippet(getAddress(latLong.latitude,latLong.longitude)).draggable(true)
        gMap.animateCamera(CameraUpdateFactory.newLatLng(latLong))
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLong,15f))
        currentMarker =  gMap.addMarker(markerOption)
        currentMarker?.showInfoWindow()
    }
    private fun getAddress(lat:Double, lon:Double): String? {
       val geoCoder =  Geocoder(this, Locale.getDefault())
       val addresses = geoCoder.getFromLocation(lat,lon,1)
        return addresses[0].getAddressLine(0).toString()
    }

}
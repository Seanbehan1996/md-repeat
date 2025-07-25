package com.yourname.fitnesstracker.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

//GET LOCATION
class LocationTracker(context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private var locationCallback: LocationCallback? = null

    private var isTracking = false

    private var onLocationUpdate: ((Location) -> Unit)? = null
    //ENABLE PERMISSIONS
    @SuppressLint("MissingPermission")
    fun startTracking(onLocationUpdate: (Location) -> Unit) {
        if (isTracking) return
        this.onLocationUpdate = onLocationUpdate
        isTracking = true

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    onLocationUpdate(it)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, null)
    }

    fun stopTracking() {
        if (!isTracking) return
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        isTracking = false
        onLocationUpdate = null
    }
}

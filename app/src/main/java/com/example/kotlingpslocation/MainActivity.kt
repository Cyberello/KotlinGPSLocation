package com.example.kotlingpslocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.kotlingpslocation.ui.theme.KotlinGPSLocationTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var location: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = locationCallback()

        if (checkGPSPermission(locationCallback)) return

        getLastKnownGPSLocation()

        requestLocationUpdate(locationCallback)
    }

    private fun checkGPSPermission(locationCallback: LocationCallback): Boolean {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            val locationPermissionRequest = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                when {
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {

                        getLastKnownGPSLocation()

                        requestLocationUpdate(locationCallback)
                    }

                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {

                        getLastKnownGPSLocation()

                        requestLocationUpdate(locationCallback)
                    }

                    else -> {
                        // No location access granted.
                    }
                }
            }

            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )

            return true
        }
        return false
    }

    private fun locationCallback() = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            for (location in p0.locations) {

                this@MainActivity.location = location
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownGPSLocation() {

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->

                if (location != null) {
                    this.location = location

                    enableEdgeToEdge()
                    setContent {
                        KotlinGPSLocationTheme {
                            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                                KotlinGPS(
                                    gpsLatLon = location.latitude.toString() + ", " + location.longitude.toString(),
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
                    }
                }
            }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdate(locationCallback: LocationCallback) {

        fusedLocationClient.requestLocationUpdates(
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).apply {
                setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            }.build(),
            locationCallback,
            Looper.getMainLooper()
        )
    }
}

@Composable
fun KotlinGPS(gpsLatLon: String, modifier: Modifier = Modifier) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Hello KotlinGPS"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = gpsLatLon,
                modifier = modifier
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun KotlinGPSPreview() {
    KotlinGPSLocationTheme {
        KotlinGPS("Lat, Lon")
    }
}
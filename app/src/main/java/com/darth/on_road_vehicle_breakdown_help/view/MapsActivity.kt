package com.darth.on_road_vehicle_breakdown_help.view

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.darth.on_road_vehicle_breakdown_help.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.darth.on_road_vehicle_breakdown_help.databinding.ActivityMapsBinding
import com.google.android.material.snackbar.Snackbar

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private lateinit var binding: ActivityMapsBinding

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences

    private var trackBoolean : Boolean? = null

    var selectedLatitude: Double? = null
    var selectedLongitude: Double? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerLauncher()

        sharedPreferences = this.getSharedPreferences("com.darth.on_road_vehicle_breakdown_help", MODE_PRIVATE)
        trackBoolean = false

        selectedLatitude = 0.0
        selectedLongitude = 0.0

        // Casting
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {

                trackBoolean = sharedPreferences.getBoolean("tracking", false)

                if (!trackBoolean!!){
                    val userLocation = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,16f))
                    sharedPreferences.edit().putBoolean("tracking",true).apply()
                }
            }
        }
    }
    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        mMap.setOnMapLongClickListener(this)

        if (ContextCompat.checkSelfPermission(this@MapsActivity,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION)){
                Snackbar.make(binding.root,"Permission needed for location", Snackbar.LENGTH_LONG).setAction("Give Permission"){
                    // Request permission
                    permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                }.show()
            } else {
                // Request permission
                permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        } else {
            // Permission granted
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastLocation != null){
                val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,16f))
            }
            mMap.isMyLocationEnabled = true
        }
    }

    private fun registerLauncher(){

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result){
                if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    // Permission granted
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                    val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastLocation != null){
                        val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,16f))
                    }
                    mMap.isMyLocationEnabled = true
                }

            }else{
                // Permission denied
                Toast.makeText(this@MapsActivity, "Permission needed!", Toast.LENGTH_LONG).show()

            }
        }
    }


    override fun onMapLongClick(p0: LatLng) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(p0))

        selectedLatitude = p0.latitude
        selectedLatitude = p0.longitude
    }
}
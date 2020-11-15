package com.jintin.fancylocation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.jintin.fancylocation.livedata.LocationData
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        const val LOCATION_REQUEST = 22

        const val TYPE_LIVEDATA = 0
        const val TYPE_FLOW = 1
    }

    private val mainViewModel by viewModels<MainViewModel>()

    private val type = TYPE_FLOW

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        observeLocation()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            observeLocation()
        }
    }

    private fun observeLocation() {
        if (!checkPermission()) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST)
            return
        }

        when (type) {
            TYPE_LIVEDATA -> liveDataObserve()
            TYPE_FLOW -> flowObserve()
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    private fun flowObserve() {
        lifecycleScope.launch {

            @Suppress("EXPERIMENTAL_API_USAGE")
            mainViewModel.locationFlow.get().collect {
                updateUI(it)
            }
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    private fun liveDataObserve() {
        mainViewModel.locationLiveData.observe(this) {
            updateUI(it)
        }
    }

    private fun updateUI(locationData: LocationData) {
        findViewById<TextView>(R.id.location).text = when (locationData) {
            is LocationData.Success -> locationData.location.toString()
            is LocationData.Fail -> "Fail to get location"
        }
    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
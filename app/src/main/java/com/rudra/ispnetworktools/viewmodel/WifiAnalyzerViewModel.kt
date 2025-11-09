package com.rudra.ispnetworktools.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WifiAnalyzerViewModel(private val context: Context) : ViewModel() {

    private val _wifiInfo = MutableStateFlow<List<String>>(emptyList())
    val wifiInfo: StateFlow<List<String>> = _wifiInfo

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    init {
        startWifiScan()
    }

    private fun startWifiScan() {
        viewModelScope.launch {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                _wifiInfo.value = listOf("Location permission not granted")
                return@launch
            }
            val scanResults = wifiManager.scanResults
            val wifiList = mutableListOf<String>()
            for (result in scanResults) {
                wifiList.add(
                    "SSID: ${result.SSID}\nBSSID: ${result.BSSID}\nFrequency: ${result.frequency}MHz\nSignal Strength: ${result.level}dBm"
                )
            }
            _wifiInfo.value = wifiList
        }
    }
}

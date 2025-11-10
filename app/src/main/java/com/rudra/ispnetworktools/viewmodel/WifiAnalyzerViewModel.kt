package com.rudra.ispnetworktools.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WifiScanResult(
    val ssid: String,
    val bssid: String,
    val frequency: Int,
    val signalStrength: Int,
    val channel: Int
)

sealed class WifiAnalyzerUiState {
    object Idle : WifiAnalyzerUiState()
    object Scanning : WifiAnalyzerUiState()
    data class Success(val scanResults: List<WifiScanResult>) : WifiAnalyzerUiState()
    data class Error(val message: String) : WifiAnalyzerUiState()
}

@HiltViewModel
class WifiAnalyzerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _wifiState = MutableStateFlow<WifiAnalyzerUiState>(WifiAnalyzerUiState.Idle)
    val wifiState: StateFlow<WifiAnalyzerUiState> = _wifiState

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    fun startWifiScan() {
        viewModelScope.launch {
            _wifiState.value = WifiAnalyzerUiState.Scanning
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                _wifiState.value = WifiAnalyzerUiState.Error("Location permission not granted.")
                return@launch
            }
            try {
                val scanResults = wifiManager.scanResults
                val wifiList = scanResults.map { result ->
                    WifiScanResult(
                        ssid = result.SSID,
                        bssid = result.BSSID,
                        frequency = result.frequency,
                        signalStrength = result.level,
                        channel = getChannel(result.frequency)
                    )
                }
                _wifiState.value = WifiAnalyzerUiState.Success(wifiList)
            } catch (e: Exception) {
                _wifiState.value = WifiAnalyzerUiState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    private fun getChannel(frequency: Int): Int {
        return when (frequency) {
            in 2412..2484 -> (frequency - 2412) / 5 + 1
            in 5170..5825 -> (frequency - 5170) / 5 + 34
            else -> -1
        }
    }
}

package com.rudra.ispnetworktools.ui

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.rudra.ispnetworktools.viewmodel.WifiAnalyzerUiState
import com.rudra.ispnetworktools.viewmodel.WifiAnalyzerViewModel
import com.rudra.ispnetworktools.viewmodel.WifiScanResult

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WifiAnalyzerScreen(viewModel: WifiAnalyzerViewModel = hiltViewModel()) {
    val wifiState by viewModel.wifiState.collectAsState()
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            viewModel.startWifiScan()
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = { viewModel.startWifiScan() }) {
            Text("Scan for Wi-Fi Networks")
        }

        when (val state = wifiState) {
            is WifiAnalyzerUiState.Idle -> {
                Text("Click the button to start scanning.", modifier = Modifier.padding(top = 8.dp))
            }
            is WifiAnalyzerUiState.Scanning -> {
                CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
            }
            is WifiAnalyzerUiState.Success -> {
                LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                    items(state.scanResults) { result ->
                        WifiNetworkCard(network = result)
                    }
                }
            }
            is WifiAnalyzerUiState.Error -> {
                Text(text = state.message, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
fun WifiNetworkCard(network: WifiScanResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("SSID: ${network.ssid}", style = MaterialTheme.typography.bodyLarge)
                Text("BSSID: ${network.bssid}", style = MaterialTheme.typography.bodySmall)
                Text("Frequency: ${network.frequency} MHz (Channel: ${network.channel})", style = MaterialTheme.typography.bodySmall)
            }
            Text("${network.signalStrength} dBm", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

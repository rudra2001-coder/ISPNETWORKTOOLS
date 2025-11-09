package com.rudra.ispnetworktools.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.ispnetworktools.viewmodel.GeolocationViewModel

@Composable
fun GeolocationScreen(geolocationViewModel: GeolocationViewModel = viewModel()) {
    var ipAddress by remember { mutableStateOf("") }
    val geolocationResult by geolocationViewModel.geolocationResult.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = ipAddress,
            onValueChange = { ipAddress = it },
            label = { Text("Enter IP address") }
        )
        Button(onClick = { geolocationViewModel.getGeolocation(ipAddress) }) {
            Text("Get Geolocation")
        }
        if (geolocationResult.isNotEmpty()) {
            Text(geolocationResult)
        }
    }
}

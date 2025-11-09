package com.rudra.ispnetworktools.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.ispnetworktools.viewmodel.SpeedTestState
import com.rudra.ispnetworktools.viewmodel.SpeedTestViewModel

@Composable
fun SpeedTestScreen(speedTestViewModel: SpeedTestViewModel = hiltViewModel()) {
    val speedTestState by speedTestViewModel.speedTestState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val state = speedTestState) {
            is SpeedTestState.Idle -> {
                Button(onClick = { speedTestViewModel.startSpeedTest() }) {
                    Text("Start Speed Test")
                }
                Text("Click the button to start the test.")
            }
            is SpeedTestState.Loading -> {
                CircularProgressIndicator()
                Text("Preparing speed test...")
            }
            is SpeedTestState.SpeedTestProgress.Download -> {
                Text("Downloading...")
                LinearProgressIndicator(progress = state.percent / 100f)
                Text(String.format("%.2f Mbps", state.downloadSpeed))
            }
            is SpeedTestState.SpeedTestProgress.Upload -> {
                Text("Uploading...")
                LinearProgressIndicator(progress = state.percent / 100f)
                Text(String.format("%.2f Mbps", state.uploadSpeed))
            }
            is SpeedTestState.Success -> {
                Text("Test Complete!")
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Download")
                        Text(String.format("%.2f Mbps", state.downloadSpeed))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Upload")
                        Text(String.format("%.2f Mbps", state.uploadSpeed))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { speedTestViewModel.reset() }) {
                    Text("Run Again")
                }
            }
            is SpeedTestState.Error -> {
                Text("Error: ${state.message}")
                Button(onClick = { speedTestViewModel.reset() }) {
                    Text("Try Again")
                }
            }
        }
    }
}

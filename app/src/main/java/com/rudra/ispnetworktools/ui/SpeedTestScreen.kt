package com.rudra.ispnetworktools.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.ispnetworktools.data.PingStats
import com.rudra.ispnetworktools.viewmodel.SpeedTestState
import com.rudra.ispnetworktools.viewmodel.SpeedTestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedTestScreen(speedTestViewModel: SpeedTestViewModel = hiltViewModel()) {
    val speedTestState by speedTestViewModel.speedTestState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Internet Speed Test") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            when (val state = speedTestState) {
                is SpeedTestState.Idle -> {
                    IdleState(onStartClick = { speedTestViewModel.startSpeedTest() })
                }
                is SpeedTestState.Loading -> {
                    LoadingState()
                }
                is SpeedTestState.SpeedTestProgress.Download -> {
                    SpeedTestInProgress(true, state.percent, state.downloadSpeed)
                }
                is SpeedTestState.SpeedTestProgress.Upload -> {
                    SpeedTestInProgress(false, state.percent, state.uploadSpeed)
                }
                is SpeedTestState.Success -> {
                    SuccessState(state.downloadSpeed, state.uploadSpeed, state.pingStats, onRunAgain = { speedTestViewModel.reset() })
                }
                is SpeedTestState.Error -> {
                    ErrorState(state.message, onTryAgain = { speedTestViewModel.reset() })
                }
            }
        }
    }
}

@Composable
private fun IdleState(onStartClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            imageVector = Icons.Default.NetworkCheck,
            contentDescription = "Speed Test",
            modifier = Modifier.size(128.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Click the button to start the test.", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onStartClick,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(Icons.Default.Speed, contentDescription = null)
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Start Speed Test")
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text("Preparing speed test...")
    }
}

@Composable
private fun SpeedTestInProgress(isDownload: Boolean, progress: Float, speed: Double) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        val animatedProgress by animateFloatAsState(targetValue = progress / 100f, label = "progress_anim")
        val animatedSpeed by animateFloatAsState(targetValue = speed.toFloat(), label = "speed_anim")

        Text(
            text = if (isDownload) "Downloading..." else "Uploading...",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        SpeedGauge(progress = animatedProgress, speed = animatedSpeed, isDownload = isDownload)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoCard(title = "Server", value = "City, Country")
            InfoCard(title = "Ping", value = "12 ms")
        }
    }
}

@Composable
private fun SpeedGauge(progress: Float, speed: Float, isDownload: Boolean) {
    val gaugeColor = if (isDownload) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    Box(
        modifier = Modifier.size(250.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = gaugeColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 15.dp.toPx(), cap = StrokeCap.Round),
                size = size
            )
            drawArc(
                color = gaugeColor,
                startAngle = 135f,
                sweepAngle = progress * 270f,
                useCenter = false,
                style = Stroke(width = 15.dp.toPx(), cap = StrokeCap.Round),
                size = size
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (isDownload) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                contentDescription = if (isDownload) "Download" else "Upload",
                tint = gaugeColor
            )
            Text(
                text = String.format("%.2f", speed),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = gaugeColor
            )
            Text("Mbps", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SuccessState(downloadSpeed: Double, uploadSpeed: Double, pingStats: PingStats, onRunAgain: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text("Test Complete!", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ResultCard("Download", downloadSpeed, MaterialTheme.colorScheme.primary)
            ResultCard("Upload", uploadSpeed, MaterialTheme.colorScheme.secondary)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoCard(title = "Jitter", value = pingStats.jitter)
            InfoCard(title = "Packet Loss", value = pingStats.packetLoss)
        }

        Button(
            onClick = onRunAgain,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Run Again")
        }
    }
}

@Composable
private fun ResultCard(title: String, speed: Double, color: Color) {
    Card(
        modifier = Modifier.size(150.dp, 120.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                String.format("%.2f", speed),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text("Mbps", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun InfoCard(title: String, value: String) {
    Card(
        modifier = Modifier.padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ErrorState(message: String, onTryAgain: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            imageVector = Icons.Default.Public,
            contentDescription = "Error",
            modifier = Modifier.size(128.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Error: $message", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onTryAgain) {
            Text("Try Again")
        }
    }
}

package com.rudra.ispnetworktools.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.ispnetworktools.viewmodel.PingReply
import com.rudra.ispnetworktools.viewmodel.PingStats
import com.rudra.ispnetworktools.viewmodel.PingUiState
import com.rudra.ispnetworktools.viewmodel.PingViewModel

@Composable
fun PingScreen(viewModel: PingViewModel = hiltViewModel()) {
    var host by remember { mutableStateOf("google.com") }
    val pingState by viewModel.pingResult.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (pingState is PingUiState.Idle) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("Host or IP Address") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = pingState is PingUiState.Idle
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.ping(host) },
                enabled = pingState is PingUiState.Idle
            ) {
                Text("Ping")
            }
        } else {
            when (val state = pingState) {
                is PingUiState.Pinging -> {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                }
                is PingUiState.InProgress -> {
                    PingResults(replies = state.replies)
                }
                is PingUiState.Complete -> {
                    PingResults(replies = state.replies)
                    PingSummary(stats = state.stats)
                }
                is PingUiState.Error -> {
                    Text(text = state.message, modifier = Modifier.padding(top = 8.dp))
                }
                else -> {}
            }
        }
    }
}

@Composable
fun PingResults(replies: List<PingReply>) {
    LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
        items(replies) { reply ->
            Text("Seq: ${reply.sequence}, TTL: ${reply.ttl}, Time: ${reply.time}ms")
        }
    }
}

@Composable
fun PingSummary(stats: PingStats) {
    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("--- Ping Statistics ---", style = MaterialTheme.typography.titleMedium)
            Text("${stats.transmitted} packets transmitted, ${stats.received} received, ${stats.packetLoss}% packet loss")
            Text("rtt min/avg/max/mdev = ${stats.min}/${stats.avg}/${stats.max}/${stats.stddev} ms")
        }
    }
}
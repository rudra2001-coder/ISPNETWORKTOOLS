package com.rudra.ispnetworktools.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.ispnetworktools.viewmodel.TraceHop
import com.rudra.ispnetworktools.viewmodel.TracerouteUiState
import com.rudra.ispnetworktools.viewmodel.TracerouteViewModel

@Composable
fun TracerouteScreen(viewModel: TracerouteViewModel = hiltViewModel()) {
    var host by remember { mutableStateOf("google.com") }
    val tracerouteState by viewModel.tracerouteState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (tracerouteState is TracerouteUiState.Idle) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("Host or IP Address") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = tracerouteState is TracerouteUiState.Idle
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.performTraceroute(host) },
                enabled = tracerouteState is TracerouteUiState.Idle
            ) {
                Text("Traceroute")
            }
        } else {
            when (val state = tracerouteState) {
                is TracerouteUiState.InProgress -> {
                    TracerouteResults(hops = state.hops)
                }
                is TracerouteUiState.Complete -> {
                    TracerouteResults(hops = state.hops)
                    Button(onClick = { viewModel.reset() }, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Run Again")
                    }
                }
                is TracerouteUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Button(onClick = { viewModel.reset() }, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Try Again")
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun TracerouteResults(hops: List<TraceHop>) {
    LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
        items(hops) { hop ->
            TraceHopItem(hop)
        }
    }
}

@Composable
fun TraceHopItem(hop: TraceHop) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = "${hop.hopNumber}", modifier = Modifier.weight(0.1f))
        Column(modifier = Modifier.weight(0.9f)) {
            Text(text = hop.hostName ?: hop.ipAddress)
            if (hop.hostName != null) {
                Text(text = hop.ipAddress)
            }
            Text(text = hop.responseTimes.joinToString())
        }
    }
}
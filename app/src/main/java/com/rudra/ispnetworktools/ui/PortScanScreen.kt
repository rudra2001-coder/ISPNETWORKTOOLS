package com.rudra.ispnetworktools.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.ispnetworktools.viewmodel.PortScanResult
import com.rudra.ispnetworktools.viewmodel.PortScanUiState
import com.rudra.ispnetworktools.viewmodel.PortScanViewModel

@Composable
fun PortScanScreen(viewModel: PortScanViewModel = hiltViewModel()) {
    var host by remember { mutableStateOf("google.com") }
    var startPort by remember { mutableStateOf("1") }
    var endPort by remember { mutableStateOf("1024") }
    val scanState by viewModel.scanState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (scanState is PortScanUiState.Idle) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("Host or IP Address") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = scanState is PortScanUiState.Idle
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startPort,
                            onValueChange = { startPort = it },
                            label = { Text("Start Port") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = scanState is PortScanUiState.Idle
                        )
                        OutlinedTextField(
                            value = endPort,
                            onValueChange = { endPort = it },
                            label = { Text("End Port") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = scanState is PortScanUiState.Idle
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.startScan(host, startPort, endPort) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Scan Ports")
            }
        } else {
            when (val state = scanState) {
                is PortScanUiState.Scanning -> {
                    CircularProgressIndicator(modifier = Modifier.height(24.dp))
                    PortScanResults(results = state.results)
                }
                is PortScanUiState.Complete -> {
                    Button(
                        onClick = { viewModel.reset() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Scan Again")
                    }
                    PortScanResults(results = state.results)
                }
                is PortScanUiState.Error -> {
                    Text(state.message)
                    Button(
                        onClick = { viewModel.reset() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Try Again")
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun PortScanResults(results: List<PortScanResult>) {
    LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
        items(results) { result ->
            if (result.isOpen) {
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("Port ${result.port} is open (${result.service})", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
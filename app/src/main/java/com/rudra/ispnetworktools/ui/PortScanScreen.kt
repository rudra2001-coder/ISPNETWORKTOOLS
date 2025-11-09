package com.rudra.ispnetworktools.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.ispnetworktools.viewmodel.PortScanViewModel

@Composable
fun PortScanScreen(viewModel: PortScanViewModel = hiltViewModel()) {
    var host by remember { mutableStateOf("google.com") }
    var startPort by remember { mutableStateOf("1") }
    var endPort by remember { mutableStateOf("1024") }
    val isScanning by viewModel.isScanning.collectAsState()
    val openPorts by viewModel.scanResult.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = host,
            onValueChange = { host = it },
            label = { Text("Host or IP Address") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = startPort,
                onValueChange = { startPort = it },
                label = { Text("Start Port") },
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            OutlinedTextField(
                value = endPort,
                onValueChange = { endPort = it },
                label = { Text("End Port") },
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
        }
        Button(
            onClick = { viewModel.startScan(host, startPort.toInt(), endPort.toInt()) },
            modifier = Modifier.padding(top = 8.dp),
            enabled = !isScanning
        ) {
            if (isScanning) {
                CircularProgressIndicator()
            } else {
                Text("Scan Ports")
            }
        }
        LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
            items(openPorts) { port ->
                Text("Port $port is open")
            }
        }
    }
}

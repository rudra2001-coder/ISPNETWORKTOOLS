package com.rudra.ispnetworktools.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import com.rudra.ispnetworktools.viewmodel.PingViewModel

@Composable
fun PingScreen(viewModel: PingViewModel = hiltViewModel()) {
    var host by remember { mutableStateOf("google.com") }
    val pingResult by viewModel.pingResult.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = host,
            onValueChange = { host = it },
            label = { Text("Host or IP Address") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { viewModel.ping(host) },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Ping")
        }
        Text(
            text = pingResult,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

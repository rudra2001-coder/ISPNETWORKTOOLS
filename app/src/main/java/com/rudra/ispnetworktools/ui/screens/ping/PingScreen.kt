package com.rudra.ispnetworktools.ui.screens.ping

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun PingScreen(viewModel: PingViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
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

class PingViewModel : ViewModel() {
    private val _pingResult = MutableStateFlow("")
    val pingResult = _pingResult.asStateFlow()

    fun ping(host: String) {
        viewModelScope.launch {
            _pingResult.value = ""
            withContext(Dispatchers.IO) {
                try {
                    val process = ProcessBuilder("ping", "-c", "4", host).start()
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        launch(Dispatchers.Main) {
                            _pingResult.value += line + "\n"
                        }
                    }
                    process.waitFor()
                } catch (e: Exception) {
                    launch(Dispatchers.Main) {
                        _pingResult.value = "Error: ${e.message}"
                    }
                }
            }
        }
    }
}

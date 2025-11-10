package com.rudra.ispnetworktools.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.ispnetworktools.viewmodel.SmtpTestUiState
import com.rudra.ispnetworktools.viewmodel.SmtpTestViewModel

@Composable
fun SmtpTestScreen(viewModel: SmtpTestViewModel = hiltViewModel()) {
    var server by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("25") }
    val smtpTestState by viewModel.smtpTestState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (smtpTestState is SmtpTestUiState.Idle) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = server,
                        onValueChange = { server = it },
                        label = { Text("SMTP Server") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("Port") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { viewModel.testSmtpConnection(server, port.toInt()) }) {
                Text("Test Connection")
            }
        } else {
            when (val state = smtpTestState) {
                is SmtpTestUiState.Testing -> {
                    CircularProgressIndicator()
                }
                is SmtpTestUiState.Success -> {
                    Text(text = state.message)
                    Button(onClick = { viewModel.resetState() }) {
                        Text("Test Again")
                    }
                }
                is SmtpTestUiState.Error -> {
                    Text(text = state.message)
                    Button(onClick = { viewModel.resetState() }) {
                        Text("Try Again")
                    }
                }
                else -> {}
            }
        }
    }
}
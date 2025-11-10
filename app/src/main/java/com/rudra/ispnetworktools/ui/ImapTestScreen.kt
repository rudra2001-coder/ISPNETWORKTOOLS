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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.ispnetworktools.viewmodel.ImapTestUiState
import com.rudra.ispnetworktools.viewmodel.ImapTestViewModel

@Composable
fun ImapTestScreen(viewModel: ImapTestViewModel = hiltViewModel()) {
    var server by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("143") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val imapTestState by viewModel.imapTestState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (imapTestState is ImapTestUiState.Idle) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = server,
                        onValueChange = { server = it },
                        label = { Text("IMAP Server") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("Port") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { viewModel.testImapConnection(server, port.toInt(), username, password) }) {
                Text("Test Connection")
            }
        } else {
            when (val state = imapTestState) {
                is ImapTestUiState.Testing -> {
                    CircularProgressIndicator()
                }
                is ImapTestUiState.Success -> {
                    Text(text = state.message)
                    Button(onClick = { viewModel.resetState() }) {
                        Text("Test Again")
                    }
                }
                is ImapTestUiState.Error -> {
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
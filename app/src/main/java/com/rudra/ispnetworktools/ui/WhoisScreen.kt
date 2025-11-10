package com.rudra.ispnetworktools.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.rudra.ispnetworktools.viewmodel.WhoisUiState
import com.rudra.ispnetworktools.viewmodel.WhoisViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhoisScreen(viewModel: WhoisViewModel = hiltViewModel()) {
    var domain by remember { mutableStateOf("google.com") }
    val whoisState by viewModel.whoisState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("WHOIS Lookup") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (whoisState is WhoisUiState.Idle) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = domain,
                            onValueChange = { domain = it },
                            label = { Text("Domain Name or IP Address") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            enabled = whoisState is WhoisUiState.Idle
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.performWhois(domain) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = domain.isNotBlank()
                ) {
                    Text("Perform WHOIS")
                }
            } else {
                when (val state = whoisState) {
                    is WhoisUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.height(24.dp))
                    }
                    is WhoisUiState.Success -> {
                        WhoisResult(result = state.result)
                    }
                    is WhoisUiState.Error -> {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun WhoisResult(result: String) {
    val scrollState = rememberScrollState()
    Card(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
        Text(
            text = result,
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(scrollState)
        )
    }
}
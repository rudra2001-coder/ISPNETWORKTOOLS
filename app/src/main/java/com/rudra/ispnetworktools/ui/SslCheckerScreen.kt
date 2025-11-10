package com.rudra.ispnetworktools.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.ispnetworktools.viewmodel.SslCertificateInfo
import com.rudra.ispnetworktools.viewmodel.SslCheckUiState
import com.rudra.ispnetworktools.viewmodel.SslCheckerViewModel

@Composable
fun SslCheckerScreen(viewModel: SslCheckerViewModel = hiltViewModel()) {
    var hostname by remember { mutableStateOf("google.com") }
    val sslCheckState by viewModel.sslCheckState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (sslCheckState is SslCheckUiState.Idle) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = hostname,
                        onValueChange = { hostname = it },
                        label = { Text("Hostname") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { viewModel.checkSslCertificate(hostname) }) {
                Text("Check SSL Certificate")
            }
        } else {
            when (val state = sslCheckState) {
                is SslCheckUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                }
                is SslCheckUiState.Success -> {
                    SslCertificateDetails(certificateInfo = state.certificateInfo)
                }
                is SslCheckUiState.Error -> {
                    Text(text = state.message, modifier = Modifier.padding(top = 8.dp))
                }
                else -> {}
            }
        }
    }
}

@Composable
fun SslCertificateDetails(certificateInfo: SslCertificateInfo) {
    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Subject: ${certificateInfo.subject}")
            Text("Issuer: ${certificateInfo.issuer}")
            Text("Valid From: ${certificateInfo.validFrom}")
            Text("Valid Until: ${certificateInfo.validUntil}")
            Text("Version: ${certificateInfo.version}")
            Text("Serial Number: ${certificateInfo.serialNumber}")
        }
    }
}

package com.rudra.ispnetworktools.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.rudra.ispnetworktools.viewmodel.WhoisViewModel

@Composable
fun WhoisScreen(viewModel: WhoisViewModel = hiltViewModel()) {
    var domain by remember { mutableStateOf("google.com") }
    val whoisResult by viewModel.whoisResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = domain,
            onValueChange = { domain = it },
            label = { Text("Domain Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { viewModel.performWhois(domain) },
            modifier = Modifier.padding(top = 8.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Perform WHOIS")
            }
        }
        val scrollState = rememberScrollState()
        Text(
            text = whoisResult,
            modifier = Modifier.padding(top = 8.dp).verticalScroll(scrollState)
        )
    }
}

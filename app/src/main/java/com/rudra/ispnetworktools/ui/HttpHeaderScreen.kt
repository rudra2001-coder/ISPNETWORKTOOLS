package com.rudra.ispnetworktools.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.ispnetworktools.viewmodel.HttpHeader
import com.rudra.ispnetworktools.viewmodel.HttpHeaderUiState
import com.rudra.ispnetworktools.viewmodel.HttpHeaderViewModel

@Composable
fun HttpHeaderScreen(viewModel: HttpHeaderViewModel = hiltViewModel()) {
    var url by remember { mutableStateOf("https://google.com") }
    val headerState by viewModel.headerState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (headerState is HttpHeaderUiState.Idle) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { viewModel.fetchHeaders(url) }) {
                Text("Fetch Headers")
            }
        } else {
            when (val state = headerState) {
                is HttpHeaderUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                }
                is HttpHeaderUiState.Success -> {
                    HeaderList(headers = state.headers)
                }
                is HttpHeaderUiState.Error -> {
                    Text(text = state.message, modifier = Modifier.padding(top = 8.dp))
                }
                else -> {}
            }
        }
    }
}

@Composable
fun HeaderList(headers: List<HttpHeader>) {
    LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
        items(headers) { header ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "${header.key}:", fontWeight = FontWeight.Bold)
                    Text(text = header.value)
                }
            }
        }
    }
}
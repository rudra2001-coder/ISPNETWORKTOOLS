package com.rudra.ispnetworktools.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import com.rudra.ispnetworktools.viewmodel.DnsLookupUiState
import com.rudra.ispnetworktools.viewmodel.DnsLookupViewModel
import com.rudra.ispnetworktools.viewmodel.DnsRecordType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DnsLookupScreen(viewModel: DnsLookupViewModel = hiltViewModel()) {
    var hostname by remember { mutableStateOf("google.com") }
    var selectedRecordType by remember { mutableStateOf(DnsRecordType.A) }
    val lookupState by viewModel.lookupResult.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (lookupState is DnsLookupUiState.Idle) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = hostname,
                            onValueChange = { hostname = it },
                            label = { Text("Hostname") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        RecordTypeSelector(selectedRecordType = selectedRecordType, onRecordTypeSelected = { selectedRecordType = it })
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.performDnsLookup(hostname, selectedRecordType) }
            ) {
                Text("Lookup DNS")
            }
        } else {
            when (val state = lookupState) {
                is DnsLookupUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                }
                is DnsLookupUiState.Success -> {
                    Text(text = state.results, modifier = Modifier.padding(top = 8.dp))
                }
                is DnsLookupUiState.Error -> {
                    Text(text = state.message, modifier = Modifier.padding(top = 8.dp))
                }
                else -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordTypeSelector(selectedRecordType: DnsRecordType, onRecordTypeSelected: (DnsRecordType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(
            value = selectedRecordType.name,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DnsRecordType.values().forEach { recordType ->
                DropdownMenuItem(
                    text = { Text(recordType.name) },
                    onClick = {
                        onRecordTypeSelected(recordType)
                        expanded = false
                    }
                )
            }
        }
    }
}

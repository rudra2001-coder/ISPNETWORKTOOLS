package com.rudra.ispnetworktools.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject


sealed class DnsLookupUiState {
    object Idle : DnsLookupUiState()
    object Loading : DnsLookupUiState()
    data class Success(val results: String) : DnsLookupUiState()
    data class Error(val message: String) : DnsLookupUiState()
}

enum class DnsRecordType {
    A, AAAA, CNAME, MX, TXT, NS, SOA
}

@HiltViewModel
class DnsLookupViewModel @Inject constructor() : ViewModel() {

    private val _lookupResult = MutableStateFlow<DnsLookupUiState>(DnsLookupUiState.Idle)
    val lookupResult = _lookupResult.asStateFlow()

    fun performDnsLookup(hostname: String, recordType: DnsRecordType) {
        viewModelScope.launch {
            _lookupResult.value = DnsLookupUiState.Loading
            withContext(Dispatchers.IO) {
                try {
                    val command = listOf("dig", "+nocomments", "+noquestion", "+noauthority", "+noadditional", "+nostats", hostname, recordType.name)
                    val process = ProcessBuilder(command).start()
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    val output = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        output.append(line).append("\n")
                    }
                    process.waitFor()

                    if(output.isBlank()) {
                         _lookupResult.value = DnsLookupUiState.Error("No records found.")
                    } else {
                        _lookupResult.value = DnsLookupUiState.Success(output.toString())
                    }
                } catch (e: Exception) {
                    _lookupResult.value = DnsLookupUiState.Error("Error: ${e.message}")
                }
            }
        }
    }
}
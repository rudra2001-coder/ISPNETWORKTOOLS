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

sealed class WhoisUiState {
    object Idle : WhoisUiState()
    object Loading : WhoisUiState()
    data class Success(val result: String) : WhoisUiState()
    data class Error(val message: String) : WhoisUiState()
}

@HiltViewModel
class WhoisViewModel @Inject constructor() : ViewModel() {

    private val _whoisState = MutableStateFlow<WhoisUiState>(WhoisUiState.Idle)
    val whoisState = _whoisState.asStateFlow()

    fun performWhois(domain: String) {
        viewModelScope.launch {
            _whoisState.value = WhoisUiState.Loading
            withContext(Dispatchers.IO) {
                try {
                    val process = ProcessBuilder("whois", domain).start()
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    val output = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        output.append(line).append("\n")
                    }
                    process.waitFor()
                    if (output.isNotBlank()) {
                        _whoisState.value = WhoisUiState.Success(output.toString())
                    } else {
                         val errorReader = BufferedReader(InputStreamReader(process.errorStream))
                         val errorOutput = StringBuilder()
                         var errorLine: String?
                        while (errorReader.readLine().also { errorLine = it } != null) {
                            errorOutput.append(errorLine).append("\n")
                        }
                        _whoisState.value = WhoisUiState.Error(errorOutput.toString())
                    }
                } catch (e: Exception) {
                    _whoisState.value = WhoisUiState.Error("Error: ${e.message}")
                }
            }
        }
    }
}
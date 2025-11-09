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

@HiltViewModel
class TracerouteViewModel @Inject constructor() : ViewModel() {

    private val _tracerouteResult = MutableStateFlow("")
    val tracerouteResult = _tracerouteResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun performTraceroute(host: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _tracerouteResult.value = ""
            withContext(Dispatchers.IO) {
                try {
                    val process = ProcessBuilder("traceroute", host).start()
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    val output = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        output.append(line).append("\n")
                    }
                    process.waitFor()
                    withContext(Dispatchers.Main) {
                        _tracerouteResult.value = output.toString()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        _tracerouteResult.value = "Error: ${e.message}"
                    }
                }
            }
            _isLoading.value = false
        }
    }
}

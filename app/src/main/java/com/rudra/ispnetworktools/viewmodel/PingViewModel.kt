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
class PingViewModel @Inject constructor() : ViewModel() {
    private val _pingResult = MutableStateFlow("")
    val pingResult = _pingResult.asStateFlow()

    fun ping(host: String) {
        viewModelScope.launch {
            _pingResult.value = ""
            withContext(Dispatchers.IO) {
                try {
                    val process = ProcessBuilder("ping", "-c", "4", host).start()
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    var line: String?
                    val output = StringBuilder()
                    while (reader.readLine().also { line = it } != null) {
                        output.append(line).append("\n")
                    }
                    process.waitFor()
                    withContext(Dispatchers.Main) {
                        _pingResult.value = output.toString()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        _pingResult.value = "Error: ${e.message}"
                    }
                }
            }
        }
    }
}

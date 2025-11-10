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
import java.util.regex.Pattern
import javax.inject.Inject

data class TraceHop(
    val hopNumber: Int,
    val ipAddress: String,
    val hostName: String? = null,
    val responseTimes: List<String> = emptyList(),
    val success: Boolean
)

sealed class TracerouteUiState {
    object Idle : TracerouteUiState()
    data class InProgress(val hops: List<TraceHop>) : TracerouteUiState()
    data class Complete(val hops: List<TraceHop>) : TracerouteUiState()
    data class Error(val message: String) : TracerouteUiState()
}

@HiltViewModel
class TracerouteViewModel @Inject constructor() : ViewModel() {

    private val _tracerouteState = MutableStateFlow<TracerouteUiState>(TracerouteUiState.Idle)
    val tracerouteState = _tracerouteState.asStateFlow()

    fun performTraceroute(host: String) {
        viewModelScope.launch {
            _tracerouteState.value = TracerouteUiState.InProgress(emptyList())
            withContext(Dispatchers.IO) {
                try {
                    val process = ProcessBuilder("traceroute", host).start()
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    val errorReader = BufferedReader(InputStreamReader(process.errorStream))

                    val currentHops = mutableListOf<TraceHop>()

                    // Read output lines
                    reader.useLines { lines ->
                        lines.forEach { line ->
                            val hop = parseTracerouteLine(line)
                            if (hop != null) {
                                currentHops.add(hop)
                                // Update state on main thread
                                withContext(Dispatchers.Main) {
                                    _tracerouteState.value = TracerouteUiState.InProgress(currentHops.toList())
                                }
                            }
                        }
                    }

                    // Check for errors
                    val errorOutput = errorReader.use(BufferedReader::readText)
                    if (errorOutput.isNotBlank()) {
                        withContext(Dispatchers.Main) {
                            _tracerouteState.value = TracerouteUiState.Error("Command error: $errorOutput")
                        }
                        return@withContext
                    }

                    val exitCode = process.waitFor()
                    if (exitCode == 0) {
                        withContext(Dispatchers.Main) {
                            _tracerouteState.value = TracerouteUiState.Complete(currentHops.toList())
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            _tracerouteState.value = TracerouteUiState.Error("Traceroute failed with exit code: $exitCode")
                        }
                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        _tracerouteState.value = TracerouteUiState.Error("Error: ${e.message}")
                    }
                }
            }
        }
    }

    fun reset() {
        _tracerouteState.value = TracerouteUiState.Idle
    }

    private fun parseTracerouteLine(line: String): TraceHop? {
        if (line.isBlank()) return null
        val trimmedLine = line.trim()

        // Pattern for successful hops: "1  gateway (192.168.1.1)  1.234 ms  1.123 ms  1.345 ms"
        val hopPattern = Pattern.compile("^(\\d+)\\s+([\\w.-]+)(?:\\s+\\(([^)]+)\\))?\\s+(.*)")
        val matcher = hopPattern.matcher(trimmedLine)

        if (matcher.find()) {
            val hop = matcher.group(1)?.toIntOrNull() ?: return null
            val hostOrIp = matcher.group(2) ?: ""
            val ipInParen = matcher.group(3)
            val rest = matcher.group(4) ?: ""

            val ip = ipInParen ?: hostOrIp
            val host = if (ipInParen != null && hostOrIp != ip) hostOrIp else null

            // Extract response times (everything that ends with "ms")
            val timePattern = Pattern.compile("([\\d.]+)\\s*ms")
            val timeMatcher = timePattern.matcher(rest)
            val times = mutableListOf<String>()
            while (timeMatcher.find()) {
                timeMatcher.group(1)?.let { times.add(it) }
            }

            return TraceHop(hop, ip, host, times, true)
        }

        // Pattern for timeout hops: "2  * * *"
        val timeoutPattern = Pattern.compile("^(\\d+)\\s+\\*\\s+\\*\\s+\\*")
        val timeoutMatcher = timeoutPattern.matcher(trimmedLine)
        if (timeoutMatcher.find()) {
            val hop = timeoutMatcher.group(1)?.toIntOrNull() ?: return null
            return TraceHop(hop, "*", null, emptyList(), false)
        }

        return null
    }
}
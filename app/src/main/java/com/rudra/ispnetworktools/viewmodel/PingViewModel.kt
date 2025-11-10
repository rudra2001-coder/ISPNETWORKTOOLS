package com.rudra.ispnetworktools.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.ispnetworktools.data.TestResult
import com.rudra.ispnetworktools.data.TestResultDao
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

data class PingReply(val sequence: Int, val ttl: Int, val time: Float)
data class PingStats(val transmitted: Int, val received: Int, val packetLoss: Float, val min: Float, val avg: Float, val max: Float, val stddev: Float)

sealed class PingUiState {
    object Idle : PingUiState()
    object Pinging : PingUiState()
    data class InProgress(val replies: List<PingReply>) : PingUiState()
    data class Complete(val replies: List<PingReply>, val stats: PingStats, val rawOutput: String) : PingUiState()
    data class Error(val message: String) : PingUiState()
}

@HiltViewModel
class PingViewModel @Inject constructor(
    private val testResultDao: TestResultDao
) : ViewModel() {
    private val _pingResult = MutableStateFlow<PingUiState>(PingUiState.Idle)
    val pingResult = _pingResult.asStateFlow()

    fun ping(host: String) {
        viewModelScope.launch {
            _pingResult.value = PingUiState.Pinging
            withContext(Dispatchers.IO) {
                try {
                    val process = ProcessBuilder("ping", "-c", "10", host).start()
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    val replies = mutableListOf<PingReply>()
                    val fullOutput = StringBuilder()

                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        fullOutput.append(line).append("\n")
                        parsePingLine(line)?.let { 
                            replies.add(it)
                            _pingResult.value = PingUiState.InProgress(replies.toList())
                        }
                    }

                    process.waitFor()
                    val stats = parsePingStats(fullOutput.toString())
                    _pingResult.value = PingUiState.Complete(replies.toList(), stats, fullOutput.toString())

                    // Save the result to the database
                    testResultDao.insert(TestResult(testType = "Ping", result = "Host: $host\n\n${fullOutput.toString()}"))

                } catch (e: Exception) {
                    _pingResult.value = PingUiState.Error("Error: ${e.message}")
                }
            }
        }
    }

    private fun parsePingLine(line: String?): PingReply? {
        val pattern = Pattern.compile("icmp_seq=(\\d+) ttl=(\\d+) time=([\\d.]+) ms")
        val matcher = pattern.matcher(line)
        return if (matcher.find()) {
            PingReply(
                sequence = matcher.group(1).toInt(),
                ttl = matcher.group(2).toInt(),
                time = matcher.group(3).toFloat()
            )
        } else null
    }

    private fun parsePingStats(output: String): PingStats {
        val packetPattern = Pattern.compile("(\\d+) packets transmitted, (\\d+) received, ([\\d.]+)% packet loss")
        val rttPattern = Pattern.compile("rtt min/avg/max/mdev = ([\\d.]+)/([\\d.]+)/([\\d.]+)/([\\d.]+) ms")
        
        val packetMatcher = packetPattern.matcher(output)
        val rttMatcher = rttPattern.matcher(output)

        val transmitted = if(packetMatcher.find()) packetMatcher.group(1).toInt() else 0
        val received = if(packetMatcher.find(0)) packetMatcher.group(2).toInt() else 0
        val packetLoss = if(packetMatcher.find(0)) packetMatcher.group(3).toFloat() else 0f

        val min = if(rttMatcher.find()) rttMatcher.group(1).toFloat() else 0f
        val avg = if(rttMatcher.find(0)) rttMatcher.group(2).toFloat() else 0f
        val max = if(rttMatcher.find(0)) rttMatcher.group(3).toFloat() else 0f
        val stddev = if(rttMatcher.find(0)) rttMatcher.group(4).toFloat() else 0f

        return PingStats(transmitted, received, packetLoss, min, avg, max, stddev)
    }
}
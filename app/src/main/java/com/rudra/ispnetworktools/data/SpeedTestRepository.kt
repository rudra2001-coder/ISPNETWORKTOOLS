package com.rudra.ispnetworktools.data

import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import fr.bmartel.speedtest.model.SpeedTestMode
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigDecimal
import java.util.regex.Pattern
import javax.inject.Inject

class SpeedTestRepository @Inject constructor() {

    fun getSpeedTestResults(): Flow<SpeedTestResult> = callbackFlow {
        val speedTestSocket = SpeedTestSocket()

        val listener = object : ISpeedTestListener {
            override fun onCompletion(report: fr.bmartel.speedtest.SpeedTestReport) {
                val speedInMbps = report.transferRateOctet.divide(BigDecimal(1000000)).toDouble()

                when (report.speedTestMode) {
                    SpeedTestMode.DOWNLOAD -> {
                        trySend(SpeedTestResult.Download(speedInMbps))
                        speedTestSocket.startUpload("http://ipv4.ikoula.testdebit.info/10M.iso", 10000000)
                    }
                    SpeedTestMode.UPLOAD -> {
                        trySend(SpeedTestResult.Upload(speedInMbps))
                        // Ping test is now started from the viewmodel
                    }
                    else -> { /* Handle other modes if needed */ }
                }
            }

            override fun onError(speedTestError: SpeedTestError, errorMessage: String) {
                trySend(SpeedTestResult.Error(errorMessage))
                close()
            }

            override fun onProgress(percent: Float, report: fr.bmartel.speedtest.SpeedTestReport) {
                if (report.transferRateOctet > BigDecimal.ZERO) {
                    val progress = report.transferRateOctet.divide(BigDecimal(1000000)).toDouble()
                    when (report.speedTestMode) {
                        SpeedTestMode.DOWNLOAD -> {
                            trySend(SpeedTestResult.DownloadProgress(percent, progress))
                        }
                        SpeedTestMode.UPLOAD -> {
                            trySend(SpeedTestResult.UploadProgress(percent, progress))
                        }
                        else -> { /* Handle other modes if needed */ }
                    }
                }
            }
        }

        speedTestSocket.addSpeedTestListener(listener)
        speedTestSocket.startDownload("http://ipv4.ikoula.testdebit.info/10M.iso")

        awaitClose {
            speedTestSocket.removeSpeedTestListener(listener)
            speedTestSocket.forceStopTask()
        }
    }

    fun getPingStats(host: String): PingStats {
        try {
            val process = ProcessBuilder("ping", "-c", "10", "-i", "0.2", host).start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            process.waitFor()
            return parsePingOutput(output.toString())
        } catch (e: Exception) {
            return PingStats("N/A", "N/A", "N/A")
        }
    }

    private fun parsePingOutput(output: String): PingStats {
        val packetLossPattern = Pattern.compile("(\\d+)% packet loss")
        val packetLossMatcher = packetLossPattern.matcher(output)
        val packetLoss = if (packetLossMatcher.find()) packetLossMatcher.group(1) + "%" else "N/A"

        val rttPattern = Pattern.compile("round-trip min/avg/max/stddev = ([\\d.]+)/([\\d.]+)/([\\d.]+)/([\\d.]+) ms")
        val rttMatcher = rttPattern.matcher(output)
        val jitter = if (rttMatcher.find()) rttMatcher.group(4) + " ms" else "N/A"
        val avgLatency = if (rttMatcher.find()) rttMatcher.group(2) + " ms" else "N/A"

        return PingStats(packetLoss, jitter, avgLatency)
    }
}

data class PingStats(val packetLoss: String, val jitter: String, val avgLatency: String)

sealed class SpeedTestResult {
    data class Download(val downloadSpeed: Double) : SpeedTestResult()
    data class Upload(val uploadSpeed: Double) : SpeedTestResult()
    data class DownloadProgress(val percent: Float, val downloadSpeed: Double) : SpeedTestResult()
    data class UploadProgress(val percent: Float, val uploadSpeed: Double) : SpeedTestResult()
    data class Error(val message: String) : SpeedTestResult()
}
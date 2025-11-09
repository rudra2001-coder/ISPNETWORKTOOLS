package com.rudra.ispnetworktools.data

import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import fr.bmartel.speedtest.model.SpeedTestMode
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.math.BigDecimal
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
                        // Upload test starts after download is complete
                        speedTestSocket.startUpload("http://ipv4.ikoula.testdebit.info/10M.iso", 10000000)
                    }
                    SpeedTestMode.UPLOAD -> {
                        trySend(SpeedTestResult.Upload(speedInMbps))
                        close() // Close the flow when both tests are complete
                    }
                    else -> {
                        // Handle other modes if needed
                    }
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
                        else -> {
                            // Handle other modes if needed
                        }
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
}

sealed class SpeedTestResult {
    data class Download(val downloadSpeed: Double) : SpeedTestResult()
    data class Upload(val uploadSpeed: Double) : SpeedTestResult()
    data class DownloadProgress(val percent: Float, val downloadSpeed: Double) : SpeedTestResult()
    data class UploadProgress(val percent: Float, val uploadSpeed: Double) : SpeedTestResult()
    data class Error(val message: String) : SpeedTestResult()
}
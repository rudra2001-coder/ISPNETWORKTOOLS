package com.rudra.ispnetworktools.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.ispnetworktools.data.SpeedTestRepository
import com.rudra.ispnetworktools.data.SpeedTestResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpeedTestViewModel @Inject constructor(
    private val speedTestRepository: SpeedTestRepository
) : ViewModel() {

    private val _speedTestState = MutableStateFlow<SpeedTestState>(SpeedTestState.Idle)
    val speedTestState: StateFlow<SpeedTestState> = _speedTestState

    private var downloadSpeed: Double = 0.0
    private var uploadSpeed: Double = 0.0

    fun startSpeedTest() {
        viewModelScope.launch {
            _speedTestState.value = SpeedTestState.Loading
            speedTestRepository.getSpeedTestResults().collect {
                when (it) {
                    is SpeedTestResult.DownloadProgress -> {
                        _speedTestState.value = SpeedTestState.SpeedTestProgress.Download(it.percent, it.downloadSpeed)
                    }
                    is SpeedTestResult.UploadProgress -> {
                        _speedTestState.value = SpeedTestState.SpeedTestProgress.Upload(it.percent, it.uploadSpeed)
                    }
                    is SpeedTestResult.Download -> {
                        downloadSpeed = it.downloadSpeed
                    }
                    is SpeedTestResult.Upload -> {
                        uploadSpeed = it.uploadSpeed
                        _speedTestState.value = SpeedTestState.Success(downloadSpeed, uploadSpeed)
                    }
                    is SpeedTestResult.Error -> {
                        _speedTestState.value = SpeedTestState.Error(it.message)
                    }
                }
            }
        }
    }

    fun reset() {
        _speedTestState.value = SpeedTestState.Idle
    }
}

sealed class SpeedTestState {
    object Idle : SpeedTestState()
    object Loading : SpeedTestState()
    sealed class SpeedTestProgress: SpeedTestState() {
        data class Download(val percent: Float, val downloadSpeed: Double) : SpeedTestProgress()
        data class Upload(val percent: Float, val uploadSpeed: Double) : SpeedTestProgress()
    }
    data class Success(val downloadSpeed: Double, val uploadSpeed: Double) : SpeedTestState()
    data class Error(val message: String) : SpeedTestState()
}

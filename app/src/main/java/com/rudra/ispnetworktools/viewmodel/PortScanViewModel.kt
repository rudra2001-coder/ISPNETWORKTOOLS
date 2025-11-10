package com.rudra.ispnetworktools.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject

data class PortScanResult(val port: Int, val isOpen: Boolean, val service: String = "unknown")

sealed class PortScanUiState {
    object Idle : PortScanUiState()
    data class Scanning(val results: List<PortScanResult>) : PortScanUiState()
    data class Complete(val results: List<PortScanResult>) : PortScanUiState()
    data class Error(val message: String) : PortScanUiState()
}

@HiltViewModel
class PortScanViewModel @Inject constructor() : ViewModel() {
    private val _scanState = MutableStateFlow<PortScanUiState>(PortScanUiState.Idle)
    val scanState = _scanState.asStateFlow()

    fun startScan(host: String, startPortStr: String, endPortStr: String) {
        val startPort = startPortStr.toIntOrNull()
        val endPort = endPortStr.toIntOrNull()

        if (startPort == null || endPort == null || startPort > endPort) {
            _scanState.value = PortScanUiState.Error("Invalid port range.")
            return
        }

        viewModelScope.launch {
            _scanState.value = PortScanUiState.Scanning(emptyList())
            withContext(Dispatchers.IO) {
                val openPorts = mutableListOf<PortScanResult>()
                try {
                    for (port in startPort..endPort) {
                        try {
                            val socket = Socket()
                            socket.connect(InetSocketAddress(host, port), 100)
                            socket.close()
                            val result = PortScanResult(port, true, getServiceName(port))
                            openPorts.add(result)
                            _scanState.value = PortScanUiState.Scanning(openPorts.toList())
                        } catch (e: Exception) {
                            // Port is closed or filtered
                        }
                    }
                    _scanState.value = PortScanUiState.Complete(openPorts.toList())
                } catch (e: Exception) {
                    _scanState.value = PortScanUiState.Error("Error scanning host '$host': ${e.message}")
                }
            }
        }
    }
    
    fun reset() {
        _scanState.value = PortScanUiState.Idle
    }

    private fun getServiceName(port: Int): String {
        return when (port) {
            20 -> "FTP (Data)"
            21 -> "FTP (Control)"
            22 -> "SSH"
            23 -> "Telnet"
            25 -> "SMTP"
            53 -> "DNS"
            80 -> "HTTP"
            110 -> "POP3"
            143 -> "IMAP"
            443 -> "HTTPS"
            else -> "Unknown"
        }
    }
}

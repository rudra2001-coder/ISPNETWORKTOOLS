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

@HiltViewModel
class PortScanViewModel @Inject constructor() : ViewModel() {
    private val _scanResult = MutableStateFlow<List<Int>>(emptyList())
    val scanResult = _scanResult.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    fun startScan(host: String, startPort: Int, endPort: Int) {
        viewModelScope.launch {
            _isScanning.value = true
            _scanResult.value = emptyList()
            withContext(Dispatchers.IO) {
                val openPorts = mutableListOf<Int>()
                for (port in startPort..endPort) {
                    try {
                        val socket = Socket()
                        // Set a timeout to avoid waiting too long on closed ports
                        socket.connect(InetSocketAddress(host, port), 50)
                        socket.close()
                        openPorts.add(port)
                    } catch (e: Exception) {
                        // Port is likely closed or host is unreachable
                    }
                }
                _scanResult.value = openPorts
            }
            _isScanning.value = false
        }
    }
}

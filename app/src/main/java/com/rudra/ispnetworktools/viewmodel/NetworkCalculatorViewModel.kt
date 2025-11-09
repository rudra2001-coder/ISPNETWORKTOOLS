package com.rudra.ispnetworktools.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.apache.commons.net.util.SubnetUtils

class NetworkCalculatorViewModel : ViewModel() {

    private val _calculationResult = MutableStateFlow("")
    val calculationResult: StateFlow<String> = _calculationResult

    fun calculate(cidr: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val utils = SubnetUtils(cidr)
                val info = utils.info
                val result = buildString {
                    append("Address: ").append(info.address).append("\n")
                    append("Netmask: ".padEnd(20)).append(info.netmask).append("\n")
                    append("Network Address: ".padEnd(20)).append(info.networkAddress).append("\n")
                    append("Broadcast Address: ".padEnd(20)).append(info.broadcastAddress).append("\n")
                    append("Low Address: ".padEnd(20)).append(info.lowAddress).append("\n")
                    append("High Address: ".padEnd(20)).append(info.highAddress).append("\n")
                    append("Usable Addresses: ".padEnd(20)).append(info.addressCountLong)
                }
                _calculationResult.value = result
            } catch (e: Exception) {
                _calculationResult.value = "Error: ${e.message}"
            }
        }
    }
}

package com.rudra.ispnetworktools.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import javax.inject.Inject

@HiltViewModel
class DnsLookupViewModel @Inject constructor() : ViewModel() {

    private val _lookupResult = MutableStateFlow<List<String>>(emptyList())
    val lookupResult = _lookupResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun performDnsLookup(hostname: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _lookupResult.value = emptyList()
            withContext(Dispatchers.IO) {
                try {
                    val addresses = InetAddress.getAllByName(hostname)
                    val addressList = addresses.map { it.hostAddress ?: "" }
                    _lookupResult.value = addressList
                } catch (e: Exception) {
                    _lookupResult.value = listOf("Error: ${e.message}")
                }
            }
            _isLoading.value = false
        }
    }
}

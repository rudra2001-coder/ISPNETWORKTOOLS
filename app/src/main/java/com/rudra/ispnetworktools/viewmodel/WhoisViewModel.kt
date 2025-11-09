package com.rudra.ispnetworktools.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.net.whois.WhoisClient
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class WhoisViewModel @Inject constructor() : ViewModel() {

    private val _whoisResult = MutableStateFlow("")
    val whoisResult = _whoisResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun performWhois(domain: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _whoisResult.value = ""
            withContext(Dispatchers.IO) {
                try {
                    val whois = WhoisClient()
                    whois.connect(WhoisClient.DEFAULT_HOST)
                    val result = whois.query(domain)
                    whois.disconnect()
                    _whoisResult.value = result
                } catch (e: IOException) {
                    _whoisResult.value = "Error: ${e.message}"
                }
            }
            _isLoading.value = false
        }
    }
}

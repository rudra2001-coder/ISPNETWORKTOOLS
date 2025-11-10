package com.rudra.ispnetworktools.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

data class HttpHeader(val key: String, val value: String)

sealed class HttpHeaderUiState {
    object Idle : HttpHeaderUiState()
    object Loading : HttpHeaderUiState()
    data class Success(val headers: List<HttpHeader>) : HttpHeaderUiState()
    data class Error(val message: String) : HttpHeaderUiState()
}

@HiltViewModel
class HttpHeaderViewModel @Inject constructor() : ViewModel() {

    private val _headerState = MutableStateFlow<HttpHeaderUiState>(HttpHeaderUiState.Idle)
    val headerState = _headerState.asStateFlow()

    fun fetchHeaders(urlString: String) {
        viewModelScope.launch {
            _headerState.value = HttpHeaderUiState.Loading
            withContext(Dispatchers.IO) {
                try {
                    val url = URL(urlString)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connect()

                    val headers = connection.headerFields.mapNotNull { (key, value) ->
                        if (key != null) {
                            HttpHeader(key, value.joinToString(", "))
                        } else {
                            null
                        }
                    }
                    _headerState.value = HttpHeaderUiState.Success(headers)
                    connection.disconnect()
                } catch (e: Exception) {
                    _headerState.value = HttpHeaderUiState.Error("Error: ${e.message}")
                }
            }
        }
    }
}
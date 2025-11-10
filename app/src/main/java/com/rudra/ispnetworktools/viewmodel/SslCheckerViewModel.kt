package com.rudra.ispnetworktools.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.cert.X509Certificate
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection
import java.net.URL


data class SslCertificateInfo(
    val subject: String,
    val issuer: String,
    val validFrom: String,
    val validUntil: String,
    val version: Int,
    val serialNumber: String
)

sealed class SslCheckUiState {
    object Idle : SslCheckUiState()
    object Loading : SslCheckUiState()
    data class Success(val certificateInfo: SslCertificateInfo) : SslCheckUiState()
    data class Error(val message: String) : SslCheckUiState()
}

@HiltViewModel
class SslCheckerViewModel @Inject constructor() : ViewModel() {

    private val _sslCheckState = MutableStateFlow<SslCheckUiState>(SslCheckUiState.Idle)
    val sslCheckState = _sslCheckState.asStateFlow()

    fun checkSslCertificate(hostname: String) {
        viewModelScope.launch {
            _sslCheckState.value = SslCheckUiState.Loading
            withContext(Dispatchers.IO) {
                try {
                    val url = URL("https://$hostname")
                    val connection = url.openConnection() as HttpsURLConnection
                    connection.connect()
                    val serverCertificates = connection.serverCertificates
                    val x509Certificate = serverCertificates[0] as X509Certificate

                    val certificateInfo = SslCertificateInfo(
                        subject = x509Certificate.subjectX500Principal.name,
                        issuer = x509Certificate.issuerX500Principal.name,
                        validFrom = x509Certificate.notBefore.toString(),
                        validUntil = x509Certificate.notAfter.toString(),
                        version = x509Certificate.version,
                        serialNumber = x509Certificate.serialNumber.toString(16)
                    )
                    _sslCheckState.value = SslCheckUiState.Success(certificateInfo)
                } catch (e: Exception) {
                    _sslCheckState.value = SslCheckUiState.Error("Error: ${e.message}")
                }
            }
        }
    }
}
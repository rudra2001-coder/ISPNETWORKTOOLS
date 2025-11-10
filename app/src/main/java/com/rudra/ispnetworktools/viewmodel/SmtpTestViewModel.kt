package com.rudra.ispnetworktools.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.net.smtp.SMTPClient
import org.apache.commons.net.smtp.SMTPReply
import javax.inject.Inject

sealed class SmtpTestUiState {
    object Idle : SmtpTestUiState()
    object Testing : SmtpTestUiState()
    data class Success(val message: String) : SmtpTestUiState()
    data class Error(val message: String) : SmtpTestUiState()
}

@HiltViewModel
class SmtpTestViewModel @Inject constructor() : ViewModel() {

    private val _smtpTestState = MutableStateFlow<SmtpTestUiState>(SmtpTestUiState.Idle)
    val smtpTestState = _smtpTestState.asStateFlow()

    fun testSmtpConnection(server: String, port: Int) {
        viewModelScope.launch {
            _smtpTestState.value = SmtpTestUiState.Testing
            withContext(Dispatchers.IO) {
                val smtp = SMTPClient()
                try {
                    smtp.connect(server, port)
                    val reply = smtp.replyCode

                    if (!SMTPReply.isPositiveCompletion(reply)) {
                        smtp.disconnect()
                        _smtpTestState.value = SmtpTestUiState.Error("SMTP server refused connection.")
                        return@withContext
                    }
                    
                    _smtpTestState.value = SmtpTestUiState.Success("Successfully connected to SMTP server: $server. Reply: ${smtp.replyString}")
                    smtp.disconnect()

                } catch (e: Exception) {
                    _smtpTestState.value = SmtpTestUiState.Error("Connection failed: ${e.message}")
                } finally {
                    if (smtp.isConnected) {
                        try {
                            smtp.disconnect()
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }
                }
            }
        }
    }
    fun resetState(){
        _smtpTestState.value = SmtpTestUiState.Idle
    }
}
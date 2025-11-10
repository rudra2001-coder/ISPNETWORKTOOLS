package com.rudra.ispnetworktools.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import javax.inject.Inject

sealed class FtpTestUiState {
    object Idle : FtpTestUiState()
    object Testing : FtpTestUiState()
    data class Success(val message: String) : FtpTestUiState()
    data class Error(val message: String) : FtpTestUiState()
}

@HiltViewModel
class FtpTestViewModel @Inject constructor() : ViewModel() {

    private val _ftpTestState = MutableStateFlow<FtpTestUiState>(FtpTestUiState.Idle)
    val ftpTestState = _ftpTestState.asStateFlow()

    fun testFtpConnection(server: String, port: Int, user: String, pass: String) {
        viewModelScope.launch {
            _ftpTestState.value = FtpTestUiState.Testing
            withContext(Dispatchers.IO) {
                val ftp = FTPClient()
                try {
                    ftp.connect(server, port)
                    val reply = ftp.replyCode

                    if (!FTPReply.isPositiveCompletion(reply)) {
                        ftp.disconnect()
                        _ftpTestState.value = FtpTestUiState.Error("FTP server refused connection.")
                        return@withContext
                    }

                    if (!ftp.login(user, pass)) {
                        _ftpTestState.value = FtpTestUiState.Error("Login failed. Check username/password.")
                        return@withContext
                    }
                    
                    _ftpTestState.value = FtpTestUiState.Success("Successfully connected and logged in to $server.")
                    ftp.logout()

                } catch (e: Exception) {
                    _ftpTestState.value = FtpTestUiState.Error("Connection failed: ${e.message}")
                } finally {
                    if (ftp.isConnected) {
                        try {
                            ftp.disconnect()
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }
                }
            }
        }
    }
    fun resetState(){
        _ftpTestState.value = FtpTestUiState.Idle
    }
}
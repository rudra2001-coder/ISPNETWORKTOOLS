package com.rudra.ispnetworktools.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.net.imap.IMAPClient
import javax.inject.Inject

sealed class ImapTestUiState {
    object Idle : ImapTestUiState()
    object Testing : ImapTestUiState()
    data class Success(val message: String) : ImapTestUiState()
    data class Error(val message: String) : ImapTestUiState()
}

@HiltViewModel
class ImapTestViewModel @Inject constructor() : ViewModel() {

    private val _imapTestState = MutableStateFlow<ImapTestUiState>(ImapTestUiState.Idle)
    val imapTestState = _imapTestState.asStateFlow()

    fun testImapConnection(server: String, port: Int, user: String, pass: String) {
        viewModelScope.launch {
            _imapTestState.value = ImapTestUiState.Testing
            withContext(Dispatchers.IO) {
                val imap = IMAPClient()
                try {
                    imap.connect(server, port)

                    if (!imap.isConnected) {
                        _imapTestState.value = ImapTestUiState.Error("IMAP server refused connection.")
                        return@withContext
                    }

                    if (!imap.login(user, pass)) {
                         _imapTestState.value = ImapTestUiState.Error("Login failed. Check username/password.")
                        return@withContext
                    }
                    
                    _imapTestState.value = ImapTestUiState.Success("Successfully connected and logged in to IMAP server: $server.")
                    imap.logout()

                } catch (e: Exception) {
                    _imapTestState.value = ImapTestUiState.Error("Connection failed: ${e.message}")
                } finally {
                    if (imap.isConnected) {
                        try {
                            imap.disconnect()
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }
                }
            }
        }
    }
    fun resetState(){
        _imapTestState.value = ImapTestUiState.Idle
    }
}
package com.rudra.ispnetworktools.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.net.pop3.POP3Client
import javax.inject.Inject

sealed class Pop3TestUiState {
    object Idle : Pop3TestUiState()
    object Testing : Pop3TestUiState()
    data class Success(val message: String) : Pop3TestUiState()
    data class Error(val message: String) : Pop3TestUiState()
}

@HiltViewModel
class Pop3TestViewModel @Inject constructor() : ViewModel() {

    private val _pop3TestState = MutableStateFlow<Pop3TestUiState>(Pop3TestUiState.Idle)
    val pop3TestState = _pop3TestState.asStateFlow()

    fun testPop3Connection(server: String, port: Int, user: String, pass: String) {
        viewModelScope.launch {
            _pop3TestState.value = Pop3TestUiState.Testing
            withContext(Dispatchers.IO) {
                val pop3 = POP3Client()
                try {
                    pop3.connect(server, port)

                    if (!pop3.isConnected) {
                        _pop3TestState.value = Pop3TestUiState.Error("POP3 server refused connection.")
                        return@withContext
                    }

                    if (!pop3.login(user, pass)) {
                         _pop3TestState.value = Pop3TestUiState.Error("Login failed. Check username/password.")
                        return@withContext
                    }
                    
                    _pop3TestState.value = Pop3TestUiState.Success("Successfully connected and logged in to POP3 server: $server.")
                    pop3.logout()

                } catch (e: Exception) {
                    _pop3TestState.value = Pop3TestUiState.Error("Connection failed: ${e.message}")
                } finally {
                    if (pop3.isConnected) {
                        try {
                            pop3.disconnect()
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }
                }
            }
        }
    }
    fun resetState(){
        _pop3TestState.value = Pop3TestUiState.Idle
    }
}
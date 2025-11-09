package com.rudra.ispnetworktools.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.ViewModel
import com.rudra.ispnetworktools.services.MyVpnService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PacketCaptureViewModel(private val context: Context) : ViewModel() {

    private val _capturedPackets = MutableStateFlow<List<String>>(emptyList())
    val capturedPackets: StateFlow<List<String>> = _capturedPackets

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing

    private val vpnIntent: Intent
        get() = Intent(context, MyVpnService::class.java)

    fun startCapture() {
        val vpnPrepareIntent = VpnService.prepare(context)
        if (vpnPrepareIntent != null) {
            (context as? Activity)?.startActivityForResult(vpnPrepareIntent, 0)
        } else {
            context.startService(vpnIntent)
            _isCapturing.value = true
        }
    }

    fun stopCapture() {
        context.stopService(vpnIntent)
        _isCapturing.value = false
    }
}

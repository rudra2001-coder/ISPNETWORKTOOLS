package com.rudra.ispnetworktools.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.ispnetworktools.data.AppDatabase
import com.rudra.ispnetworktools.data.TestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class WakeOnLanViewModel(context: Context) : ViewModel() {

    private val _wakeOnLanResult = MutableStateFlow("")
    val wakeOnLanResult: StateFlow<String> = _wakeOnLanResult

    private val testResultDao = AppDatabase.getDatabase(context).testResultDao()

    fun sendMagicPacket(macAddress: String, ipAddress: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val macBytes = getMacBytes(macAddress)
                val broadcastAddress = InetAddress.getByName(ipAddress)

                val magicPacket = ByteArray(6 + 16 * macBytes.size)
                for (i in 0..5) {
                    magicPacket[i] = 0xFF.toByte()
                }
                for (i in 1..16) {
                    System.arraycopy(macBytes, 0, magicPacket, i * macBytes.size, macBytes.size)
                }

                val packet = DatagramPacket(magicPacket, magicPacket.size, broadcastAddress, 9)
                val socket = DatagramSocket()
                socket.send(packet)
                socket.close()

                val result = "Magic packet sent to $macAddress"
                _wakeOnLanResult.value = result
                testResultDao.insert(TestResult(testType = "Wake on LAN", result = result))
            } catch (e: Exception) {
                val errorResult = "Error: ${e.message}"
                _wakeOnLanResult.value = errorResult
                testResultDao.insert(TestResult(testType = "Wake on LAN", result = errorResult))
            }
        }
    }

    private fun getMacBytes(macStr: String): ByteArray {
        val bytes = ByteArray(6)
        val hex = macStr.split("[:\\-]".toRegex()).toTypedArray()
        if (hex.size != 6) {
            throw IllegalArgumentException("Invalid MAC address.")
        }
        try {
            for (i in 0..5) {
                bytes[i] = hex[i].toInt(16).toByte()
            }
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid hex digit in MAC address.")
        }
        return bytes
    }
}

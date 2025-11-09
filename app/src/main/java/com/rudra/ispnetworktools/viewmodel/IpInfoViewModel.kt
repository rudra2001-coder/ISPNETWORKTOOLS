package com.rudra.ispnetworktools.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class IpInfoViewModel(private val context: Context) : ViewModel() {

    private val _ipInfo = MutableStateFlow("Loading IP information...")
    val ipInfo: StateFlow<String> = _ipInfo

    private val client = HttpClient(CIO)

    init {
        getIpInfo()
    }

    private fun getIpInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            val publicIp = getPublicIp()
            val localIp = getLocalIp()
            val gateway = getGateway()
            val dnsServers = getDnsServers()

            val info = buildString {
                append("Public IP: ").append(publicIp).append("\n")
                append("Local IP: ".padEnd(20)).append(localIp).append("\n")
                append("Gateway: ".padEnd(20)).append(gateway).append("\n")
                append("DNS Servers: ".padEnd(20)).append(dnsServers)
            }
            _ipInfo.value = info
        }
    }

    private suspend fun getPublicIp(): String {
        return try {
            client.get("https://api.ipify.org").bodyAsText()
        } catch (e: Exception) {
            "N/A"
        }
    }

    private fun getLocalIp(): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress = wifiManager.connectionInfo.ipAddress
        return if (ipAddress == 0) "N/A" else String.format(
            "%d.%d.%d.%d",
            ipAddress and 0xff,
            ipAddress shr 8 and 0xff,
            ipAddress shr 16 and 0xff,
            ipAddress shr 24 and 0xff
        )
    }

    private fun getGateway(): String {
        val connectivityManager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return "N/A"
        val linkProperties = connectivityManager.getLinkProperties(activeNetwork) ?: return "N/A"
        for (route in linkProperties.routes) {
            if (route.isDefaultRoute) {
                return route.gateway?.hostAddress ?: "N/A"
            }
        }
        return "N/A"
    }

    private fun getDnsServers(): String {
        val connectivityManager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return "N/A"
        val linkProperties = connectivityManager.getLinkProperties(activeNetwork) ?: return "N/A"
        return linkProperties.dnsServers.joinToString { it.hostAddress ?: "" }
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}

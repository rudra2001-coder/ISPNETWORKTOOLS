package com.rudra.ispnetworktools.services

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

class MyVpnService : VpnService() {

    private val TAG = "MyVpnService"
    private var vpnThread: Thread? = null
    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "VPN service started")
        vpnThread = Thread {
            runVpn()
        }
        vpnThread?.start()
        return START_STICKY
    }

    private fun runVpn() {
        try {
            vpnInterface = establishVpn()
            val vpnInput = FileInputStream(vpnInterface?.fileDescriptor)
            val vpnOutput = FileOutputStream(vpnInterface?.fileDescriptor)

            val buffer = ByteBuffer.allocate(32767)

            while (true) {
                val readBytes = vpnInput.read(buffer.array())
                if (readBytes > 0) {
                    buffer.limit(readBytes)
                    Log.d(TAG, "Packet captured: ${buffer.remaining()} bytes")
                    buffer.clear()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "VPN error", e)
        } finally {
            vpnInterface?.close()
        }
    }

    private fun establishVpn(): ParcelFileDescriptor? {
        val builder = Builder()
        builder.setSession("MyVpnService")
        builder.addAddress("10.0.0.2", 24)
        builder.addDnsServer("8.8.8.8")
        builder.addRoute("0.0.0.0", 0)
        return builder.establish()
    }

    override fun onDestroy() {
        super.onDestroy()
        vpnThread?.interrupt()
        Log.d(TAG, "VPN service stopped")
    }
}

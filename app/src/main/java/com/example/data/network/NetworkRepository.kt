package com.example.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import com.example.core.logging.FluxLogger
import com.example.core.logging.LogCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

data class NetworkTelemetry(
    val isConnected: Boolean,
    val connectionType: String,
    val linkSpeedMbps: Int,
    val dnsLatencyMs: Long,
    val isMetered: Boolean,
    val signalDescription: String
)

class NetworkRepository(private val context: Context) {

    suspend fun getNetworkTelemetry(): NetworkTelemetry = withContext(Dispatchers.IO) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNet = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(activeNet)

        val isConnected = caps != null && (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
        val isCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false

        val connType = when {
            isWifi -> "Wi-Fi"
            isCellular -> "Cellular (Mobile Data)"
            isConnected -> "Ethernet / Other"
            else -> "Disconnected"
        }

        var linkSpeed = 0
        if (isWifi) {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val wifiInfo: WifiInfo? = wm?.connectionInfo
            linkSpeed = wifiInfo?.linkSpeed ?: 0
        }

        val isMetered = cm?.isActiveNetworkMetered ?: false

        // Measure real TCP/DNS handshake latency to Google DNS (8.8.8.8:53)
        var latencyMs = -1L
        if (isConnected) {
            try {
                val start = System.currentTimeMillis()
                Socket().use { socket ->
                    socket.connect(InetSocketAddress("8.8.8.8", 53), 1500)
                }
                latencyMs = System.currentTimeMillis() - start
            } catch (_: Exception) {
                latencyMs = -1L
            }
        }

        NetworkTelemetry(
            isConnected = isConnected,
            connectionType = connType,
            linkSpeedMbps = linkSpeed,
            dnsLatencyMs = latencyMs,
            isMetered = isMetered,
            signalDescription = if (isConnected) "Connected via $connType (${if (latencyMs > 0) "${latencyMs}ms latency" else "Active"})"
            else "No internet connection detected"
        )
    }
}

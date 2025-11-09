package com.rudra.ispnetworktools.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiFind
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times

@Composable
fun WifiAnalyzerScreen() {
    var wifiNetworks by remember { mutableStateOf(emptyList<WifiNetwork>()) }
    var isLoading by remember { mutableStateOf(false) }
    var currentWifi by remember { mutableStateOf<WifiNetwork?>(null) }

    // Simulate loading WiFi networks
    LaunchedEffect(Unit) {
        isLoading = true
        // Simulate network scan delay
        kotlinx.coroutines.delay(2000)
        wifiNetworks = getSampleWifiNetworks()
        currentWifi = wifiNetworks.firstOrNull { it.isConnected }
        isLoading = false
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "WiFi Analyzer",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            CurrentConnectionCard(currentWifi = currentWifi)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Available Networks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Button(
                    onClick = { /* Refresh networks */ },
                    enabled = !isLoading
                ) {
                    Text("Refresh")
                }
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        items(wifiNetworks.size) { index ->
            WifiNetworkCard(network = wifiNetworks[index])
        }
    }
}

@Composable
fun CurrentConnectionCard(currentWifi: WifiNetwork?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (currentWifi != null) Icons.Default.Wifi
                    else Icons.Default.WifiOff,
                    contentDescription = "Current WiFi",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (currentWifi != null) "Connected" else "Not Connected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (currentWifi != null) {
                Column {
                    Text(
                        text = currentWifi.ssid,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Signal: ${currentWifi.signalStrength} dBm",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Frequency: ${currentWifi.frequency} MHz",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Security: ${currentWifi.security}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "No WiFi connection",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WifiNetworkCard(network: WifiNetwork) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when {
                            network.isConnected -> Icons.Default.Wifi
                            network.signalStrength > -50 -> Icons.Default.SignalWifiOff
                            else -> Icons.Default.WifiFind
                        },
                        contentDescription = "WiFi Signal",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = network.ssid,
                        fontWeight = if (network.isConnected) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Strength: ${network.signalStrength} dBm",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Security: ${network.security}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Signal strength indicator
            SignalStrengthIndicator(strength = network.signalStrength)
        }
    }
}

@Composable
fun SignalStrengthIndicator(strength: Int) {
    val bars = when {
        strength > -50 -> 4 // Excellent
        strength > -60 -> 3 // Good
        strength > -70 -> 2 // Fair
        strength > -80 -> 1 // Weak
        else -> 0 // Very weak
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((index + 1) * 6.dp)
                    .background(
                        color = if (index < bars) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    )
            )
        }
    }
}

data class WifiNetwork(
    val ssid: String,
    val signalStrength: Int,
    val frequency: Int,
    val security: String,
    val isConnected: Boolean = false
)

// Sample data for preview
private fun getSampleWifiNetworks(): List<WifiNetwork> {
    return listOf(
        WifiNetwork("Home_WiFi_5G", -45, 5200, "WPA2", true),
        WifiNetwork("Home_WiFi_2G", -55, 2400, "WPA2"),
        WifiNetwork("Neighbor_WiFi", -65, 2400, "WPA2"),
        WifiNetwork("Guest_Network", -72, 2400, "WPA2"),
        WifiNetwork("XfinityWiFi", -78, 2400, "Open"),
        WifiNetwork("AndroidAP", -82, 2400, "WPA2")
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewWifiAnalyzerScreen() {
    MaterialTheme {
        WifiAnalyzerScreen()
    }
}
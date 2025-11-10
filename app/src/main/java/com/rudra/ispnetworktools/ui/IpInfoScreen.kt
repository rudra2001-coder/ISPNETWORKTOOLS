package com.rudra.ispnetworktools.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

data class IpInfo(
    val ipAddress: String = "",
    val country: String = "",
    val city: String = "",
    val isp: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IpInfoScreen() {
    var ipInfo by remember { mutableStateOf<IpInfo?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var fetchTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(fetchTrigger) {
        if (fetchTrigger > 0) {
            isLoading = true
            errorMessage = null
            ipInfo = null
            try {
                delay(1500)
                if (Random.nextBoolean()) {
                    ipInfo = getMockIpInfo()
                } else {
                    throw Exception("Failed to fetch IP information. Please check your connection.")
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("IP Information") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isLoading) {
                    Spacer(modifier = Modifier.height(64.dp))
                    CircularProgressIndicator(modifier = Modifier.size(64.dp))
                    Text("Fetching IP details...", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 16.dp))
                } else {
                    AnimatedVisibility(visible = ipInfo != null) {
                        ipInfo?.let { IpInfoContent(ipInfo = it) }
                    }

                    AnimatedVisibility(visible = errorMessage != null) {
                        errorMessage?.let { ErrorState(message = it) }
                    }

                    if (ipInfo == null && errorMessage == null) {
                        InitialState()
                    }
                }
            }

            Button(
                onClick = { fetchTrigger++ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = !isLoading
            ) {
                Text(if (ipInfo == null && errorMessage == null) "Get My IP Information" else "Fetch Again")
            }
        }
    }
}

@Composable
private fun InitialState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(top = 64.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Public,
            contentDescription = "IP Info",
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Discover details about your public IP address, including location and ISP.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorState(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(top = 64.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Public,
            contentDescription = "Error",
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Text(text = message, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun IpInfoContent(ipInfo: IpInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        IpInfoCard(ipInfo)
    }
}

@Composable
fun IpInfoCard(ipInfo: IpInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            InfoRow(icon = Icons.Default.WifiTethering, label = "IP Address", value = ipInfo.ipAddress)
            InfoRow(icon = Icons.Default.Language, label = "Country", value = ipInfo.country)
            InfoRow(icon = Icons.Default.LocationCity, label = "City", value = ipInfo.city)
            InfoRow(icon = Icons.Default.Business, label = "ISP", value = ipInfo.isp)
            InfoRow(icon = Icons.Default.GpsFixed, label = "Location", value = "${ipInfo.latitude}, ${ipInfo.longitude}")
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun getMockIpInfo(): IpInfo {
    return IpInfo(
        ipAddress = "192.168.1.101",
        country = "United States",
        city = "Mountain View",
        isp = "Google LLC",
        latitude = 37.422,
        longitude = -122.084
    )
}

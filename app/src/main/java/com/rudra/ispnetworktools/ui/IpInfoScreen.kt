package com.rudra.ispnetworktools.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class IpInfo(
    val ipAddress: String = "",
    val country: String = "",
    val city: String = "",
    val isp: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

@Composable
fun IpInfoScreen() {
    var ipInfo by remember { mutableStateOf(IpInfo()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "IP Information",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        errorMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        if (ipInfo.ipAddress.isNotEmpty()) {
            IpInfoCard(ipInfo = ipInfo)
        }

        Button(
            onClick = {
                // TODO: Fetch IP information
                isLoading = true
                errorMessage = null
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Get IP Information")
        }
    }
}

@Composable
fun IpInfoCard(ipInfo: IpInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoRow("IP Address", ipInfo.ipAddress)
            InfoRow("Country", ipInfo.country)
            InfoRow("City", ipInfo.city)
            InfoRow("ISP", ipInfo.isp)
            InfoRow("Location", "${ipInfo.latitude}, ${ipInfo.longitude}")
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontWeight = FontWeight.Normal
        )
    }
}
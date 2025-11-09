package com.rudra.ispnetworktools.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.InetAddress

@Composable
fun TracerouteScreen() {
    var targetHost by remember { mutableStateOf("google.com") }
    var isTracing by remember { mutableStateOf(false) }
    var traceResults by remember { mutableStateOf<List<TraceHop>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Traceroute",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = targetHost,
                onValueChange = { targetHost = it },
                label = { Text("Host or IP address") },
                placeholder = { Text("e.g., google.com, 8.8.8.8") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Target host")
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (targetHost.isNotBlank()) {
                        startTraceroute(
                            targetHost = targetHost,
                            onStart = {
                                isTracing = true
                                traceResults = emptyList()
                                errorMessage = null
                            },
                            onProgress = { hop ->
                                traceResults = traceResults + hop
                            },
                            onComplete = {
                                isTracing = false
                            },
                            onError = { error ->
                                isTracing = false
                                errorMessage = error
                            }
                        )
                    }
                },
                enabled = !isTracing && targetHost.isNotBlank(),
                modifier = Modifier.height(56.dp)
            ) {
                if (isTracing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start traceroute")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isTracing) "Tracing..." else "Start")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("google.com", "github.com", "8.8.8.8", "1.1.1.1").forEach { host ->
                FilterChip(
                    selected = targetHost == host,
                    onClick = { targetHost = host },
                    label = { Text(host) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results section
        when {
            isTracing -> {
                TracingInProgress(
                    currentHop = traceResults.size,
                    targetHost = targetHost
                )
            }

            errorMessage != null -> {
                ErrorCard(
                    message = errorMessage!!,
                    onRetry = {
                        errorMessage = null
                        // Retry logic would go here
                    }
                )
            }

            traceResults.isNotEmpty() -> {
                TraceResultsCard(
                    results = traceResults,
                    targetHost = targetHost,
                    onClear = {
                        traceResults = emptyList()
                    }
                )
            }

            else -> {
                EmptyState()
            }
        }
    }
}

@Composable
private fun TracingInProgress(currentHop: Int, targetHost: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tracing route to $targetHost",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Current hop: $currentHop",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This may take a few moments...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TraceResultsCard(
    results: List<TraceHop>,
    targetHost: String,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trace Results - $targetHost",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Refresh, contentDescription = "Clear results")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Summary
            val successfulHops = results.count { it.success }
            val totalHops = results.size
            Text(
                text = "Completed: $successfulHops/$totalHops hops",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Results list
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results) { hop ->
                    TraceHopItem(hop = hop)
                }
            }
        }
    }
}

@Composable
private fun TraceHopItem(hop: TraceHop) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (hop.success) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hop number
            Text(
                text = "${hop.hopNumber}.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.width(32.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Hop details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (hop.success) {
                    Text(
                        text = hop.hostName ?: hop.ipAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                    Text(
                        text = hop.ipAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    Text(
                        text = "Request timed out",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Response times
                if (hop.success) {
                    Text(
                        text = "Times: ${hop.responseTimes.joinToString(" ms, ")} ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Status indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (hop.success) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        shape = MaterialTheme.shapes.small
                    )
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Traceroute Failed",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No Traceroute Results",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter a hostname or IP address and click Start to begin tracing",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// Data classes
data class TraceHop(
    val hopNumber: Int,
    val ipAddress: String,
    val hostName: String?,
    val responseTimes: List<Long>,
    val success: Boolean
)

// Simulated traceroute function
private fun startTraceroute(
    targetHost: String,
    onStart: () -> Unit,
    onProgress: (TraceHop) -> Unit,
    onComplete: () -> Unit,
    onError: (String) -> Unit
) {
    // This would be replaced with actual traceroute implementation
    // For now, we'll simulate the process
    onStart()

    // Simulate async operation
    androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
        kotlinx.coroutines.GlobalScope.launch {
            try {
                // Simulate hops with delays
                val maxHops = 20
                var destinationReached = false

                for (hop in 1..maxHops) {
                    delay(500) // Simulate network delay

                    // Simulate hop results
                    val success = hop < 10 || hop == maxHops
                    val responseTimes = if (success) {
                        listOf(
                            (20L..100L).random(),
                            (20L..100L).random(),
                            (20L..100L).random()
                        )
                    } else {
                        emptyList()
                    }

                    val hopResult = TraceHop(
                        hopNumber = hop,
                        ipAddress = if (success) "192.168.${hop / 256}.${hop % 256}" else "0.0.0.0",
                        hostName = if (success && hop < 10) "router-${hop}.example.com" else null,
                        responseTimes = responseTimes,
                        success = success
                    )

                    onProgress(hopResult)

                    if (hop == maxHops) {
                        destinationReached = true
                    }

                    if (destinationReached) break
                }

                onComplete()
            } catch (e: Exception) {
                onError("Failed to trace route: ${e.message}")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTracerouteScreen() {
    MaterialTheme {
        TracerouteScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTracerouteWithResults() {
    MaterialTheme {
        TracerouteScreen()
    }
}
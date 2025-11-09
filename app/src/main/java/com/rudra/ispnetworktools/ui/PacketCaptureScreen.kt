package com.rudra.ispnetworktools.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacketCaptureScreen() {
    var captureState by remember { mutableStateOf(CaptureState.STOPPED) }
    var packets by remember { mutableStateOf(emptyList<NetworkPacket>()) }
    var filter by remember { mutableStateOf(PacketFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var captureStats by remember { mutableStateOf(CaptureStats()) }

    val filteredPackets = packets
        .filter { packet ->
            (filter == PacketFilter.ALL || matchesFilter(packet, filter)) &&
                    (searchQuery.isEmpty() ||
                            packet.source.contains(searchQuery, ignoreCase = true) ||
                            packet.destination.contains(searchQuery, ignoreCase = true) ||
                            packet.protocol.contains(searchQuery, ignoreCase = true))
        }

    LaunchedEffect(captureState) {
        if (captureState == CaptureState.RUNNING) {
            // Simulate packet capture
            // In real implementation, this would connect to a packet capture service
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Packet Capture",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filters")
                    }
                    IconButton(onClick = {
                        packets = emptyList()
                        captureStats = CaptureStats()
                    }) {
                        Icon(Icons.Default.ClearAll, contentDescription = "Clear")
                    }
                }
            )
        },
        bottomBar = {
            CaptureControlBar(
                captureState = captureState,
                packetCount = packets.size,
                onStartCapture = {
                    captureState = CaptureState.RUNNING
                    // Start actual packet capture here
                    simulatePacketCapture { packet ->
                        packets = packets + packet
                        captureStats = updateStats(captureStats, packet)
                    }
                },
                onStopCapture = {
                    captureState = CaptureState.STOPPED
                    // Stop packet capture here
                },
                onPauseCapture = {
                    captureState = CaptureState.PAUSED
                    // Pause packet capture here
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Statistics bar
            if (captureState != CaptureState.STOPPED) {
                CaptureStatsBar(stats = captureStats, captureState = captureState)
            }

            // Search and info bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search packets...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${filteredPackets.size} packets",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Protocol filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PacketFilter.entries.forEach { packetFilter ->
                    FilterChip(
                        selected = filter == packetFilter,
                        onClick = { filter = packetFilter },
                        label = { Text(packetFilter.displayName) },
                        leadingIcon = {
                            Icon(
                                imageVector = packetFilter.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Packets list
            when {
                captureState == CaptureState.STOPPED && packets.isEmpty() -> {
                    EmptyCaptureState()
                }

                filteredPackets.isEmpty() && searchQuery.isNotEmpty() -> {
                    EmptySearchState(searchQuery = searchQuery)
                }

                else -> {
                    PacketsList(
                        packets = filteredPackets,
                        onPacketClick = { packet ->
                            // Show packet details
                        }
                    )
                }
            }
        }
    }

    if (showFilterDialog) {
        AdvancedFilterDialog(
            onDismiss = { showFilterDialog = false },
            onApplyFilters = { advancedFilter ->
                // Apply advanced filters
            }
        )
    }
}

@Composable
private fun CaptureControlBar(
    captureState: CaptureState,
    packetCount: Int,
    onStartCapture: () -> Unit,
    onStopCapture: () -> Unit,
    onPauseCapture: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = when (captureState) {
                        CaptureState.STOPPED -> "Ready to capture"
                        CaptureState.RUNNING -> "Capturing..."
                        CaptureState.PAUSED -> "Paused"
                    },
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "$packetCount packets captured",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (captureState) {
                    CaptureState.STOPPED -> {
                        Button(
                            onClick = onStartCapture,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start")
                        }
                    }

                    CaptureState.RUNNING -> {
                        OutlinedButton(onClick = onPauseCapture) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pause")
                        }
                        Button(
                            onClick = onStopCapture,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop")
                        }
                    }

                    CaptureState.PAUSED -> {
                        Button(
                            onClick = onStartCapture,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resume")
                        }
                        OutlinedButton(onClick = onStopCapture) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptureStatsBar(stats: CaptureStats, captureState: CaptureState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CaptureStat(
                value = "${stats.packetsPerSecond}",
                label = "Pkts/s",
                highlight = captureState == CaptureState.RUNNING
            )
            CaptureStat(
                value = stats.totalPackets.toString(),
                label = "Total"
            )
            CaptureStat(
                value = "${stats.tcpPercent}%",
                label = "TCP"
            )
            CaptureStat(
                value = "${stats.udpPercent}%",
                label = "UDP"
            )
            CaptureStat(
                value = "${stats.otherPercent}%",
                label = "Other"
            )
        }
    }
}

@Composable
private fun CaptureStat(value: String, label: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = if (highlight) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PacketsList(packets: List<NetworkPacket>, onPacketClick: (NetworkPacket) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(packets, key = { it.id }) { packet ->
            PacketListItem(
                packet = packet,
                onClick = { onPacketClick(packet) }
            )
        }
    }
}

@Composable
private fun PacketListItem(packet: NetworkPacket, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Protocol and time row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = when (packet.protocol) {
                                    "TCP" -> MaterialTheme.colorScheme.primary
                                    "UDP" -> MaterialTheme.colorScheme.secondary
                                    "ICMP" -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.outline
                                },
                                shape = MaterialTheme.shapes.small
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = packet.protocol,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = formatTime(packet.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Source and destination
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = packet.source,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Source",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "To",
                    modifier = Modifier
                        .size(16.dp)
                        .padding(horizontal = 8.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = packet.destination,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Destination",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Packet info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${packet.length} bytes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = packet.info,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AdvancedFilterDialog(
    onDismiss: () -> Unit,
    onApplyFilters: (AdvancedFilter) -> Unit
) {
    var sourceIp by remember { mutableStateOf("") }
    var destIp by remember { mutableStateOf("") }
    var protocol by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Advanced Filters") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = sourceIp,
                    onValueChange = { sourceIp = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Source IP") },
                    placeholder = { Text("192.168.1.1") }
                )
                OutlinedTextField(
                    value = destIp,
                    onValueChange = { destIp = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Destination IP") },
                    placeholder = { Text("10.0.0.1") }
                )
                OutlinedTextField(
                    value = protocol,
                    onValueChange = { protocol = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Protocol") },
                    placeholder = { Text("TCP, UDP, ICMP") }
                )
                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Port") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("80, 443, etc.") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val filter = AdvancedFilter(
                        sourceIp = sourceIp.ifEmpty { null },
                        destIp = destIp.ifEmpty { null },
                        protocol = protocol.ifEmpty { null },
                        port = port.toIntOrNull()
                    )
                    onApplyFilters(filter)
                    onDismiss()
                }
            ) {
                Text("Apply Filters")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EmptyCaptureState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.NetworkCheck,
            contentDescription = "No capture",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Packet Capture",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start capturing to see network packets in real-time",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun EmptySearchState(searchQuery: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = "No results",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Packets Found",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No packets match \"$searchQuery\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Data classes and enums
data class NetworkPacket(
    val id: String,
    val timestamp: Date,
    val source: String,
    val destination: String,
    val protocol: String,
    val length: Int,
    val info: String
)

data class CaptureStats(
    val totalPackets: Int = 0,
    val packetsPerSecond: Int = 0,
    val tcpPercent: Int = 0,
    val udpPercent: Int = 0,
    val otherPercent: Int = 0
)

data class AdvancedFilter(
    val sourceIp: String? = null,
    val destIp: String? = null,
    val protocol: String? = null,
    val port: Int? = null
)

enum class CaptureState {
    STOPPED, RUNNING, PAUSED
}

enum class PacketFilter(
    val displayName: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    ALL("All", Icons.Default.AllInclusive),
    TCP("TCP", Icons.Default.Lan),
    UDP("UDP", Icons.Default.Wifi),
    ICMP("ICMP", Icons.Default.NetworkPing),
    HTTP("HTTP", Icons.Default.Http)
}

// Utility functions
private fun formatTime(date: Date): String {
    val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    return formatter.format(date)
}

private fun matchesFilter(packet: NetworkPacket, filter: PacketFilter): Boolean {
    return when (filter) {
        PacketFilter.ALL -> true
        PacketFilter.TCP -> packet.protocol == "TCP"
        PacketFilter.UDP -> packet.protocol == "UDP"
        PacketFilter.ICMP -> packet.protocol == "ICMP"
        PacketFilter.HTTP -> packet.info.contains("HTTP") || packet.destination.contains(":80") || packet.destination.contains(":443")
    }
}

private fun updateStats(stats: CaptureStats, packet: NetworkPacket): CaptureStats {
    val totalPackets = stats.totalPackets + 1
    val tcpCount = if (packet.protocol == "TCP") 1 else 0
    val udpCount = if (packet.protocol == "UDP") 1 else 0
    val otherCount = if (packet.protocol !in listOf("TCP", "UDP")) 1 else 0

    return stats.copy(
        totalPackets = totalPackets,
        packetsPerSecond = (totalPackets / 10).coerceAtLeast(1), // Simplified calculation
        tcpPercent = (tcpCount * 100 / totalPackets.coerceAtLeast(1)),
        udpPercent = (udpCount * 100 / totalPackets.coerceAtLeast(1)),
        otherPercent = (otherCount * 100 / totalPackets.coerceAtLeast(1))
    )
}

private fun simulatePacketCapture(onPacketCaptured: (NetworkPacket) -> Unit) {
    // This would be replaced with actual packet capture implementation
    // For now, we'll simulate some network packets
    val protocols = listOf("TCP", "UDP", "ICMP")
    val sources = listOf("192.168.1.100", "10.0.0.2", "172.16.1.50")
    val destinations = listOf("8.8.8.8:53", "1.1.1.1:443", "github.com:80", "google.com:443")

    // Simulate packet generation (in real app, this would come from packet capture library)
}

private fun getSamplePackets(): List<NetworkPacket> {
    return listOf(
        NetworkPacket(
            id = "1",
            timestamp = Date(),
            source = "192.168.1.100:51234",
            destination = "8.8.8.8:53",
            protocol = "UDP",
            length = 78,
            info = "DNS Query google.com"
        ),
        NetworkPacket(
            id = "2",
            timestamp = Date(System.currentTimeMillis() + 100),
            source = "8.8.8.8:53",
            destination = "192.168.1.100:51234",
            protocol = "UDP",
            length = 142,
            info = "DNS Response google.com"
        ),
        NetworkPacket(
            id = "3",
            timestamp = Date(System.currentTimeMillis() + 200),
            source = "192.168.1.100:51235",
            destination = "142.251.32.46:443",
            protocol = "TCP",
            length = 66,
            info = "TCP SYN"
        )
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewPacketCaptureScreen() {
    MaterialTheme {
        PacketCaptureScreen()
    }
}
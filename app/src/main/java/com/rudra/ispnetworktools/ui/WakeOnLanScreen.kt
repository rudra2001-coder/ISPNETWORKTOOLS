package com.rudra.ispnetworktools.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WakeOnLanScreen() {
    var devices by remember { mutableStateOf(emptyList<WoLDevice>()) }
    var showAddDeviceDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Load saved devices
    LaunchedEffect(Unit) {
        devices = getSampleDevices()
    }

    val filteredDevices = devices.filter { device ->
        device.name.contains(searchQuery, ignoreCase = true) ||
                device.macAddress.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Wake on LAN",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                actions = {
                    IconButton(onClick = { showAddDeviceDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add device")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDeviceDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("Add Device") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search devices...") },
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

            Spacer(modifier = Modifier.height(16.dp))

            when {
                filteredDevices.isEmpty() && searchQuery.isNotEmpty() -> {
                    EmptySearchState(searchQuery = searchQuery)
                }

                devices.isEmpty() -> {
                    EmptyDevicesState(onAddDevice = { showAddDeviceDialog = true })
                }

                else -> {
                    DevicesList(
                        devices = filteredDevices,
                        onWakeDevice = { device ->
                            // Simulate Wake-on-LAN packet sending
                            sendWakeOnLanPacket(device)
                        },
                        onEditDevice = { device ->
                            // Edit device logic
                        },
                        onDeleteDevice = { device ->
                            devices = devices.filter { it.id != device.id }
                        }
                    )
                }
            }
        }
    }

    if (showAddDeviceDialog) {
        AddDeviceDialog(
            onDismiss = { showAddDeviceDialog = false },
            onSave = { newDevice ->
                devices = devices + newDevice
                showAddDeviceDialog = false
            }
        )
    }
}

@Composable
private fun DevicesList(
    devices: List<WoLDevice>,
    onWakeDevice: (WoLDevice) -> Unit,
    onEditDevice: (WoLDevice) -> Unit,
    onDeleteDevice: (WoLDevice) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(devices, key = { it.id }) { device ->
            DeviceCard(
                device = device,
                onWake = { onWakeDevice(device) },
                onEdit = { onEditDevice(device) },
                onDelete = { onDeleteDevice(device) }
            )
        }
    }
}

@Composable
private fun DeviceCard(
    device: WoLDevice,
    onWake: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (device.type) {
                        DeviceType.COMPUTER -> Icons.Default.Computer
                        DeviceType.SERVER -> Icons.Default.Storage
                        DeviceType.GAME_CONSOLE -> Icons.Default.VideogameAsset
                        DeviceType.OTHER -> Icons.Default.DeviceUnknown
                    },
                    contentDescription = device.type.displayName,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = device.macAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Status indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (device.lastSeen != null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                            shape = MaterialTheme.shapes.small
                        )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Device details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = device.type.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (device.lastSeen != null) {
                    Text(
                        text = "Last seen: ${device.lastSeen}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (device.broadcastAddress != null) {
                Text(
                    text = "Broadcast: ${device.broadcastAddress}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(onClick = { showDeleteConfirm = true }) {
                    Text("Delete")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = onWake) {
                    Icon(Icons.Default.Power, contentDescription = "Wake", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Wake Up")
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Device") },
            text = { Text("Are you sure you want to delete ${device.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDeviceDialog(
    onDismiss: () -> Unit,
    onSave: (WoLDevice) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var macAddress by remember { mutableStateOf("") }
    var broadcastAddress by remember { mutableStateOf("255.255.255.255") }
    var deviceType by remember { mutableStateOf(DeviceType.COMPUTER) }
    var port by remember { mutableStateOf("9") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Device") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Device Name") },
                    placeholder = { Text("e.g., My Desktop PC") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = macAddress,
                    onValueChange = { macAddress = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("MAC Address") },
                    placeholder = { Text("e.g., 00:11:22:33:44:55") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                )

                OutlinedTextField(
                    value = broadcastAddress,
                    onValueChange = { broadcastAddress = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Broadcast Address") },
                    placeholder = { Text("e.g., 255.255.255.255") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                )

                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Port") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Device type selector
                Text(
                    text = "Device Type",
                    style = MaterialTheme.typography.labelMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DeviceType.entries.forEach { type ->
                        FilterChip(
                            selected = deviceType == type,
                            onClick = { deviceType = type },
                            label = { Text(type.displayName) },
                            leadingIcon = {
                                Icon(
                                    imageVector = type.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && isValidMacAddress(macAddress)) {
                        val newDevice = WoLDevice(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            macAddress = macAddress.uppercase(),
                            broadcastAddress = broadcastAddress,
                            port = port.toIntOrNull() ?: 9,
                            type = deviceType
                        )
                        onSave(newDevice)
                    }
                },
                enabled = name.isNotBlank() && isValidMacAddress(macAddress)
            ) {
                Text("Save Device")
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
private fun EmptyDevicesState(onAddDevice: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Devices,
            contentDescription = "No devices",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Devices Added",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add your first device to use Wake-on-LAN",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddDevice) {
            Text("Add Your First Device")
        }
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
            text = "No Devices Found",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No devices match \"$searchQuery\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Data classes and enums
data class WoLDevice(
    val id: String,
    val name: String,
    val macAddress: String,
    val broadcastAddress: String? = "255.255.255.255",
    val port: Int = 9,
    val type: DeviceType = DeviceType.COMPUTER,
    val lastSeen: String? = null
)

enum class DeviceType(
    val displayName: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    COMPUTER("Computer", Icons.Default.Computer),
    SERVER("Server", Icons.Default.Storage),
    GAME_CONSOLE("Game Console", Icons.Default.VideogameAsset),
    OTHER("Other", Icons.Default.DeviceUnknown)
}

// Utility functions
private fun isValidMacAddress(mac: String): Boolean {
    val macRegex = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$".toRegex()
    return macRegex.matches(mac)
}

private fun sendWakeOnLanPacket(device: WoLDevice) {
    // This would contain the actual Wake-on-LAN implementation
    // For now, we'll just simulate it
    println("Sending Wake-on-LAN packet to ${device.macAddress}")
}

private fun getSampleDevices(): List<WoLDevice> {
    return listOf(
        WoLDevice(
            id = "1",
            name = "Desktop PC",
            macAddress = "00:11:22:33:44:55",
            type = DeviceType.COMPUTER,
            lastSeen = "2 hours ago"
        ),
        WoLDevice(
            id = "2",
            name = "Home Server",
            macAddress = "AA:BB:CC:DD:EE:FF",
            type = DeviceType.SERVER,
            lastSeen = "5 minutes ago"
        ),
        WoLDevice(
            id = "3",
            name = "PlayStation 5",
            macAddress = "11:22:33:44:55:66",
            type = DeviceType.GAME_CONSOLE,
            lastSeen = "1 day ago"
        )
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewWakeOnLanScreen() {
    MaterialTheme {
        WakeOnLanScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWakeOnLanScreenWithDevices() {
    MaterialTheme {
        WakeOnLanScreen()
    }
}

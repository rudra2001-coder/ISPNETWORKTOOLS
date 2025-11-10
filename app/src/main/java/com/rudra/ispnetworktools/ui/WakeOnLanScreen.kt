package com.rudra.ispnetworktools.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WakeOnLanScreen() {
    var devices by remember { mutableStateOf(emptyList<WoLDevice>()) }
    var showAddDeviceDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        devices = getSampleDevices()
    }

    val filteredDevices = devices.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.macAddress.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Wake on LAN") },
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
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                placeholder = { Text("Search devices...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                            sendWakeOnLanPacket(device)
                            scope.launch {
                                snackbarHostState.showSnackbar("Magic packet sent to ${device.name}")
                            }
                        },
                        onEditDevice = { /* TODO: Implement edit functionality */ },
                        onDeleteDevice = { device ->
                            devices = devices.filter { it.id != device.id }
                            scope.launch {
                                snackbarHostState.showSnackbar("${device.name} deleted")
                            }
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
                scope.launch {
                    snackbarHostState.showSnackbar("${newDevice.name} saved")
                }
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
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
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = device.type.icon,
                contentDescription = device.type.displayName,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

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
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
                device.lastSeen?.let {
                    Text(
                        text = "Last seen: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(onClick = onWake, modifier = Modifier.padding(horizontal = 8.dp)) {
                Icon(Icons.Default.Power, contentDescription = "Wake")
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = {
                        onEdit()
                        showMenu = false
                    }, leadingIcon = { Icon(Icons.Default.Edit, null) })
                    DropdownMenuItem(text = { Text("Delete") }, onClick = {
                        showDeleteConfirm = true
                        showMenu = false
                    }, leadingIcon = { Icon(Icons.Default.Delete, null) })
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
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    placeholder = { Text("00:11:22:33:44:55") },
                    singleLine = true,
                )

                OutlinedTextField(
                    value = broadcastAddress,
                    onValueChange = { broadcastAddress = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Broadcast Address (Optional)") },
                    singleLine = true,
                )

                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Port (Optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Text("Device Type", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DeviceType.entries.forEach { type ->
                        FilterChip(
                            selected = deviceType == type,
                            onClick = { deviceType = type },
                            label = { Text(type.displayName) },
                            leadingIcon = { Icon(type.icon, null, Modifier.size(18.dp)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && isValidMacAddress(macAddress)) {
                        onSave(WoLDevice(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            macAddress = macAddress.uppercase(),
                            broadcastAddress = broadcastAddress.ifBlank { "255.255.255.255" },
                            port = port.toIntOrNull() ?: 9,
                            type = deviceType
                        ))
                    }
                },
                enabled = name.isNotBlank() && isValidMacAddress(macAddress)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EmptyDevicesState(onAddDevice: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
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
        Text("No Devices Added", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Add your first device to send Wake-on-LAN packets.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddDevice) { Text("Add Your First Device") }
    }
}

@Composable
private fun EmptySearchState(searchQuery: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
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
        Text("No Devices Found", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "No devices match \"$searchQuery\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

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

private fun isValidMacAddress(mac: String): Boolean {
    val macRegex = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$".toRegex()
    return macRegex.matches(mac)
}

private fun sendWakeOnLanPacket(device: WoLDevice) {
    // This would contain the actual Wake-on-LAN implementation
    println("Sending Wake-on-LAN packet to ${device.macAddress} on port ${device.port}")
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

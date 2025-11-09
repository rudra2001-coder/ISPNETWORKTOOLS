package com.rudra.ispnetworktools.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestHistoryScreen() {
    var selectedTestType by remember { mutableStateOf(TestType.ALL) }
    var sortOrder by remember { mutableStateOf(SortOrder.NEWEST_FIRST) }
    var searchQuery by remember { mutableStateOf("") }

    val testHistory by remember { mutableStateOf(getSampleTestHistory()) }

    val filteredHistory = testHistory
        .filter { test ->
            (selectedTestType == TestType.ALL || test.type == selectedTestType) &&
                    (searchQuery.isEmpty() ||
                            test.name.contains(searchQuery, ignoreCase = true) ||
                            test.target.contains(searchQuery, ignoreCase = true))
        }
        .sortedWith(
            when (sortOrder) {
                SortOrder.NEWEST_FIRST -> compareByDescending { it.timestamp }
                SortOrder.OLDEST_FIRST -> compareBy { it.timestamp }
                SortOrder.FASTEST_FIRST -> compareBy { it.result?.speed ?: 0.0 }
                SortOrder.SLOWEST_FIRST -> compareByDescending { it.result?.speed ?: 0.0 }
            }
        )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Test History",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                actions = {
                    IconButton(onClick = { /* Export functionality */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Export")
                    }
                    IconButton(onClick = { /* Clear history */ }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear History")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search and filters
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search tests...") },
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

                Spacer(modifier = Modifier.height(12.dp))

                // Test type filter
                Text(
                    text = "Test Type",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TestType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedTestType == type,
                            onClick = { selectedTestType = type },
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

                Spacer(modifier = Modifier.height(12.dp))

                // Sort order
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sort by:",
                        style = MaterialTheme.typography.labelMedium
                    )

                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = sortOrder.displayName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .width(160.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            SortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = { Text(order.displayName) },
                                    onClick = {
                                        sortOrder = order
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Results summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HistoryStat(
                        value = filteredHistory.size.toString(),
                        label = "Total Tests"
                    )
                    HistoryStat(
                        value = filteredHistory.count { it.result?.success == true }.toString(),
                        label = "Successful"
                    )
                    HistoryStat(
                        value = String.format("%.1f", filteredHistory.mapNotNull { it.result?.speed }.average()),
                        label = "Avg Speed (Mbps)"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // History list
            when {
                filteredHistory.isEmpty() && searchQuery.isNotEmpty() -> {
                    EmptySearchState(searchQuery = searchQuery)
                }

                testHistory.isEmpty() -> {
                    EmptyHistoryState()
                }

                else -> {
                    TestHistoryList(
                        tests = filteredHistory,
                        onTestClick = { test ->
                            // Navigate to test details
                        },
                        onDeleteTest = { test ->
                            // Delete test from history
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TestHistoryList(
    tests: List<NetworkTest>,
    onTestClick: (NetworkTest) -> Unit,
    onDeleteTest: (NetworkTest) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(tests, key = { it.id }) { test ->
            TestHistoryItem(
                test = test,
                onClick = { onTestClick(test) },
                onDelete = { onDeleteTest(test) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TestHistoryItem(
    test: NetworkTest,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
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
                    imageVector = test.type.icon,
                    contentDescription = test.type.displayName,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = test.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatDate(test.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            color = when {
                                test.result?.success == true -> MaterialTheme.colorScheme.primary
                                test.result?.success == false -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.outline
                            }
                        )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Test details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = test.target,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = test.duration,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Results row
            test.result?.let { result ->
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (test.type) {
                        TestType.SPEED_TEST -> {
                            SpeedTestResult(result)
                        }
                        TestType.PING -> {
                            PingTestResult(result)
                        }
                        TestType.TRACEROUTE -> {
                            TracerouteTestResult(result)
                        }
                        TestType.WAKE_ON_LAN -> {
                            WoLTestResult(result)
                        }
                        else -> {
                            BasicTestResult(result)
                        }
                    }

                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete test",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Test") },
            text = { Text("Are you sure you want to delete this test from history?") },
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

@Composable
private fun SpeedTestResult(result: TestResult) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(
                text = String.format("%.1f Mbps", result.speed ?: 0.0),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${result.packetLoss ?: 0}% loss",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PingTestResult(result: TestResult) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(
                text = "${result.latency ?: 0} ms",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${result.packetsSent ?: 0}/${result.packetsReceived ?: 0} packets",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TracerouteTestResult(result: TestResult) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(
                text = "${result.hops ?: 0} hops",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${result.packetLoss ?: 0}% loss",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WoLTestResult(result: TestResult) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = if (result.success == true) "Sent successfully" else "Failed",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (result.success == true) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun BasicTestResult(result: TestResult) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = if (result.success == true) "Success" else "Failed",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (result.success == true) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun HistoryStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyHistoryState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = "No history",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Test History",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your network tests will appear here once you run them",
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
            text = "No Tests Found",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No tests match \"$searchQuery\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Data classes and enums
data class NetworkTest(
    val id: String,
    val name: String,
    val type: TestType,
    val target: String,
    val timestamp: Date,
    val duration: String,
    val result: TestResult?
)

data class TestResult(
    val success: Boolean? = null,
    val speed: Double? = null, // Mbps
    val latency: Long? = null, // ms
    val packetLoss: Int? = null, // percentage
    val packetsSent: Int? = null,
    val packetsReceived: Int? = null,
    val hops: Int? = null,
    val errorMessage: String? = null
)

enum class TestType(
    val displayName: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    ALL("All Tests", Icons.Default.History),
    SPEED_TEST("Speed Test", Icons.Default.Speed),
    PING("Ping", Icons.Default.NetworkPing),
    TRACEROUTE("Traceroute", Icons.Default.Route),
    WAKE_ON_LAN("Wake on LAN", Icons.Default.Power),
    PORT_SCAN("Port Scan", Icons.Default.Scanner) // You'd need to add this icon
}

enum class SortOrder(val displayName: String) {
    NEWEST_FIRST("Newest First"),
    OLDEST_FIRST("Oldest First"),
    FASTEST_FIRST("Fastest First"),
    SLOWEST_FIRST("Slowest First")
}

// Utility functions
private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return formatter.format(date)
}

private fun getSampleTestHistory(): List<NetworkTest> {
    return listOf(
        NetworkTest(
            id = "1",
            name = "Internet Speed Test",
            type = TestType.SPEED_TEST,
            target = "Internet",
            timestamp = Date(System.currentTimeMillis() - 3600000),
            duration = "30s",
            result = TestResult(success = true, speed = 85.4, packetLoss = 0)
        ),
        NetworkTest(
            id = "2",
            name = "Google Ping",
            type = TestType.PING,
            target = "google.com",
            timestamp = Date(System.currentTimeMillis() - 7200000),
            duration = "10s",
            result = TestResult(success = true, latency = 24, packetsSent = 10, packetsReceived = 10)
        ),
        NetworkTest(
            id = "3",
            name = "Route to GitHub",
            type = TestType.TRACEROUTE,
            target = "github.com",
            timestamp = Date(System.currentTimeMillis() - 10800000),
            duration = "45s",
            result = TestResult(success = true, hops = 12, packetLoss = 0)
        ),
        NetworkTest(
            id = "4",
            name = "Wake Desktop PC",
            type = TestType.WAKE_ON_LAN,
            target = "00:11:22:33:44:55",
            timestamp = Date(System.currentTimeMillis() - 14400000),
            duration = "2s",
            result = TestResult(success = true)
        )
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewTestHistoryScreen() {
    MaterialTheme {
        TestHistoryScreen()
    }
}
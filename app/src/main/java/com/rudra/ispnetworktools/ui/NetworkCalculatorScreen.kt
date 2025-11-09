package com.rudra.ispnetworktools.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Schema
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkCalculatorScreen() {
    var selectedTab by remember { mutableStateOf(CalculatorTab.SUBNET) }
    var ipAddress by remember { mutableStateOf("192.168.1.0") }
    var subnetMask by remember { mutableStateOf("255.255.255.0") }
    var cidrNotation by remember { mutableIntStateOf(24) }
    var calculationResult by remember { mutableStateOf<NetworkCalculation?>(null) }

    LaunchedEffect(ipAddress, subnetMask, cidrNotation, selectedTab) {
        calculationResult = calculateNetwork(ipAddress, subnetMask, cidrNotation)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Network Calculator",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab selection
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                CalculatorTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.displayName) },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.displayName
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                CalculatorTab.SUBNET -> {
                    SubnetCalculatorContent(
                        ipAddress = ipAddress,
                        subnetMask = subnetMask,
                        cidrNotation = cidrNotation,
                        onIpAddressChange = { ipAddress = it },
                        onSubnetMaskChange = { subnetMask = it },
                        onCidrChange = { cidrNotation = it },
                        result = calculationResult
                    )
                }

                CalculatorTab.CIDR -> {
                    CidrCalculatorContent(
                        onCidrChange = { cidrNotation = it },
                        result = calculationResult
                    )
                }

                CalculatorTab.VLSM -> {
                    VlsmCalculatorContent()
                }

                CalculatorTab.CONVERTER -> {
                    ConverterContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SubnetCalculatorContent(
    ipAddress: String,
    subnetMask: String,
    cidrNotation: Int,
    onIpAddressChange: (String) -> Unit,
    onSubnetMaskChange: (String) -> Unit,
    onCidrChange: (Int) -> Unit,
    result: NetworkCalculation?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Input section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Network Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // IP Address input
                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { if (isValidIpAddress(it)) onIpAddressChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("IP Address") },
                    placeholder = { Text("192.168.1.0") },
                    leadingIcon = {
                        Icon(Icons.Default.Dns, contentDescription = "IP Address")
                    },
                    singleLine = true,
                    isError = !isValidIpAddress(ipAddress)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subnet Mask input
                OutlinedTextField(
                    value = subnetMask,
                    onValueChange = { if (isValidSubnetMask(it)) onSubnetMaskChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Subnet Mask") },
                    placeholder = { Text("255.255.255.0") },
                    leadingIcon = {
                        Icon(Icons.Default.Security, contentDescription = "Subnet Mask")
                    },
                    singleLine = true,
                    isError = !isValidSubnetMask(subnetMask)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // CIDR Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("CIDR Notation: /$cidrNotation")
                        Text("${getUsableHosts(cidrNotation)} usable hosts")
                    }
                    Slider(
                        value = cidrNotation.toFloat(),
                        onValueChange = { onCidrChange(it.toInt()) },
                        valueRange = 8f..30f,
                        steps = 22
                    )
                }
            }
        }

        // Results section
        result?.let {
            NetworkResultsCard(result = it)
        }

        // Quick examples
        QuickExamplesCard(
            onExampleSelected = { example ->
                onIpAddressChange(example.ip)
                onSubnetMaskChange(example.subnet)
                onCidrChange(example.cidr)
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CidrCalculatorContent(
    onCidrChange: (Int) -> Unit,
    result: NetworkCalculation?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "CIDR Calculator",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // CIDR notation input
                var cidrInput by remember { mutableStateOf("24") }

                OutlinedTextField(
                    value = cidrInput,
                    onValueChange = {
                        cidrInput = it
                        it.toIntOrNull()?.takeIf { num -> num in 0..32 }?.let { num ->
                            onCidrChange(num)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("CIDR Notation") },
                    placeholder = { Text("24") },
                    leadingIcon = {
                        Icon(Icons.Default.Numbers, contentDescription = "CIDR")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // CIDR quick selection
                Text(
                    text = "Common CIDR Values",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(8, 16, 24, 26, 28, 30, 32).forEach { cidr ->
                        FilterChip(
                            selected = cidrInput == cidr.toString(),
                            onClick = {
                                cidrInput = cidr.toString()
                                onCidrChange(cidr)
                            },
                            label = { Text("/$cidr") }
                        )
                    }
                }
            }
        }

        result?.let {
            CidrResultsCard(result = it)
        }
    }
}

@Composable
private fun VlsmCalculatorContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "VLSM Calculator",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Variable Length Subnet Masking allows efficient IP address allocation by creating subnets of different sizes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* TODO: Implement VLSM calculator */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = "Calculate")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open VLSM Calculator")
                }
            }
        }
    }
}

@Composable
private fun ConverterContent() {
    var binaryInput by remember { mutableStateOf("11000000.10101000.00000001.00000000") }
    var decimalInput by remember { mutableStateOf("192.168.1.0") }
    var hexInput by remember { mutableStateOf("C0.A8.01.00") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "IP Address Converter",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Decimal (standard) IP
                OutlinedTextField(
                    value = decimalInput,
                    onValueChange = { decimalInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Decimal IP") },
                    placeholder = { Text("192.168.1.0") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Binary IP
                OutlinedTextField(
                    value = binaryInput,
                    onValueChange = { binaryInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Binary IP") },
                    placeholder = { Text("11000000.10101000.00000001.00000000") },
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Hexadecimal IP
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { hexInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Hexadecimal IP") },
                    placeholder = { Text("C0.A8.01.00") },
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* Convert decimal to others */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Convert")
                    }
                    Button(
                        onClick = {
                            decimalInput = ""
                            binaryInput = ""
                            hexInput = ""
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text("Clear")
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkResultsCard(result: NetworkCalculation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Calculation Results",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Network information in a grid
            ResultGridItem(label = "Network Address", value = result.networkAddress)
            ResultGridItem(label = "Broadcast Address", value = result.broadcastAddress)
            ResultGridItem(label = "First Usable IP", value = result.firstUsableIp)
            ResultGridItem(label = "Last Usable IP", value = result.lastUsableIp)
            ResultGridItem(label = "Subnet Mask", value = result.subnetMask)
            ResultGridItem(label = "Wildcard Mask", value = result.wildcardMask)
            ResultGridItem(label = "CIDR Notation", value = "/${result.cidr}")
            ResultGridItem(label = "Total Hosts", value = result.totalHosts.toString())
            ResultGridItem(label = "Usable Hosts", value = result.usableHosts.toString())
            ResultGridItem(label = "IP Class", value = result.ipClass)

            Spacer(modifier = Modifier.height(8.dp))

            // IP range
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "IP Range",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${result.firstUsableIp} - ${result.lastUsableIp}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun CidrResultsCard(result: NetworkCalculation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "CIDR Results - /${result.cidr}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            ResultGridItem(label = "Subnet Mask", value = result.subnetMask)
            ResultGridItem(label = "Total IPs", value = result.totalHosts.toString())
            ResultGridItem(label = "Usable IPs", value = result.usableHosts.toString())
            ResultGridItem(label = "Network Bits", value = result.cidr.toString())
            ResultGridItem(label = "Host Bits", value = (32 - result.cidr).toString())

            Spacer(modifier = Modifier.height(8.dp))

            // Binary representation
            Text(
                text = "Binary: ${result.subnetMask.split(".").joinToString(".") { it.toInt().toString(2).padStart(8, '0') }}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun QuickExamplesCard(onExampleSelected: (NetworkExample) -> Unit) {
    val examples = listOf(
        NetworkExample("Class C Default", "192.168.1.0", "255.255.255.0", 24),
        NetworkExample("Class B Default", "172.16.0.0", "255.255.0.0", 16),
        NetworkExample("Class A Default", "10.0.0.0", "255.0.0.0", 8),
        NetworkExample("Small Subnet", "192.168.1.0", "255.255.255.252", 30),
        NetworkExample("Medium Subnet", "192.168.1.0", "255.255.255.128", 25)
    )

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quick Examples",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                examples.forEach { example ->
                    SuggestionChip(
                        onClick = { onExampleSelected(example) },
                        label = { Text(example.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun ResultGridItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    Divider(modifier = Modifier.padding(vertical = 8.dp))
}

data class NetworkExample(
    val name: String,
    val ip: String,
    val subnet: String,
    val cidr: Int
)

enum class CalculatorTab(val displayName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    SUBNET("Subnet", Icons.Default.NetworkCell),
    CIDR("CIDR", Icons.Default.Numbers),
    VLSM("VLSM", Icons.Default.Schema),
    CONVERTER("Converter", Icons.Default.Transform)
}

data class NetworkCalculation(
    val networkAddress: String,
    val broadcastAddress: String,
    val firstUsableIp: String,
    val lastUsableIp: String,
    val subnetMask: String,
    val wildcardMask: String,
    val cidr: Int,
    val totalHosts: Long,
    val usableHosts: Long,
    val ipClass: String
)

fun isValidIpAddress(ip: String): Boolean {
    val ipRegex = Regex("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")
    return ipRegex.matches(ip)
}

fun isValidSubnetMask(mask: String): Boolean {
    if (!isValidIpAddress(mask)) return false
    val parts = mask.split(".").map { it.toInt() }
    val binaryString = parts.joinToString("") { it.toString(2).padStart(8, '0') }
    return "01" !in binaryString
}

fun getUsableHosts(cidr: Int): Long {
    return if (cidr in 0..30) {
        2.0.pow(32 - cidr).toLong() - 2
    } else {
        0
    }
}

fun calculateNetwork(ipAddress: String, subnetMask: String, cidrNotation: Int): NetworkCalculation? {
    if (!isValidIpAddress(ipAddress) || !isValidSubnetMask(subnetMask)) {
        return null
    }

    val ipParts = ipAddress.split(".").map { it.toLong() }
    val maskParts = subnetMask.split(".").map { it.toLong() }

    val networkAddressParts = ipParts.zip(maskParts).map { (ip, mask) -> ip and mask }
    val networkAddress = networkAddressParts.joinToString(".")

    val wildcardMaskParts = maskParts.map { 255 - it }
    val wildcardMask = wildcardMaskParts.joinToString(".")

    val broadcastAddressParts = networkAddressParts.zip(wildcardMaskParts).map { (net, wild) -> net or wild }
    val broadcastAddress = broadcastAddressParts.joinToString(".")

    val firstUsableIpParts = networkAddressParts.toMutableList()
    if (cidrNotation < 31) {
        firstUsableIpParts[3]++
    }
    val firstUsableIp = firstUsableIpParts.joinToString(".")

    val lastUsableIpParts = broadcastAddressParts.toMutableList()
    if (cidrNotation < 31) {
        lastUsableIpParts[3]--
    }
    val lastUsableIp = lastUsableIpParts.joinToString(".")

    val totalHosts = 2.0.pow(32 - cidrNotation).toLong()
    val usableHosts = if (totalHosts >= 2) totalHosts - 2 else 0

    val ipClass = when {
        ipParts[0] in 1..126 -> "A"
        ipParts[0] in 128..191 -> "B"
        ipParts[0] in 192..223 -> "C"
        ipParts[0] in 224..239 -> "D (Multicast)"
        else -> "E (Reserved)"
    }

    return NetworkCalculation(
        networkAddress = networkAddress,
        broadcastAddress = broadcastAddress,
        firstUsableIp = firstUsableIp,
        lastUsableIp = lastUsableIp,
        subnetMask = subnetMask,
        wildcardMask = wildcardMask,
        cidr = cidrNotation,
        totalHosts = totalHosts,
        usableHosts = usableHosts,
        ipClass = ipClass
    )
}

@Preview(showBackground = true)
@Composable
fun NetworkCalculatorScreenPreview() {
    NetworkCalculatorScreen()
}

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Schema
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkCalculatorScreen() {
    var selectedTab by remember { mutableStateOf(CalculatorTab.SUBNET) }
    var ipAddress by remember { mutableStateOf("192.168.1.0") }
    var subnetMask by remember { mutableStateOf("255.255.255.0") }
    val cidr by remember { derivedStateOf { subnetMaskToCidr(subnetMask) } }

    val calculationResult by remember {
        derivedStateOf {
            calculateNetwork(ipAddress, subnetMask, cidr)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Network Calculator") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                CalculatorTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.displayName) },
                        icon = { Icon(tab.icon, contentDescription = tab.displayName) }
                    )
                }
            }

            Column(Modifier.verticalScroll(rememberScrollState())) {
                when (selectedTab) {
                    CalculatorTab.SUBNET -> SubnetCalculatorContent(
                        ipAddress = ipAddress,
                        subnetMask = subnetMask,
                        onIpAddressChange = { ipAddress = it },
                        onSubnetMaskChange = { subnetMask = it },
                        result = calculationResult
                    )
                    CalculatorTab.CIDR -> CidrCalculatorContent(result = calculationResult)
                    CalculatorTab.CONVERTER -> ConverterContent()
                    CalculatorTab.VLSM -> VlsmCalculatorContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SubnetCalculatorContent(
    ipAddress: String,
    subnetMask: String,
    onIpAddressChange: (String) -> Unit,
    onSubnetMaskChange: (String) -> Unit,
    result: NetworkCalculation?
) {
    val isIpValid by remember(ipAddress) { mutableStateOf(isValidIpAddress(ipAddress)) }
    val isSubnetValid by remember(subnetMask) { mutableStateOf(isValidSubnetMask(subnetMask)) }
    val cidr = subnetMaskToCidr(subnetMask)

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Network Information", style = MaterialTheme. typography.titleLarge)
                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = onIpAddressChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("IP Address") },
                    leadingIcon = { Icon(Icons.Default.Dns, null) },
                    isError = !isIpValid,
                    singleLine = true
                )
                OutlinedTextField(
                    value = subnetMask,
                    onValueChange = onSubnetMaskChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Subnet Mask") },
                    leadingIcon = { Icon(Icons.Default.Security, null) },
                    isError = !isSubnetValid,
                    singleLine = true
                )
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("CIDR: /$cidr")
                        Text("${result?.usableHosts ?: 0} usable hosts")
                    }
                    Slider(
                        value = cidr.toFloat(),
                        onValueChange = { onSubnetMaskChange(cidrToSubnetMask(it.toInt())) },
                        valueRange = 1f..32f,
                        steps = 30
                    )
                }
            }
        }

        result?.let { NetworkResultsCard(result = it) }

        QuickExamplesCard {
            onIpAddressChange(it.ip)
            onSubnetMaskChange(it.subnet)
        }
    }
}

@Composable
private fun CidrCalculatorContent(result: NetworkCalculation?) {
    result?.let {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CidrResultsCard(result = it)
        }
    }
}

@Composable
private fun ConverterContent() {
    var decimalIp by remember { mutableStateOf("192.168.1.1") }

    val conversionResult by remember(decimalIp) {
        derivedStateOf {
            if (isValidIpAddress(decimalIp)) {
                val parts = decimalIp.split(".").map { it.toInt() }
                val binary = parts.joinToString(".") { it.toString(2).padStart(8, '0') }
                val hex = parts.joinToString(".") { it.toString(16).uppercase().padStart(2, '0') }
                Triple(decimalIp, binary, hex)
            } else {
                null
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), Arrangement.spacedBy(12.dp)) {
                Text("IP Address Converter", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = decimalIp,
                    onValueChange = { decimalIp = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Enter Decimal IP") },
                    isError = conversionResult == null,
                    singleLine = true
                )
            }
        }

        conversionResult?.let {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), Arrangement.spacedBy(12.dp)) {
                    Text("Conversion Results", style = MaterialTheme.typography.titleMedium)
                    ConversionResultRow("Binary", it.second)
                    ConversionResultRow("Hexadecimal", it.third)
                }
            }
        }
    }
}

@Composable
private fun VlsmCalculatorContent() {
    var majorNetwork by remember { mutableStateOf("192.168.0.0/24") }
    var subnetRequests by remember { mutableStateOf(listOf(VlsmSubnetRequest(UUID.randomUUID().toString(), "Sales", 50))) }
    var vlsmResult by remember { mutableStateOf<List<NetworkCalculation>?>(null) }
    var vlsmError by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), Arrangement.spacedBy(12.dp)) {
                Text("VLSM Calculator", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = majorNetwork,
                    onValueChange = { majorNetwork = it },
                    label = { Text("Major Network (e.g., 192.168.0.0/24)") },
                    modifier = Modifier.fillMaxWidth()
                )
                subnetRequests.forEachIndexed { index, req ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = req.name,
                            onValueChange = { newName ->
                                val newList = subnetRequests.toMutableList()
                                newList[index] = req.copy(name = newName)
                                subnetRequests = newList
                            },
                            label = { Text("Subnet Name") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = req.hosts.toString(),
                            onValueChange = { newHosts ->
                                val newList = subnetRequests.toMutableList()
                                newList[index] = req.copy(hosts = newHosts.toIntOrNull() ?: 0)
                                subnetRequests = newList
                            },
                            label = { Text("Hosts") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.5f).padding(start = 8.dp)
                        )
                        IconButton(onClick = {
                            subnetRequests = subnetRequests.filter { it.id != req.id }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Subnet")
                        }
                    }
                }
                Button(onClick = { subnetRequests = subnetRequests + VlsmSubnetRequest(UUID.randomUUID().toString(), "", 0) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Subnet")
                    Text("Add Subnet")
                }
            }
        }

        Button(onClick = { 
            vlsmError = null
            val result = calculateVlsm(majorNetwork, subnetRequests)
            if (result == null) {
                vlsmError = "Invalid Major Network. Please use CIDR notation (e.g., 192.168.0.0/24)."
            }
            vlsmResult = result
        }) {
            Text("Calculate VLSM")
        }

        vlsmError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        vlsmResult?.let {
            Text("VLSM Results", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
            it.forEach { result ->
                NetworkResultsCard(result = result)
            }
        }
    }
}

@Composable
private fun ConversionResultRow(label: String, value: String) {
    Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    Text(value, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyLarge)
    Divider()
}

@Composable
private fun NetworkResultsCard(result: NetworkCalculation) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            val title = result.name ?: result.networkAddress
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (result.name != null) {
                Text(result.networkAddress, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
            }
            Spacer(Modifier.height(16.dp))
            ResultGridItem("Broadcast Address", result.broadcastAddress)
            ResultGridItem("IP Range", "${result.firstUsableIp} - ${result.lastUsableIp}")
            ResultGridItem("Usable Hosts", result.usableHosts.toString())
            ResultGridItem("Subnet Mask", result.subnetMask)
        }
    }
}

@Composable
private fun CidrResultsCard(result: NetworkCalculation) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("CIDR /${result.cidr} Results", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            ResultGridItem("Subnet Mask", result.subnetMask)
            ResultGridItem("Total IPs", result.totalHosts.toString())
            ResultGridItem("Usable IPs", result.usableHosts.toString())
            ResultGridItem("Network Bits", result.cidr.toString())
            ResultGridItem("Host Bits", (32 - result.cidr).toString())
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun QuickExamplesCard(onExampleSelected: (NetworkExample) -> Unit) {
    val examples = listOf(
        NetworkExample("Class C", "192.168.1.0", "255.255.255.0"),
        NetworkExample("Class B", "172.16.0.0", "255.255.0.0"),
        NetworkExample("Class A", "10.0.0.0", "255.0.0.0"),
        NetworkExample("Small Subnet", "192.168.10.0", "255.255.255.252"),
    )

    Column {
        Text("Quick Examples", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            examples.forEach { example ->
                SuggestionChip(onClick = { onExampleSelected(example) }, label = { Text(example.name) })
            }
        }
    }
}

@Composable
fun ResultGridItem(label: String, value: String) {
    Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(value, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.End)
    }
    Divider()
}

data class NetworkExample(val name: String, val ip: String, val subnet: String)

enum class CalculatorTab(val displayName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    SUBNET("Subnet", Icons.Default.NetworkCell),
    CIDR("CIDR", Icons.Default.Numbers),
    CONVERTER("Converter", Icons.Default.Transform),
    VLSM("VLSM", Icons.Default.Schema)
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
    val ipClass: String,
    val name: String? = null
)

data class VlsmSubnetRequest(
    val id: String,
    val name: String,
    val hosts: Int
)

private fun isValidIpAddress(ip: String): Boolean {
    val ipRegex = Regex("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")
    return ipRegex.matches(ip)
}

private fun isValidSubnetMask(mask: String): Boolean {
    if (!isValidIpAddress(mask)) return false
    val binaryString = mask.split(".").map { it.toInt().toString(2).padStart(8, '0') }.joinToString("")
    return "01" !in binaryString
}

private fun subnetMaskToCidr(mask: String): Int {
    if (!isValidSubnetMask(mask)) return 0
    return mask.split(".").sumOf { Integer.parseInt(it).toString(2).count { char -> char == '1' } }
}

private fun cidrToSubnetMask(cidr: Int): String {
    if (cidr !in 0..32) return "0.0.0.0"
    val maskValue = if (cidr == 0) 0L else (-1L shl (32 - cidr))
    return longToIp(maskValue and 0xFFFFFFFFL)
}

private fun calculateNetwork(ipAddress: String, subnetMask: String, cidr: Int, name: String? = null): NetworkCalculation? {
    if (!isValidIpAddress(ipAddress) || !isValidSubnetMask(subnetMask)) return null

    val ip = ipAddress.split(".").map { it.toLong() }
    val mask = subnetMask.split(".").map { it.toLong() }

    val networkAddressParts = ip.zip(mask) { ipPart, maskPart -> ipPart and maskPart }
    val wildcardParts = mask.map { 255 - it }
    val broadcastAddressParts = networkAddressParts.zip(wildcardParts) { netPart, wildPart -> netPart or wildPart }

    val networkAddress = networkAddressParts.joinToString(".")
    val broadcastAddress = broadcastAddressParts.joinToString(".")
    val firstIp = networkAddressParts.toMutableList().apply { if (cidr < 31) this[3]++ }.joinToString(".")
    val lastIp = broadcastAddressParts.toMutableList().apply { if (cidr < 31) this[3]-- }.joinToString(".")

    val totalHosts = 2.0.pow(32 - cidr).toLong()
    val usableHosts = if (cidr < 31) totalHosts - 2 else 0

    val ipClass = when (ip.first().toInt()) {
        in 1..126 -> "A"
        in 128..191 -> "B"
        in 192..223 -> "C"
        in 224..239 -> "D (Multicast)"
        else -> "E (Reserved)"
    }

    return NetworkCalculation(
        networkAddress, broadcastAddress, firstIp, lastIp, subnetMask,
        wildcardParts.joinToString("."), cidr, totalHosts, usableHosts, ipClass, name
    )
}

private fun calculateVlsm(majorNetwork: String, requests: List<VlsmSubnetRequest>): List<NetworkCalculation>? {
    val parts = majorNetwork.split('/')
    if (parts.size != 2) return null
    val majorIp = parts[0]
    val majorCidr = parts[1].toIntOrNull() ?: return null
    if (!isValidIpAddress(majorIp)) return null

    var currentIp = ipToLong(majorIp)
    val sortedRequests = requests.sortedByDescending { it.hosts }
    val results = mutableListOf<NetworkCalculation>()

    for (request in sortedRequests) {
        val requiredBits = ceil(log2(request.hosts.toDouble() + 2)).toInt()
        val subnetCidr = 32 - requiredBits
        val subnetMask = cidrToSubnetMask(subnetCidr)
        val networkIp = longToIp(currentIp)

        calculateNetwork(networkIp, subnetMask, subnetCidr, request.name)?.let {
            results.add(it)
        }

        currentIp += 2.0.pow(requiredBits).toLong()
    }
    return results
}

private fun ipToLong(ip: String): Long {
    return ip.split('.').map { it.toLong() }.reduce { acc, part -> (acc shl 8) + part }
}

private fun longToIp(ip: Long): String {
    return (0..3).map { (ip shr (24 - it * 8)) and 0xFF }.joinToString(".")
}


@Preview(showBackground = true)
@Composable
fun NetworkCalculatorScreenPreview() {
    MaterialTheme { NetworkCalculatorScreen() }
}

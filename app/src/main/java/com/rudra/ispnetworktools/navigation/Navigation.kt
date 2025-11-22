package com.rudra.ispnetworktools.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rudra.ispnetworktools.ui.DnsLookupScreen
import com.rudra.ispnetworktools.ui.FtpTestScreen
import com.rudra.ispnetworktools.ui.HttpHeaderScreen
import com.rudra.ispnetworktools.ui.ImapTestScreen
import com.rudra.ispnetworktools.ui.IpInfoScreen
import com.rudra.ispnetworktools.ui.NetworkCalculatorScreen
import com.rudra.ispnetworktools.ui.PacketCaptureScreen
import com.rudra.ispnetworktools.ui.PingScreen
import com.rudra.ispnetworktools.ui.Pop3TestScreen
import com.rudra.ispnetworktools.ui.PortScanScreen
import com.rudra.ispnetworktools.ui.SmtpTestScreen
import com.rudra.ispnetworktools.ui.SpeedTestScreen
import com.rudra.ispnetworktools.ui.SslCheckerScreen
import com.rudra.ispnetworktools.ui.TestHistoryScreen
import com.rudra.ispnetworktools.ui.TracerouteScreen
import com.rudra.ispnetworktools.ui.WakeOnLanScreen
import com.rudra.ispnetworktools.ui.WhoisScreen
import com.rudra.ispnetworktools.ui.WifiAnalyzerScreen
import com.rudra.ispnetworktools.ui.dashboard.DashboardScreen
import com.rudra.ispnetworktools.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Ping : Screen("ping", "Ping", Icons.Default.GraphicEq)
    object PortScan : Screen("port_scan", "Port Scan", Icons.Default.Search)
    object DnsLookup : Screen("dns_lookup", "DNS Lookup", Icons.Default.FindInPage)
    object Traceroute : Screen("traceroute", "Traceroute", Icons.Default.Timeline)
    object Whois : Screen("whois", "WHOIS", Icons.Default.Fingerprint)
    object SpeedTest : Screen("speed_test", "Speed Test", Icons.Default.NetworkCheck)
    object IpInfo : Screen("ip_info", "IP Info", Icons.Default.Info)
    object WakeOnLan : Screen("wake_on_lan", "Wake on LAN", Icons.Default.Power)
    object WifiAnalyzer : Screen("wifi_analyzer", "Wi-Fi Analyzer", Icons.Default.Wifi)
    object SslChecker : Screen("ssl_checker", "SSL Checker", Icons.Default.Security)
    object HttpHeader : Screen("http_header", "HTTP Headers", Icons.Default.FindInPage)
    object FtpTest : Screen("ftp_test", "FTP Test", Icons.Default.GraphicEq)
    object SmtpTest : Screen("smtp_test", "SMTP Test", Icons.Default.GraphicEq)
    object Pop3Test : Screen("pop3_test", "POP3 Test", Icons.Default.GraphicEq)
    object ImapTest : Screen("imap_test", "IMAP Test", Icons.Default.GraphicEq)
    object NetworkCalculator : Screen("network_calculator", "Network Calculator", Icons.Default.Calculate)
    object PacketCapture : Screen("packet_capture", "Packet Capture", Icons.Default.Security)
    object TestHistory : Screen("test_history", "History", Icons.Default.History)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }
        composable(Screen.Ping.route) {
            PingScreen()
        }
        composable(Screen.PortScan.route) {
            PortScanScreen()
        }
        composable(Screen.DnsLookup.route) {
            DnsLookupScreen()
        }
        composable(Screen.Traceroute.route) {
            TracerouteScreen()
        }
        composable(Screen.Whois.route) {
            WhoisScreen()
        }
        composable(Screen.SpeedTest.route) {
            SpeedTestScreen()
        }
        composable(Screen.IpInfo.route) {
            IpInfoScreen()
        }
        composable(Screen.WakeOnLan.route) {
            WakeOnLanScreen()
        }
        composable(Screen.WifiAnalyzer.route) {
            WifiAnalyzerScreen()
        }
        composable(Screen.SslChecker.route) {
            SslCheckerScreen()
        }
        composable(Screen.HttpHeader.route) {
            HttpHeaderScreen()
        }
        composable(Screen.FtpTest.route) {
            FtpTestScreen()
        }
        composable(Screen.SmtpTest.route) {
            SmtpTestScreen()
        }
        composable(Screen.Pop3Test.route) {
            Pop3TestScreen()
        }
        composable(Screen.ImapTest.route) {
            ImapTestScreen()
        }
        composable(Screen.NetworkCalculator.route) {
            NetworkCalculatorScreen()
        }
        composable(Screen.PacketCapture.route) {
            PacketCaptureScreen()
        }
        composable(Screen.TestHistory.route) {
            TestHistoryScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
package com.rudra.ispnetworktools.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rudra.ispnetworktools.navigation.Screen

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    DashboardContent(
        uiState = uiState,
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onToolClick = { tool ->
            viewModel.onToolUsed(tool)
            navController.navigate(tool.screen.route)
        },
        onFavoriteToggle = viewModel::onFavoriteToggle,
        navController = navController
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToolClick: (ToolItem) -> Unit,
    onFavoriteToggle: (ToolItem) -> Unit,
    navController: NavController
) {
    val tools = rememberToolItems()
    val filteredTools = tools.filter {
        it.screen.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        text = "Network Toolkit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.TestHistory.route) }) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                    IconButton(onClick = { /* Open settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        AnimatedVisibility(
            visible = !uiState.isLoading,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                SearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                )

                if (searchQuery.isEmpty()) {
                    QuickStatsSection(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                if (uiState.favoriteTools.isNotEmpty() && searchQuery.isEmpty()) {
                    FavoritesSection(
                        favoriteTools = uiState.favoriteTools,
                        onToolClick = onToolClick,
                        onFavoriteToggle = onFavoriteToggle,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f).padding(top = 16.dp)) {
                    Text(
                        text = if (searchQuery.isEmpty()) "All Tools" else "Search Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    ToolsGridSection(
                        tools = if (searchQuery.isEmpty()) tools else filteredTools,
                        favoriteTools = uiState.favoriteTools,
                        onToolClick = onToolClick,
                        onFavoriteToggle = onFavoriteToggle,
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                        isSearchResult = searchQuery.isNotEmpty()
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        placeholder = {
            Text(
                "Search tools...",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        singleLine = true
    )
}

@Composable
private fun QuickStatsSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "Quick Stats",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickStatItem("Recent Tests", "5", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            QuickStatItem("Favorite Tools", "3", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
            QuickStatItem("Network Status", "Online", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuickStatItem(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FavoritesSection(
    favoriteTools: List<ToolItem>,
    onToolClick: (ToolItem) -> Unit,
    onFavoriteToggle: (ToolItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Favorites",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(favoriteTools, key = { it.screen.route }) { tool ->
                FavoriteToolCard(
                    tool = tool,
                    onClick = { onToolClick(tool) },
                    onFavoriteToggle = { onFavoriteToggle(tool) }
                )
            }
        }
    }
}

@Composable
private fun ToolsGridSection(
    tools: List<ToolItem>,
    favoriteTools: List<ToolItem>,
    onToolClick: (ToolItem) -> Unit,
    onFavoriteToggle: (ToolItem) -> Unit,
    modifier: Modifier = Modifier,
    isSearchResult: Boolean = false
) {
    val categorizedTools = if (isSearchResult) {
        mapOf("Search Results" to tools)
    } else {
        tools.groupBy { it.category }
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        categorizedTools.forEach { (category, categoryTools) ->
            if (!isSearchResult) {
                item {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            items(categoryTools.chunked(2)) { rowTools ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowTools.forEach { tool ->
                        ToolCard(
                            tool = tool,
                            isFavorite = favoriteTools.any { it.screen.route == tool.screen.route },
                            onClick = { onToolClick(tool) },
                            onFavoriteToggle = { onFavoriteToggle(tool) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowTools.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolCard(
    tool: ToolItem,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    tool.gradientStart,
                                    tool.gradientEnd
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = tool.screen.title,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.Star,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = tool.screen.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = tool.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                color = tool.gradientStart.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = tool.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = tool.gradientStart,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoriteToolCard(
    tool: ToolItem,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                tool.gradientStart,
                                tool.gradientEnd
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.screen.title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = tool.screen.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Remove from favorites",
                    tint = MaterialTheme.colorScheme.error
                )
            }

            Icon(
                Icons.AutoMirrored.Default.ArrowForward,
                contentDescription = "Open tool",
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

data class ToolItem(
    val screen: Screen,
    val icon: ImageVector,
    val description: String,
    val category: String,
    val gradientStart: Color,
    val gradientEnd: Color
)

@Composable
private fun rememberToolItems(): List<ToolItem> {
    return listOf(
        ToolItem(
            screen = Screen.Ping,
            icon = Icons.Default.Radar,
            description = "Test network connectivity and latency",
            category = "Diagnostics",
            gradientStart = Color(0xFF667eea),
            gradientEnd = Color(0xFF764ba2)
        ),
        ToolItem(
            screen = Screen.PortScan,
            icon = Icons.Default.Security,
            description = "Scan for open ports on network devices",
            category = "Security",
            gradientStart = Color(0xFFf093fb),
            gradientEnd = Color(0xFFf5576c)
        ),
        ToolItem(
            screen = Screen.DnsLookup,
            icon = Icons.Default.Search,
            description = "Lookup DNS records and domain information",
            category = "Information",
            gradientStart = Color(0xFF4facfe),
            gradientEnd = Color(0xFF00f2fe)
        ),
        ToolItem(
            screen = Screen.Traceroute,
            icon = Icons.Default.NetworkCheck,
            description = "Trace the network path to a destination",
            category = "Diagnostics",
            gradientStart = Color(0xFF43e97b),
            gradientEnd = Color(0xFF38f9d7)
        ),
        ToolItem(
            screen = Screen.Whois,
            icon = Icons.Default.Badge,
            description = "Get domain registration information",
            category = "Information",
            gradientStart = Color(0xFFfa709a),
            gradientEnd = Color(0xFFfee140)
        ),
        ToolItem(
            screen = Screen.SpeedTest,
            icon = Icons.Default.Speed,
            description = "Test your internet connection speed",
            category = "Performance",
            gradientStart = Color(0xFF30cfd0),
            gradientEnd = Color(0xFF330867)
        ),
        ToolItem(
            screen = Screen.IpInfo,
            icon = Icons.Default.Public,
            description = "Get detailed information about IP addresses",
            category = "Information",
            gradientStart = Color(0xFFa8edea),
            gradientEnd = Color(0xFFfed6e3)
        ),
        ToolItem(
            screen = Screen.WakeOnLan,
            icon = Icons.Default.PowerSettingsNew,
            description = "Wake up devices on your local network",
            category = "Utilities",
            gradientStart = Color(0xFF5ee7df),
            gradientEnd = Color(0xFFb490ca)
        ),
        ToolItem(
            screen = Screen.WifiAnalyzer,
            icon = Icons.Default.Wifi,
            description = "Analyze WiFi networks and signal strength",
            category = "Wireless",
            gradientStart = Color(0xFFd299c2),
            gradientEnd = Color(0xFFfef9d7)
        ),
        ToolItem(
            screen = Screen.NetworkCalculator,
            icon = Icons.Default.Calculate,
            description = "Calculate network subnets and IP ranges",
            category = "Utilities",
            gradientStart = Color(0xFFcd9cf2),
            gradientEnd = Color(0xFFf6f3ff)
        ),
        ToolItem(
            screen = Screen.PacketCapture,
            icon = Icons.Default.DataUsage,
            description = "Capture and analyze network packets",
            category = "Advanced",
            gradientStart = Color(0xFF09203f),
            gradientEnd = Color(0xFF537895)
        ),
        ToolItem(
            screen = Screen.TestHistory,
            icon = Icons.Default.History,
            description = "View your previous test results",
            category = "History",
            gradientStart = Color(0xFF434343),
            gradientEnd = Color(0xFF000000)
        )
    )
}

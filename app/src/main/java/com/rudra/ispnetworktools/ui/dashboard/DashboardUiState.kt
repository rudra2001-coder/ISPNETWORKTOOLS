package com.rudra.ispnetworktools.ui.dashboard

data class DashboardUiState(
    val isLoading: Boolean = false,
    val favoriteTools: List<ToolItem> = emptyList(),
    val recentTools: List<ToolItem> = emptyList(),
    val error: String? = null ,
    val data: Any? = null
)





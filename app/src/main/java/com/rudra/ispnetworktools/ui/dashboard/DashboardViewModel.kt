package com.rudra.ispnetworktools.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.ispnetworktools.data.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Combine both favorites and recent tools
            combine(
                preferencesManager.getFavoriteTools(),
                preferencesManager.getRecentTools()
            ) { favorites, recent ->
                DashboardUiState(
                    isLoading = false,
                    favoriteTools = favorites,
                    recentTools = recent.take(5) // Show only 5 most recent
                )
            }.collect { newState ->
                _uiState.update { newState }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFavoriteToggle(tool: ToolItem) {
        viewModelScope.launch {
            val isNowFavorite = preferencesManager.toggleFavorite(tool)
            
            // Update UI state immediately for better UX
            _uiState.update { currentState ->
                val updatedFavorites = if (isNowFavorite) {
                    currentState.favoriteTools + tool
                } else {
                    currentState.favoriteTools.filter { it.screen.route != tool.screen.route }
                }
                currentState.copy(favoriteTools = updatedFavorites)
            }
        }
    }

    fun onToolUsed(tool: ToolItem) {
        viewModelScope.launch {
            preferencesManager.addToRecentTools(tool)
            
            // Update recent tools in UI state
            _uiState.update { currentState ->
                val updatedRecent = listOf(tool) + currentState.recentTools
                    .filter { it.screen.route != tool.screen.route }
                    .take(4) // Keep only 4 plus the new one = 5 total
                currentState.copy(recentTools = updatedRecent)
            }
        }
    }

    fun clearRecentTools() {
        viewModelScope.launch {
            preferencesManager.clearRecentTools()
            _uiState.update { it.copy(recentTools = emptyList()) }
        }
    }

    fun clearFavorites() {
        viewModelScope.launch {
            preferencesManager.clearFavorites()
            _uiState.update { it.copy(favoriteTools = emptyList()) }
        }
    }

    fun refreshData() {
        loadDashboardData()
    }
}
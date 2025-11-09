package com.rudra.ispnetworktools.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.rudra.ispnetworktools.ui.dashboard.ToolItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

    companion object {
        private val FAVORITE_TOOLS_KEY = stringSetPreferencesKey("favorite_tools")
        private val RECENT_TOOLS_KEY = stringSetPreferencesKey("recent_tools")
        private const val MAX_RECENT_TOOLS = 10
        private const val FAVORITES_LIMIT = 20
    }

    private val gson = Gson()

    suspend fun saveFavoriteTools(tools: List<ToolItem>) {
        context.dataStore.edit { preferences ->
            val limitedTools = tools.take(FAVORITES_LIMIT)
            preferences[FAVORITE_TOOLS_KEY] = limitedTools.map { tool ->
                gson.toJson(tool)
            }.toSet()
        }
    }

    fun getFavoriteTools(): Flow<List<ToolItem>> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[FAVORITE_TOOLS_KEY]?.mapNotNull { json ->
                    try {
                        gson.fromJson(json, ToolItem::class.java)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
            }
    }

    suspend fun addToRecentTools(tool: ToolItem) {
        context.dataStore.edit { preferences ->
            val currentRecentJson = preferences[RECENT_TOOLS_KEY] ?: emptySet()
            val currentRecent = currentRecentJson.mapNotNull { json ->
                    try {
                        gson.fromJson(json, ToolItem::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }.toMutableList()

            currentRecent.removeAll { it.screen.route == tool.screen.route }

            currentRecent.add(0, tool)

            if (currentRecent.size > MAX_RECENT_TOOLS) {
                currentRecent.subList(MAX_RECENT_TOOLS, currentRecent.size).clear()
            }

            preferences[RECENT_TOOLS_KEY] = currentRecent.map { gson.toJson(it) }.toSet()
        }
    }

    fun getRecentTools(): Flow<List<ToolItem>> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[RECENT_TOOLS_KEY]?.mapNotNull { json ->
                    try {
                        gson.fromJson(json, ToolItem::class.java)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
            }
    }

    suspend fun clearRecentTools() {
        context.dataStore.edit { preferences ->
            preferences.remove(RECENT_TOOLS_KEY)
        }
    }

    suspend fun clearFavorites() {
        context.dataStore.edit { preferences ->
            preferences.remove(FAVORITE_TOOLS_KEY)
        }
    }

    suspend fun toggleFavorite(tool: ToolItem): Boolean {
        var isNowFavorite = false
        context.dataStore.edit { preferences ->
            val currentFavoritesJson = preferences[FAVORITE_TOOLS_KEY] ?: emptySet()
            val currentFavorites = currentFavoritesJson.mapNotNull { json ->
                try {
                    gson.fromJson(json, ToolItem::class.java)
                } catch (e: Exception) {
                    null
                }
            }.toMutableList()

            val existingTool = currentFavorites.find { it.screen.route == tool.screen.route }

            if (existingTool != null) {
                currentFavorites.remove(existingTool)
                isNowFavorite = false
            } else {
                if (currentFavorites.size < FAVORITES_LIMIT) {
                    currentFavorites.add(tool)
                    isNowFavorite = true
                }
            }

            preferences[FAVORITE_TOOLS_KEY] = currentFavorites.map { gson.toJson(it) }.toSet()
        }
        return isNowFavorite
    }

    fun isToolFavorite(tool: ToolItem): Flow<Boolean> {
        return getFavoriteTools().map { favorites -> favorites.any { it.screen.route == tool.screen.route } }
    }

    fun getToolUsageStats(): Flow<Map<String, Int>> {
        return getRecentTools().map { recents ->
            val usageMap = mutableMapOf<String, Int>()
            recents.forEach {
                usageMap[it.screen.route] = usageMap.getOrDefault(it.screen.route, 0) + 1
            }
            usageMap
        }
    }
}

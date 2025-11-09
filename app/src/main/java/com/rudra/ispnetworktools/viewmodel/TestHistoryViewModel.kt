package com.rudra.ispnetworktools.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.ispnetworktools.data.AppDatabase
import com.rudra.ispnetworktools.data.TestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TestHistoryViewModel(context: Context) : ViewModel() {

    private val testResultDao = AppDatabase.getDatabase(context).testResultDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val testResults: StateFlow<List<TestResult>> = _searchQuery.flatMapLatest {
        if (it.isBlank()) {
            testResultDao.getAllTestResults()
        } else {
            testResultDao.searchTestResults("%${it}%")
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun clearHistory() {
        viewModelScope.launch {
            testResultDao.clearAll()
        }
    }
}

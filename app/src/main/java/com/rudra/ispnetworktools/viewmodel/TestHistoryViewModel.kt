package com.rudra.ispnetworktools.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.ispnetworktools.data.TestHistoryRepository
import com.rudra.ispnetworktools.data.TestResult
import com.rudra.ispnetworktools.data.TestResultDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TestHistoryViewModel @Inject constructor(
    private val testResultDao: TestResultDao,
    private val testHistoryRepository: TestHistoryRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val testResults: StateFlow<List<TestResult>> = testResultDao.getAllTestResults()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _shareFile = MutableSharedFlow<Uri>()
    val shareFile = _shareFile.asSharedFlow()

    fun exportHistory() {
        viewModelScope.launch {
            val file = testHistoryRepository.exportTestHistoryToCsv()
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            _shareFile.emit(uri)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            testResultDao.clearAll()
        }
    }
}

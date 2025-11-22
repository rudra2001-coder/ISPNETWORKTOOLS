package com.rudra.ispnetworktools.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.ispnetworktools.data.TestResultDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val testResultDao: TestResultDao
) : ViewModel() {

    fun resetApplication() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                testResultDao.clearAll()
            }
        }
    }
}
package com.rudra.ispnetworktools.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rudra.ispnetworktools.data.TestResultDao

class TestHistoryViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TestHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TestHistoryViewModel(
                context as TestResultDao,
                testHistoryRepository = TODO(),
                context = TODO()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
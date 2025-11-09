package com.rudra.ispnetworktools.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WifiAnalyzerViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WifiAnalyzerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WifiAnalyzerViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

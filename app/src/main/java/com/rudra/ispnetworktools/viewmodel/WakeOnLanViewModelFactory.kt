package com.rudra.ispnetworktools.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WakeOnLanViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WakeOnLanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WakeOnLanViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

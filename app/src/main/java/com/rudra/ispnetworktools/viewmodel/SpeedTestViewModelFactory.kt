package com.rudra.ispnetworktools.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rudra.ispnetworktools.data.SpeedTestRepository

class SpeedTestViewModelFactory(private val repository: SpeedTestRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpeedTestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SpeedTestViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

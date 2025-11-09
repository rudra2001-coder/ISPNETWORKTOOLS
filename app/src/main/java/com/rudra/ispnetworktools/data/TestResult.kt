package com.rudra.ispnetworktools.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_results")
data class TestResult(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val testType: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

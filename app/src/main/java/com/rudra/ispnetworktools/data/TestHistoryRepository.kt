package com.rudra.ispnetworktools.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import javax.inject.Inject

class TestHistoryRepository @Inject constructor(
    private val testResultDao: TestResultDao,
    @ApplicationContext private val context: Context
) {

    suspend fun exportTestHistoryToCsv(): File = withContext(Dispatchers.IO) {
        val testResults = testResultDao.getAllTestResults().first()
        val file = File(context.getExternalFilesDir(null), "test_history.csv")
        val fileWriter = FileWriter(file)

        try {
            // Write CSV header
            fileWriter.append("ID,Test Type,Result,Timestamp\n")

            // Write test results
            testResults.forEach { result ->
                // Properly escape quotes and handle potential null values
                val escapedResult = result.result?.replace("\"", "\"\"") ?: ""
                fileWriter.append("${result.id},${result.testType},\"$escapedResult\",${result.timestamp}\n")
            }

            fileWriter.flush()
        } finally {
            fileWriter.close()
        }

        return@withContext file
    }
}
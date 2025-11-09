package com.rudra.ispnetworktools.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TestResultDao {

    @Insert
    suspend fun insert(testResult: TestResult)

    @Query("SELECT * FROM test_results ORDER BY timestamp DESC")
    fun getAllTestResults(): Flow<List<TestResult>>

    @Query("SELECT * FROM test_results WHERE testType LIKE :query OR result LIKE :query ORDER BY timestamp DESC")
    fun searchTestResults(query: String): Flow<List<TestResult>>

    @Query("DELETE FROM test_results")
    suspend fun clearAll()
}

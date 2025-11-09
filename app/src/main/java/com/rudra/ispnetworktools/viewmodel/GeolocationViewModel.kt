package com.rudra.ispnetworktools.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.ispnetworktools.data.AppDatabase
import com.rudra.ispnetworktools.data.TestResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class GeolocationResponse(
    val country: String,
    val regionName: String,
    val city: String,
    val lat: Double,
    val lon: Double,
    val isp: String,
    val org: String,
    val query: String
)

class GeolocationViewModel(context: Context) : ViewModel() {

    private val _geolocationResult = MutableStateFlow("")
    val geolocationResult: StateFlow<String> = _geolocationResult

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }
    private val testResultDao = AppDatabase.getDatabase(context).testResultDao()

    fun getGeolocation(ipAddress: String) {
        viewModelScope.launch {
            _geolocationResult.value = "Fetching geolocation for $ipAddress..."
            try {
                val response: GeolocationResponse = client.get("http://ip-api.com/json/$ipAddress").body()
                val result = buildString {
                    append("IP: ").append(response.query).append("\n")
                    append("Country: ").append(response.country).append("\n")
                    append("Region: ").append(response.regionName).append("\n")
                    append("City: ").append(response.city).append("\n")
                    append("ISP: ").append(response.isp).append("\n")
                    append("Organization: ").append(response.org).append("\n")
                    append("Latitude: ").append(response.lat).append("\n")
                    append("Longitude: ").append(response.lon)
                }
                _geolocationResult.value = result
                testResultDao.insert(TestResult(testType = "Geolocation", result = result))
            } catch (e: Exception) {
                val errorResult = "Error: ${e.message}"
                _geolocationResult.value = errorResult
                testResultDao.insert(TestResult(testType = "Geolocation", result = errorResult))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}

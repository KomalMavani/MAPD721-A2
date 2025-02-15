package com.example.mapd721_a2

import androidx.compose.runtime.*
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter


class HealthConnectManager : ViewModel() {
    private var healthConnectClient: HealthConnectClient? = null
    private var _readings = mutableStateOf<List<HeartRateRecord>>(emptyList())
    val readings: State<List<HeartRateRecord>> = _readings

    private var _message = mutableStateOf<String?>(null)
    val message: State<String?> = _message

    fun initialize(client: HealthConnectClient) {
        healthConnectClient = client
    }

    fun saveHeartRate(heartRate: Int, dateTimeStr: String) {
        viewModelScope.launch {
            try {
                val localDateTime = LocalDateTime.parse(dateTimeStr,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                val zoneOffset = ZoneOffset.systemDefault().rules.getOffset(localDateTime)
                val instant = localDateTime.toInstant(zoneOffset)

                val record = HeartRateRecord(
                    startTime = instant,
                    endTime = instant,
                    startZoneOffset = zoneOffset,
                    endZoneOffset = zoneOffset,
                    samples = listOf(
                        HeartRateRecord.Sample(
                            beatsPerMinute = heartRate.toLong(),
                            time = instant
                        )
                    )
                )

                healthConnectClient?.insertRecords(listOf(record))
                _message.value = "Heart rate saved successfully!"
                loadHeartRates()
            } catch (e: Exception) {
                _message.value = "Error saving heart rate: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun loadHeartRates() {
        viewModelScope.launch {

            try {
                if (healthConnectClient == null) {
                    _message.value = "HealthConnectClient is not initialized"
                    return@launch
                }

                val now = Instant.now()
                val startTime = now.minus(Duration.ofDays(30))

                val request = ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, now)
                )

                val response = healthConnectClient!!.readRecords(request)
                _readings.value = response.records
                _message.value = if (response.records.isNotEmpty()) {
                    "Loaded ${response.records.size} readings"
                } else {
                    "No heart rate readings found"
                }
            } catch (e: Exception) {
                _message.value = "Error loading heart rates: ${e.message}"
                e.printStackTrace()
                _readings.value = emptyList()
            }
        }
    }


    fun clearMessage() {
        _message.value = null
    }

}
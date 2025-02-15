package com.example.mapd721_a2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.border
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CardDefaults

import androidx.lifecycle.lifecycleScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    private lateinit var healthConnectClient: HealthConnectClient
    private val permissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class)

    )

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        checkPermissionsAndInitialize()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        healthConnectClient = HealthConnectClient.getOrCreate(this)
        checkPermissionsAndInitialize()
    }

    private fun checkPermissionsAndInitialize() {
        lifecycleScope.launch {
            val hasPermissions = healthConnectClient.permissionController
                .getGrantedPermissions()
                .containsAll(permissions)

            if (hasPermissions) {
                setContent {
                    HealthConnectApp(healthConnectClient)
                }
            } else {
                requestPermissionLauncher.launch(permissions.toTypedArray())
            }
        }
    }
}

@Composable
fun HealthConnectApp(healthConnectClient: HealthConnectClient) {
    val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<HealthViewModel>()
    var heartRate by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf("") }
    val readings by viewModel.readings
    val message by viewModel.message
    val context = LocalContext.current // Retrieve the context for the Toast

    // Initialize the ViewModel with the HealthConnectClient
    LaunchedEffect(Unit) {
        viewModel.initialize(healthConnectClient)
    }

    // Show message if it exists
    LaunchedEffect(message) {
        message?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage() // Clear the message after displaying
        }
    }

    // The main UI content
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .background(Color(0xFFF1F1F1)) // Set light grey background color here
    ) {

        // Heart rate input field
        OutlinedTextField(
            value = heartRate,
            onValueChange = { heartRate = it },
            label = { Text("Heart Rate") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Date/Time input field
        OutlinedTextField(
            value = dateTime,
            onValueChange = { dateTime = it },
            label = { Text("Date/Time (yyyy-MM-dd HH:mm)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Buttons (Save and Load) in one line, full width
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Optional padding around the buttons
        ) {
            Button(
                onClick = {
                    heartRate.toIntOrNull()?.let { hr ->
                        viewModel.saveHeartRate(hr, dateTime)
                    }
                },
                modifier = Modifier.weight(1f), // Equal space for each button
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFF176), // Light yellow background
                    contentColor = Color.Black // Black text color
                ),
                border = BorderStroke(2.dp, Color(0xFFFFA000)) // Dark yellow border
            ) {
                Text("Save", fontWeight = FontWeight.Bold) // Bold text
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    viewModel.loadHeartRates()
                },
                modifier = Modifier.weight(1f), // Equal space for each button
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA5D6A7), // Light green background
                    contentColor = Color.Black // Black text color
                ),
                border = BorderStroke(2.dp, Color(0xFF388E3C)) // Dark green border
            ) {
                Text("Load", fontWeight = FontWeight.Bold) // Bold text
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History Section
        Text("Heartrate History", style = MaterialTheme.typography.titleMedium)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            itemsIndexed(readings) { index, reading ->
                // Alternate colors based on even or odd index
                val backgroundColor = if (index % 2 == 0) Color(0xFFFFF176) else Color(0xFFA5D6A7) // Light gray and darker gray

                HeartRateItem(reading, backgroundColor)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About Section at the top
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F1F1))
                .padding(vertical = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Student Name: Komal Mavani")
                Text("Student ID: 301472922")
            }

        }
    }
}

@Composable
fun HeartRateItem(reading: HeartRateRecord, backgroundColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            reading.samples.firstOrNull()?.let { sample ->
                Text("Heart Rate: ${sample.beatsPerMinute} bpm", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Time: ${
                        reading.startTime.atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    }",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
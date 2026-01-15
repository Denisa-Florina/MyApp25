package com.example.myapp.todo.ui.item

import MyLocation
import MyLocationViewModel
import android.Manifest
import android.app.Application
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.R
import com.example.myapp.core.Result
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(itemId: String?, onClose: () -> Unit) {
    val itemViewModel = viewModel<ItemViewModel>(factory = ItemViewModel.Factory(itemId))
    val itemUiState = itemViewModel.uiState

    // States for the form
    var text by rememberSaveable { mutableStateOf(itemUiState.item.text) }
    var description by rememberSaveable { mutableStateOf(itemUiState.item.description) }
    var priority by rememberSaveable { mutableStateOf(itemUiState.item.priority.toFloat()) }
    var isCompleted by rememberSaveable { mutableStateOf(itemUiState.item.isCompleted) }
    var latitude by rememberSaveable { mutableStateOf(itemUiState.item.latitude) }
    var longitude by rememberSaveable { mutableStateOf(itemUiState.item.longitude) }
    var dueDate by rememberSaveable { mutableStateOf(itemUiState.item.dueDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Logic for FAB animation
    val isReadyToSave = text.isNotBlank()

    val locationViewModel = viewModel<MyLocationViewModel>(
        factory = MyLocationViewModel.Factory(
            LocalContext.current.applicationContext as Application
        )
    )

    // Initialization logic for editing existing items
    var initialized by remember { mutableStateOf(itemId == null) }
    LaunchedEffect(itemId, itemUiState.loadResult) {
        if (!initialized && itemUiState.loadResult !is Result.Loading) {
            val item = itemUiState.item
            text = item.text
            description = item.description
            priority = item.priority.toFloat()
            isCompleted = item.isCompleted
            dueDate = item.dueDate
            latitude = item.latitude
            longitude = item.longitude
            initialized = true
        }
    }

    // --- Date Picker Dialog ---
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate?.time ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dueDate = Date(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.item)) }
            )
        },
        floatingActionButton = {
            // Butonul de Salvare Plutitor cu Animație
            FloatingActionButton(
                onClick = {
                    itemViewModel.saveOrUpdateItem(
                        text = text,
                        description = description,
                        priority = priority.roundToInt(),
                        isCompleted = isCompleted,
                        dueDate = dueDate,
                        latitude = latitude,
                        longitude = longitude
                    )
                    onClose()
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save"
                    )
                    AnimatedVisibility(visible = isReadyToSave) {
                        Text(
                            text = "Save",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (itemUiState.loadResult is Result.Loading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                if (itemUiState.submitResult is Result.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                // 1. TITLE INPUT
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 2. PRIORITY SLIDER
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Priority", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = priority.roundToInt().toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = priority,
                        onValueChange = { priority = it },
                        valueRange = 0f..5f,
                        steps = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 3. DATE PICKER
                val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
                val dateSource = remember { MutableInteractionSource() }
                LaunchedEffect(dateSource) {
                    dateSource.interactions.collect { if (it is PressInteraction.Release) showDatePicker = true }
                }

                OutlinedTextField(
                    value = dueDate?.let { dateFormatter.format(it) } ?: "",
                    onValueChange = {},
                    label = { Text("Due Date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    interactionSource = dateSource
                )

                // 4. DESCRIPTION INPUT
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 5
                )

                // 5. MAP
                MyLocation(
                    modifier = Modifier.fillMaxWidth().height(250.dp)
                )

                // 6. COMPLETED SWITCH
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCompleted = !isCompleted }
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = "Mark as Completed", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = isCompleted, onCheckedChange = { isCompleted = it })
                }

                if (itemUiState.submitResult is Result.Error) {
                    Text(
                        text = "Error: ${(itemUiState.submitResult as Result.Error).exception?.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Spacer final pentru a nu fi acoperit conținutul de butonul plutitor
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
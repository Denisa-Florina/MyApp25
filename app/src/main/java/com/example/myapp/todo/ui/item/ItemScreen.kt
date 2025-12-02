package com.example.myapp.todo.ui.item

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.R
import com.example.myapp.core.Result
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(itemId: String?, onClose: () -> Unit) {
    val itemViewModel = viewModel<ItemViewModel>(factory = ItemViewModel.Factory(itemId))
    val itemUiState = itemViewModel.uiState

    // State for form fields
    var text by rememberSaveable { mutableStateOf(itemUiState.item.text) }
    var description by rememberSaveable { mutableStateOf(itemUiState.item.description) }
    var priority by rememberSaveable { mutableStateOf(itemUiState.item.priority.toString()) }
    var isCompleted by rememberSaveable { mutableStateOf(itemUiState.item.isCompleted) }

    // Date State
    var dueDate by rememberSaveable { mutableStateOf(itemUiState.item.dueDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    Log.d("ItemScreen", "recompose, text = $text")

    // When submitting finishes, close screen
    LaunchedEffect(itemUiState.submitResult) {
        if (itemUiState.submitResult is Result.Success) {
            onClose()
        }
    }

    // When loading existing item, update fields once
    var initialized by remember { mutableStateOf(itemId == null) }
    LaunchedEffect(itemId, itemUiState.loadResult) {
        if (!initialized && itemUiState.loadResult !is Result.Loading) {
            val item = itemUiState.item
            text = item.text
            description = item.description
            priority = item.priority.toString()
            isCompleted = item.isCompleted
            dueDate = item.dueDate
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
                    datePickerState.selectedDateMillis?.let {
                        dueDate = Date(it)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.item)) },
                actions = {
                    Button(onClick = {
                        itemViewModel.saveOrUpdateItem(
                            text = text,
                            description = description,
                            priority = priority.toIntOrNull() ?: 0,
                            isCompleted = isCompleted,
                            dueDate = dueDate // Pass the date here
                        )
                    }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (itemUiState.loadResult is Result.Loading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { CircularProgressIndicator() }
            } else {

                if (itemUiState.submitResult is Result.Loading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) { LinearProgressIndicator() }
                }

                if (itemUiState.loadResult is Result.Error) {
                    Text(
                        text = "Failed to load item - ${(itemUiState.loadResult as Result.Error).exception?.message}"
                    )
                }

                // TEXT field
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Text") },
                    modifier = Modifier.fillMaxWidth()
                )

                // DESCRIPTION field
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                // PRIORITY field
                TextField(
                    value = priority,
                    onValueChange = { priority = it },
                    label = { Text("Priority") },
                    modifier = Modifier.fillMaxWidth()
                )

                // DATE PICKER FIELD
                // We use an OutlinedTextField that is read-only.
                // We detect clicks to open the dialog.
                val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

                OutlinedTextField(
                    value = dueDate?.let { dateFormatter.format(it) } ?: "",
                    onValueChange = {},
                    label = { Text("Due Date") },
                    readOnly = true, // Prevent typing
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Date"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    // This interaction source trick ensures the click registers even if readOnly is true
                    interactionSource = remember { MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect {
                                    if (it is PressInteraction.Release) {
                                        showDatePicker = true
                                    }
                                }
                            }
                        }
                )

                // COMPLETED checkbox
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { isCompleted = it }
                    )
                    Text("Completed")
                }

                if (itemUiState.submitResult is Result.Error) {
                    Text(
                        text = "Failed to submit item - ${(itemUiState.submitResult as Result.Error).exception?.message}"
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewItemScreen() {
    ItemScreen(itemId = "0", onClose = {})
}
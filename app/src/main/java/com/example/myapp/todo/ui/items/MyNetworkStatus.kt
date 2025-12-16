package com.example.myapp.todo.ui.items

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapp.todo.ui.items.ConnectivityManagerNetworkMonitor
import kotlinx.coroutines.launch

class MyNetworkStatusViewModel(application: Application) : AndroidViewModel(application) {
    var uiState by mutableStateOf(false)
        private set

    init {
        collectNetworkStatus()
    }

    private fun collectNetworkStatus() {
        viewModelScope.launch {
            ConnectivityManagerNetworkMonitor(getApplication()).isOnline.collect {
                uiState = it;
            }
        }
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MyNetworkStatusViewModel(application)
            }
        }
    }
}

@Composable
fun NetworkStatusIcon(isOnline: Boolean) {
    Box(
        modifier = Modifier
            .background(
                color = if (isOnline)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else
                    MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isOnline)
                    Icons.Default.Wifi
                else
                    Icons.Default.WifiOff,
                contentDescription = null,
                tint = if (isOnline)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = if (isOnline) "Online" else "Offline",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}


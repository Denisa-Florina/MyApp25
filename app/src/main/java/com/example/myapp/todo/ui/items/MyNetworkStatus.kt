package com.example.myapp.todo.ui.items

import android.app.Application
import android.Manifest
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class MyNetworkStatusViewModel(application: Application)
    : AndroidViewModel(application) {

    var uiState by mutableStateOf(false)
        private set

    init {
        createNetworkNotificationChannel(application)
        collectNetworkStatus()
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    MyNetworkStatusViewModel(application)
                }
            }
    }


    private fun collectNetworkStatus() {
        viewModelScope.launch {
            ConnectivityManagerNetworkMonitor(getApplication())
                .isOnline
                .distinctUntilChanged()
                .collect { isOnline ->
                    uiState = isOnline

                    // Only show notification if permission granted (Android 13+)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        val permissionGranted =
                            ContextCompat.checkSelfPermission(
                                getApplication(),
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        if (!permissionGranted) return@collect
                    }

                    showNetworkStatusNotification(getApplication(), isOnline)
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

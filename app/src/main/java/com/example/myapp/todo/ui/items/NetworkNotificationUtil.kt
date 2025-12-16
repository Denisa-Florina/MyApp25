package com.example.myapp.todo.ui.items

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myapp.R
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

fun createNetworkNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "NETWORK_STATUS",
            "Network status",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifies when network status changes"
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        manager.createNotificationChannel(channel)
    }
}

fun showNetworkStatusNotification(
    context: Context,
    isOnline: Boolean
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            return
        }
    }

    val builder = NotificationCompat.Builder(context, "NETWORK_STATUS")
        .setSmallIcon(android.R.drawable.stat_notify_sync)
        .setContentTitle("Network status")
        .setContentText(
            if (isOnline) "🟢 Back online, we can sync the data" else "🔴 You are offline :(("
        )
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    NotificationManagerCompat.from(context)
        .notify(1001, builder.build())
}


package com.example.myapp.todo.ui.items

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapp.todo.data.Item
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.RectangleShape
import com.example.myapp.todo.data.SyncStatus

typealias OnItemFn = (id: String?) -> Unit
@Composable
fun ItemList(
    itemList: List<Item>,
    onItemClick: OnItemFn,
    onDeleteItem: OnItemFn,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(itemList) { item ->
            ItemDetailCard(item, onItemClick, onDeleteItem)
        }
    }
}

@Composable
fun ItemDetailCard(item: Item, onItemClick: OnItemFn, onDeleteItem: OnItemFn) {
    val cardColors = if (item.isCompleted) {
        CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    } else {
        CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    ElevatedCard(
        onClick = { onItemClick(item._id) },
        colors = cardColors,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PriorityIndicator(item.priority)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    SyncStatusIndicator(item.syncStatus)
                }
                if (item.description.isNotEmpty()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                item.dueDate?.let { date ->
                    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
                    Text(
                        text = "Due: ${formatter.format(date)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            if (item.isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = { onDeleteItem(item._id) }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete item"
                )
            }
        }
    }
}

@Composable
fun SyncStatusIndicator(syncStatus: SyncStatus) {
    when (syncStatus) {
        SyncStatus.PENDING -> {
            Icon(
                imageVector = Icons.Default.CloudQueue,
                contentDescription = "Pending sync",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
            )
        }
        SyncStatus.UPDATED -> {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = "Has updates",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
            )
        }
        SyncStatus.DELETED -> {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Pending deletion",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
        }
        SyncStatus.SYNCED -> {
        }
    }
}
@Composable
fun PriorityIndicator(priority: Int) {
    val color = when (priority) {
        in 4..Int.MAX_VALUE -> MaterialTheme.colorScheme.error
        3 -> Color(0xFFFFA000)
        else -> Color(0xFF4CAF50)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "PriorityPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    Box(
        modifier = Modifier
            .size(12.dp)
            .scale(if (priority >= 4) scale else 1f)
            .background(color = color, shape = RectangleShape)
    )
}
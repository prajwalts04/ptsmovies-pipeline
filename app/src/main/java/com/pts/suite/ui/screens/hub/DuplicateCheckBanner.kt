package com.pts.suite.ui.screens.hub

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pts.suite.ui.theme.*

// Duplicate check result model
data class DuplicateCheckResult(
    val isDuplicate: Boolean = false,
    val onDisk: Boolean = false,
    val inQueue: Boolean = false,
    val diskPath: String? = null,
    val diskSize: String? = null,
    val queueTaskId: String? = null,
    val queueStage: String? = null,
    val seriesEpisodeDupes: List<EpisodeDupeInfo> = emptyList()
)

data class EpisodeDupeInfo(
    val season: Int,
    val episode: Int,
    val epCode: String,
    val onDisk: Boolean = false,
    val inQueue: Boolean = false
)

@Composable
fun DuplicateCheckBanner(
    result: DuplicateCheckResult,
    onSkipDuplicates: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    if (!result.isDuplicate && !result.onDisk && !result.inQueue && result.seriesEpisodeDupes.isEmpty()) {
        return
    }

    val amberBorder = Color(0xFFF59E0B)
    val amberBg = Color(0xF01A140A)
    val onDiskGreen = Color(0xFF4ADE80)
    val onDiskBg = Color(0x3322C55E)
    val inQueueBlue = Color(0xFF38BDF8)
    val inQueueBg = Color(0x3338BDF8)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(amberBg)
            .border(1.5.dp, amberBorder, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header Row with warning icon and badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = "Duplicate Warning",
                    tint = amberBorder,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "DUPLICATE DETECTED",
                    color = amberBorder,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                if (result.onDisk) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = onDiskBg,
                        border = BorderStroke(0.5.dp, onDiskGreen)
                    ) {
                        Text(
                            text = "ON DISK",
                            color = onDiskGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (result.inQueue) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = inQueueBg,
                        border = BorderStroke(0.5.dp, inQueueBlue)
                    ) {
                        Text(
                            text = "IN QUEUE",
                            color = inQueueBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Graphite400, modifier = Modifier.size(16.dp))
            }
        }

        // Details description
        if (!result.diskPath.isNullOrBlank()) {
            Text(
                text = "File: ${result.diskPath} (${result.diskSize ?: "Exists on disk"})",
                color = Graphite300,
                fontSize = 11.5.sp,
                fontFamily = FontFamily.Monospace
            )
        } else if (result.queueStage != null) {
            Text(
                text = "Item is already active in queue: Stage [${result.queueStage}]",
                color = Graphite300,
                fontSize = 11.5.sp
            )
        }

        // Series Episode Chips (if series duplicate check)
        if (result.seriesEpisodeDupes.isNotEmpty()) {
            Text(
                text = "Series Episode Status:",
                color = Graphite200,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(result.seriesEpisodeDupes) { ep ->
                    val chipColor = when {
                        ep.onDisk -> onDiskGreen
                        ep.inQueue -> inQueueBlue
                        else -> Graphite400
                    }
                    val chipBg = when {
                        ep.onDisk -> onDiskBg
                        ep.inQueue -> inQueueBg
                        else -> DarkSurfaceElevated
                    }
                    val statusText = when {
                        ep.onDisk -> "DISK"
                        ep.inQueue -> "QUEUE"
                        else -> "NEW"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(chipBg)
                            .border(0.5.dp, chipColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${ep.epCode} ($statusText)",
                            color = chipColor,
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Skip Duplicates Button
            Button(
                onClick = onSkipDuplicates,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, amberBorder),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth().height(36.dp)
            ) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = null,
                    tint = amberBorder,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SKIP DUPLICATES & ENQUEUE REMAINING",
                    color = amberBorder,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

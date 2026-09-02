package com.pts.suite.ui.screens.hub

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pts.suite.data.api.DownloadTask
import com.pts.suite.data.api.RetrofitClient
import com.pts.suite.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HubQueueScreen(
    tasks: List<DownloadTask>,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf("queue") } // queue, single, bulk
    var inputUrl by remember { mutableStateOf("") }
    var inputTitle by remember { mutableStateOf("") }
    var inputBulkText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Sub-Navigation Tabs: Queue, Single Dispatch, Bulk Paste
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                "queue" to "Queue (${tasks.size})",
                "single" to "Single Dispatch",
                "bulk" to "Bulk Paste"
            )
            tabs.forEach { (tabId, label) ->
                val isSelected = selectedTab == tabId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) EmeraldGreen else DarkSurface)
                        .border(1.dp, if (isSelected) EmeraldGreen else SketchBorder, RoundedCornerShape(20.dp))
                        .clickable { selectedTab = tabId }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) PitchBlack else Graphite200,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        when (selectedTab) {
            "single" -> {
                // Single Dispatch Form
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurface)
                        .border(1.dp, SketchBorder, RoundedCornerShape(10.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "DISPATCH NEW TRANSCODE", color = Graphite100, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = inputTitle,
                        onValueChange = { inputTitle = it },
                        label = { Text("Title & Year (e.g. Leo 2023)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = SketchBorder,
                            focusedTextColor = Graphite100,
                            unfocusedTextColor = Graphite200
                        )
                    )

                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        label = { Text("Direct Video URL / Magnet Link") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = SketchBorder,
                            focusedTextColor = Graphite100,
                            unfocusedTextColor = Graphite200
                        )
                    )

                    Button(
                        onClick = {
                            if (inputUrl.isBlank()) {
                                Toast.makeText(context, "Please enter a download link", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isSubmitting = true
                            scope.launch {
                                try {
                                    val service = RetrofitClient.getService(context)
                                    val title = if (inputTitle.isNotBlank()) inputTitle else "Download " + System.currentTimeMillis()
                                    val payload = mapOf(
                                        "metadata" to mapOf("imdbId" to "pts_" + System.currentTimeMillis(), "title" to title, "year" to "2026"),
                                        "type" to "Movie",
                                        "downloadLink" to inputUrl,
                                        "quality" to "480p x265"
                                    )
                                    val res = service.queueDownload(payload)
                                    if (res.isSuccessful) {
                                        Toast.makeText(context, "Dispatched to GHA Pipeline!", Toast.LENGTH_SHORT).show()
                                        inputUrl = ""
                                        inputTitle = ""
                                        selectedTab = "queue"
                                        onRefresh()
                                    } else {
                                        Toast.makeText(context, "Dispatch error: " + res.code(), Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Text("DISPATCH TO GHA PIPELINE", color = PitchBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }

            "bulk" -> {
                // Bulk Paste Form (Excel tab-separated: Season \t Episode \t Link)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurface)
                        .border(1.dp, SketchBorder, RoundedCornerShape(10.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "BULK EXCEL PASTE", color = Graphite100, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = "Paste rows copied from Excel:\nColumn 1: Season (e.g. 1) | Column 2: Episode (e.g. 01) | Column 3: Link",
                        color = Graphite400,
                        fontSize = 11.sp
                    )

                    OutlinedTextField(
                        value = inputTitle,
                        onValueChange = { inputTitle = it },
                        label = { Text("Series Name (e.g. Breaking Bad)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = SketchBorder,
                            focusedTextColor = Graphite100,
                            unfocusedTextColor = Graphite200
                        )
                    )

                    OutlinedTextField(
                        value = inputBulkText,
                        onValueChange = { inputBulkText = it },
                        label = { Text("Paste tab-separated rows here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = SketchBorder,
                            focusedTextColor = Graphite100,
                            unfocusedTextColor = Graphite200
                        )
                    )

                    Button(
                        onClick = {
                            if (inputBulkText.isBlank()) {
                                Toast.makeText(context, "Paste rows first", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isSubmitting = true
                            scope.launch {
                                try {
                                    val lines = inputBulkText.lines().filter { it.isNotBlank() }
                                    var count = 0
                                    val service = RetrofitClient.getService(context)
                                    lines.forEach { line ->
                                        val parts = line.split("\t", "   ", " ")
                                        val link = parts.lastOrNull()?.trim() ?: ""
                                        if (link.startsWith("http")) {
                                            val payload = mapOf(
                                                "metadata" to mapOf(
                                                    "imdbId" to "ser_" + System.currentTimeMillis(),
                                                    "title" to (if (inputTitle.isNotBlank()) inputTitle else "Series Bulk")
                                                ),
                                                "type" to "Series",
                                                "downloadLink" to link,
                                                "quality" to "480p x265"
                                            )
                                            service.queueDownload(payload)
                                            count++
                                        }
                                    }
                                    Toast.makeText(context, "Dispatched $count items!", Toast.LENGTH_SHORT).show()
                                    inputBulkText = ""
                                    selectedTab = "queue"
                                    onRefresh()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Text("QUEUE ALL BULK EPISODES", color = PitchBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }

            "queue" -> {
                // Queue List & Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE GHA PIPELINE JOBS",
                        color = Graphite300,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = EmeraldGreen)
                        }
                        IconButton(onClick = {
                            scope.launch {
                                try {
                                    RetrofitClient.getService(context).clearCompletedDownloads()
                                    onRefresh()
                                    Toast.makeText(context, "Cleared completed", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {}
                            }
                        }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Completed", tint = Graphite400)
                        }
                    }
                }

                if (tasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface)
                            .border(1.dp, SketchBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("All downloads finished. Queue is idle.", color = Graphite200, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(tasks, key = { it.id }) { task ->
                            DownloadTaskCard(
                                task = task,
                                onCancel = {
                                    scope.launch {
                                        try {
                                            RetrofitClient.getService(context).cancelDownloadTask(task.id)
                                            onRefresh()
                                        } catch (e: Exception) {}
                                    }
                                },
                                onRetry = {
                                    scope.launch {
                                        try {
                                            RetrofitClient.getService(context).retryDownloadTask(task.id)
                                            onRefresh()
                                        } catch (e: Exception) {}
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadTaskCard(
    task: DownloadTask,
    onCancel: () -> Unit,
    onRetry: () -> Unit
) {
    val stageColor = when (task.stage) {
        "completed" -> EmeraldGreen
        "failed", "error" -> DangerRed
        "gha_compressing" -> Color(0xFF38BDF8)
        "pi_downloading" -> Color(0xFFF59E0B)
        else -> Graphite300
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .border(1.dp, SketchBorder, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Graphite100,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            // Stage Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(stageColor.copy(alpha = 0.2f))
                    .border(1.dp, stageColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = task.stage.replace("_", " ").uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = stageColor
                )
            }
        }

        // Progress bar
        LinearProgressIndicator(
            progress = { (task.progress.coerceIn(0, 100)) / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = stageColor,
            trackColor = Graphite800
        )

        // Telemetry Row: Speed, Progress %, ETA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${task.progress}% • ${task.speed}",
                fontSize = 12.sp,
                color = Graphite300,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "ETA: ${task.eta}",
                fontSize = 12.sp,
                color = Graphite400,
                fontFamily = FontFamily.Monospace
            )
        }

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (task.stage == "failed" || task.status == "error") {
                TextButton(onClick = onRetry) {
                    Text("RETRY", color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            TextButton(onClick = onCancel) {
                Text("CANCEL", color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

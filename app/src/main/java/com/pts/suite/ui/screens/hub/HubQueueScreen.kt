package com.pts.suite.ui.screens.hub

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pts.suite.data.api.DownloadTask
import com.pts.suite.data.api.RetrofitClient
import com.pts.suite.data.api.SystemStats
import com.pts.suite.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun HubQueueScreen(
    tasks: List<DownloadTask>,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var liveTasks by remember { mutableStateOf(tasks) }
    var systemStats by remember { mutableStateOf<SystemStats?>(null) }
    var selectedTab by remember { mutableStateOf("queue") } // queue, single, bulk
    var isSubmitting by remember { mutableStateOf(false) }

    // Single dispatch form state
    var inputTitle by remember { mutableStateOf("") }
    var inputUrl by remember { mutableStateOf("") }
    var inputType by remember { mutableStateOf("Movie") }

    // Edit link modal state
    var editingTask by remember { mutableStateOf<DownloadTask?>(null) }
    var editLinkUrl by remember { mutableStateOf("") }

    // Bulk URL dialog state
    var showBulkDialog by remember { mutableStateOf(false) }

    // Duplicate check state
    var duplicateCheckResult by remember { mutableStateOf(DuplicateCheckResult()) }

    // Keep liveTasks in sync when props update
    LaunchedEffect(tasks) {
        liveTasks = tasks
    }

    // 1500ms Live Polling Loop for /api/downloads and /api/system/stats
    LaunchedEffect(Unit) {
        while (isActive) {
            try {
                val service = RetrofitClient.getService(context)
                val queueRes = service.getDownloadsQueue()
                if (queueRes.isSuccessful && queueRes.body() != null) {
                    liveTasks = queueRes.body()!!.downloads
                }
                val statsRes = service.getSystemStats()
                if (statsRes.isSuccessful && statsRes.body() != null) {
                    systemStats = statsRes.body()
                }
            } catch (e: Exception) {}
            delay(1500)
        }
    }

    // Real-time duplicate checking when single dispatch title/type changes
    LaunchedEffect(inputTitle, inputType) {
        if (inputTitle.length >= 3) {
            delay(400) // debounce
            // Proactively check active queue for duplicates
            val inQueueMatch = liveTasks.find {
                it.title.contains(inputTitle, ignoreCase = true)
            }
            if (inQueueMatch != null) {
                duplicateCheckResult = DuplicateCheckResult(
                    isDuplicate = true,
                    inQueue = true,
                    queueTaskId = inQueueMatch.id,
                    queueStage = inQueueMatch.stage
                )
            } else {
                duplicateCheckResult = DuplicateCheckResult()
            }
        } else {
            duplicateCheckResult = DuplicateCheckResult()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // System telemetry mini-row (polled every 1500ms)
        if (systemStats != null) {
            val stats = systemStats!!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .border(0.5.dp, SketchBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "CPU: ${stats.cpu?.percent ?: 0f}%",
                        color = Graphite200,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "RAM: ${stats.memory?.percent ?: 0}%",
                        color = Graphite200,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "DISK: ${stats.disk?.avail ?: "--"}",
                        color = EmeraldGreen,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "1.5s LIVE",
                    color = EmeraldGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Top Sub-Navigation Tabs & Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val tabs = listOf(
                    "queue" to "Queue (${liveTasks.size})",
                    "single" to "Single Dispatch"
                )
                tabs.forEach { (tabId, label) ->
                    val isSelected = selectedTab == tabId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) EmeraldGreen else DarkSurface)
                            .border(1.dp, if (isSelected) EmeraldGreen else SketchBorder, RoundedCornerShape(20.dp))
                            .clickable { selectedTab = tabId }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) PitchBlack else Graphite200,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Bulk Parser Dialog Trigger
                Button(
                    onClick = { showBulkDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                    border = BorderStroke(1.dp, EmeraldGreen),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("BULK", color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Clear Completed Tasks Button
                IconButton(
                    onClick = {
                        scope.launch {
                            try {
                                val service = RetrofitClient.getService(context)
                                service.clearCompletedDownloads()
                                Toast.makeText(context, "Completed tasks cleared", Toast.LENGTH_SHORT).show()
                                onRefresh()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error clearing: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.CleaningServices, contentDescription = "Clear Completed", tint = Graphite300, modifier = Modifier.size(18.dp))
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
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "DISPATCH NEW TRANSCODE",
                        color = Graphite100,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    // Real-time Duplicate Banner
                    DuplicateCheckBanner(
                        result = duplicateCheckResult,
                        onDismiss = { duplicateCheckResult = DuplicateCheckResult() }
                    )

                    // Media Type Selector (Movie / Series)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Movie", "Series").forEach { type ->
                            val isSelected = inputType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) EmeraldGreenDark else DarkSurfaceElevated)
                                    .border(1.dp, if (isSelected) EmeraldGreen else SketchBorder, RoundedCornerShape(8.dp))
                                    .clickable { inputType = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type,
                                    color = if (isSelected) EmeraldGreen else Graphite300,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

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
                                Toast.makeText(context, "Please enter a download URL", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isSubmitting = true
                            scope.launch {
                                try {
                                    val service = RetrofitClient.getService(context)
                                    val title = if (inputTitle.isNotBlank()) inputTitle else "Download " + System.currentTimeMillis()
                                    val payload = mapOf(
                                        "metadata" to mapOf(
                                            "imdbId" to "pts_" + System.currentTimeMillis(),
                                            "title" to title,
                                            "year" to "2026"
                                        ),
                                        "type" to inputType,
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
                                        Toast.makeText(context, "Dispatch error: ${res.code()}", Toast.LENGTH_SHORT).show()
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
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Text("DISPATCH TO GHA PIPELINE", color = PitchBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }

            "queue" -> {
                // Downloads Queue List
                if (liveTasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CloudQueue, contentDescription = null, tint = Graphite400, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Queue is empty", color = Graphite300, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Dispatch a single transcode or paste bulk URLs", color = Graphite400, fontSize = 12.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(liveTasks, key = { it.id }) { task ->
                            DownloadTaskCard(
                                task = task,
                                onRetry = {
                                    scope.launch {
                                        try {
                                            val service = RetrofitClient.getService(context)
                                            service.retryDownloadTask(task.id)
                                            Toast.makeText(context, "Task restarted", Toast.LENGTH_SHORT).show()
                                            onRefresh()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Retry failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onCancel = {
                                    scope.launch {
                                        try {
                                            val service = RetrofitClient.getService(context)
                                            service.cancelDownloadTask(task.id)
                                            Toast.makeText(context, "Task cancelled", Toast.LENGTH_SHORT).show()
                                            onRefresh()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Cancel failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onEditLink = {
                                    editingTask = task
                                    editLinkUrl = ""
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Edit Link Dialog
    if (editingTask != null) {
        val task = editingTask!!
        AlertDialog(
            onDismissRequest = { editingTask = null },
            title = { Text("Edit Download URL", color = Graphite100, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Task: ${task.title}", color = Graphite300, fontSize = 12.sp)
                    OutlinedTextField(
                        value = editLinkUrl,
                        onValueChange = { editLinkUrl = it },
                        label = { Text("New Direct Download URL") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = SketchBorder,
                            focusedTextColor = Graphite100,
                            unfocusedTextColor = Graphite200
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editLinkUrl.isNotBlank()) {
                            scope.launch {
                                try {
                                    val service = RetrofitClient.getService(context)
                                    service.queueDownload(mapOf("id" to task.id, "downloadLink" to editLinkUrl))
                                    Toast.makeText(context, "Download link updated", Toast.LENGTH_SHORT).show()
                                    editingTask = null
                                    onRefresh()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error updating link: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("UPDATE & RESTART", color = PitchBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingTask = null }) {
                    Text("CANCEL", color = Graphite300)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }

    // Bulk URL Dialog
    if (showBulkDialog) {
        BulkUrlDialog(
            initialTitle = inputTitle,
            onDismiss = { showBulkDialog = false },
            onSubmit = { title, season, items ->
                showBulkDialog = false
                isSubmitting = true
                scope.launch {
                    try {
                        val service = RetrofitClient.getService(context)
                        val payload = mapOf(
                            "type" to "Series",
                            "title" to title,
                            "items" to items.map {
                                mapOf(
                                    "season" to it.season,
                                    "episode" to it.episode,
                                    "epCode" to it.epCode,
                                    "downloadLink" to it.downloadUrl
                                )
                            }
                        )
                        val res = service.queueDownload(payload)
                        if (res.isSuccessful) {
                            Toast.makeText(context, "Enqueued ${items.size} episodes!", Toast.LENGTH_SHORT).show()
                            selectedTab = "queue"
                            onRefresh()
                        } else {
                            Toast.makeText(context, "Bulk enqueue error: ${res.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Bulk error: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isSubmitting = false
                    }
                }
            }
        )
    }
}

@Composable
private fun DownloadTaskCard(
    task: DownloadTask,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    onEditLink: () -> Unit
) {
    // 6-stage lifecycle badge configuration
    val (badgeLabel, badgeColor, badgeBg, badgeIcon) = when (task.stage.lowercase()) {
        "queued" -> Quad("Queued", Graphite300, Graphite800, Icons.Default.HourglassTop)
        "gha_downloading" -> Quad("GHA Downloading", Color(0xFF38BDF8), Color(0x3338BDF8), Icons.Default.Download)
        "gha_compressing" -> Quad("Transcoding x265", Color(0xFFF59E0B), Color(0x33F59E0B), Icons.Default.Settings)
        "gha_uploading_hf", "vps_uploading" -> Quad("Uploading HF", Color(0xFFC084FC), Color(0x33C084FC), Icons.Default.CloudUpload)
        "hf_ready", "pi_downloading", "pi_syncing" -> Quad("Syncing to Pi", Color(0xFF4ADE80), Color(0x3322C55E), Icons.Default.Save)
        "completed", "cleaning_hf" -> Quad("Complete", EmeraldGreen, EmeraldGreenDark, Icons.Default.CheckCircle)
        "failed" -> Quad("Failed", DangerRed, Color(0x33EF4444), Icons.Default.Error)
        else -> Quad(task.stage, Graphite300, Graphite800, Icons.Default.Sync)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .border(1.5.dp, SketchBorder, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Title & Stage Badge Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task.title,
                color = Graphite100,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = badgeBg,
                border = BorderStroke(0.5.dp, badgeColor)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(badgeIcon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = badgeLabel,
                        color = badgeColor,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Progress Bar
        val progressRatio = (task.progress.toFloat() / 100f).coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = { progressRatio },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (task.stage.equals("failed", ignoreCase = true)) DangerRed else EmeraldGreen,
            trackColor = Graphite700
        )

        // Telemetry Row: Speed, ETA, Transferred/Total
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${task.progress}% • ${task.speed}",
                color = Graphite200,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "ETA: ${task.eta}",
                color = Graphite300,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = "${task.transferred} / ${task.total}",
                color = Graphite400,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Message or Error Details
        if (!task.error.isNullOrBlank()) {
            Text(
                text = "Error: ${task.error}",
                color = DangerRed,
                fontSize = 11.sp,
                maxLines = 2
            )
        } else if (task.message.isNotBlank()) {
            Text(
                text = task.message,
                color = Graphite400,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Task Action Buttons: Edit Link, Retry, Delete
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Edit Link Button
            TextButton(
                onClick = onEditLink,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Edit Link", color = Color(0xFF38BDF8), fontSize = 11.sp)
            }

            // Retry Button (if failed or active)
            if (task.stage.equals("failed", ignoreCase = true)) {
                TextButton(
                    onClick = onRetry,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Retry", color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Cancel / Delete Button
            TextButton(
                onClick = onCancel,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Delete", color = DangerRed, fontSize = 11.sp)
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

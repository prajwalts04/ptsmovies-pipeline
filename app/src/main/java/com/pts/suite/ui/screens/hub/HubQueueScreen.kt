package com.pts.suite.ui.screens.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.pts.suite.data.api.DispatchLinkRequest
import com.pts.suite.data.api.DownloadTask
import com.pts.suite.data.api.RetrofitClient
import com.pts.suite.ui.components.DockTabItem
import com.pts.suite.ui.components.DynamicBottomDock
import com.pts.suite.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HubQueueScreen(
    tasks: List<DownloadTask>,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf("queue") }
    var inputUrl by remember { mutableStateOf("") }
    var inputTitle by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val hubTabs = listOf(
        DockTabItem("queue", "Transcode Queue", Icons.Default.Bolt),
        DockTabItem("add", "Dispatch Link", Icons.Default.AddLink),
        DockTabItem("runners", "GHA Runners", Icons.Default.CloudSync)
    )

    Scaffold(
        bottomBar = {
            DynamicBottomDock(
                tabs = hubTabs,
                selectedTabId = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        containerColor = PitchBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "GHA TRANSCODE PIPELINE",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Graphite100,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (selectedTab == "add") {
                // Dispatch New Transcode Link Form
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurface)
                        .border(1.dp, SketchBorder, RoundedCornerShape(10.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Add Direct Video Link", color = Graphite100, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = inputTitle,
                        onValueChange = { inputTitle = it },
                        label = { Text("Title (e.g. Inception 2010)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Graphite100, unfocusedBorderColor = SketchBorder)
                    )

                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        label = { Text("Direct HTTP / Magnet Download Link") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Graphite100, unfocusedBorderColor = SketchBorder)
                    )

                    Button(
                        onClick = {
                            if (inputUrl.isNotBlank() && inputTitle.isNotBlank()) {
                                isSubmitting = true
                                scope.launch {
                                    RetrofitClient.getService(context).dispatchLink(
                                        DispatchLinkRequest(downloadUrl = inputUrl, title = inputTitle)
                                    )
                                    inputUrl = ""
                                    inputTitle = ""
                                    isSubmitting = false
                                    selectedTab = "queue"
                                    onRefresh()
                                }
                            }
                        },
                        enabled = !isSubmitting && inputUrl.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Graphite100, contentColor = PitchBlack)
                    ) {
                        Text(if (isSubmitting) "DISPATCHING..." else "DISPATCH TO RUNNER", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Queue Task List
                if (tasks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active transcode jobs in queue.", color = Graphite400, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(tasks) { task ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurface)
                                    .border(1.dp, SketchBorder, RoundedCornerShape(8.dp))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = task.title, color = Graphite100, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(
                                        text = task.stage.uppercase(),
                                        color = when (task.stage) {
                                            "completed" -> EmeraldGreen
                                            "failed" -> DangerRed
                                            else -> GoldenYellow
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = { task.progress / 100f },
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = EmeraldGreen,
                                    trackColor = Graphite800
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "${task.progress}% • ${task.speed}", color = Graphite300, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    Text(text = "ETA: ${task.eta}", color = Graphite400, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

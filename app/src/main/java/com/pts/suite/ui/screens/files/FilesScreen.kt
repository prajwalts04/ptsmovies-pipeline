package com.pts.suite.ui.screens.files

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pts.suite.data.api.FileItem
import com.pts.suite.ui.components.DockTabItem
import com.pts.suite.ui.components.DynamicBottomDock
import com.pts.suite.ui.theme.*

@Composable
fun FilesScreen(
    currentPath: String,
    files: List<FileItem>,
    onNavigateDir: (String) -> Unit,
    onBackDir: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("browse") }

    val fileTabs = listOf(
        DockTabItem("browse", "Browse (/Data)", Icons.Default.Folder),
        DockTabItem("recent", "Recent Files", Icons.Default.Schedule),
        DockTabItem("uploads", "Uploads", Icons.Default.UploadFile)
    )

    Scaffold(
        bottomBar = {
            DynamicBottomDock(
                tabs = fileTabs,
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Path Navigation Breadcrumb
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentPath != "/Data") {
                    IconButton(onClick = onBackDir, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Up", tint = Graphite100)
                    }
                }
                Text(
                    text = currentPath,
                    color = EmeraldGreen,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            // Directory Listing
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(files) { file ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurface)
                            .border(1.dp, SketchBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                if (file.isDirectory) {
                                    onNavigateDir(file.path)
                                }
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                                contentDescription = null,
                                tint = if (file.isDirectory) EmeraldGreen else Graphite300,
                                modifier = Modifier.size(20.dp)
                            )

                            Column {
                                Text(text = file.name, color = Graphite100, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                if (!file.isDirectory) {
                                    Text(text = file.sizeFormatted, color = Graphite400, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }

                        if (file.isDirectory) {
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Graphite500)
                        }
                    }
                }
            }
        }
    }
}

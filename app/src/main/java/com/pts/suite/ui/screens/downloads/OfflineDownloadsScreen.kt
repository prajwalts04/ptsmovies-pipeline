package com.pts.suite.ui.screens.downloads

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
import com.pts.suite.data.db.AppDatabase
import com.pts.suite.data.db.LocalDownloadEntity
import com.pts.suite.data.download.OfflineDownloadManager
import com.pts.suite.ui.theme.*

@Composable
fun OfflineDownloadsScreen(
    onPlayOfflineFile: (filePath: String, title: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    var downloads by remember { mutableStateOf<List<LocalDownloadEntity>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("Series") } // "Movies" or "Series"
    var expandedShowTitle by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        downloads = db.mediaDao().getAllDownloads()
    }

    val downloadedMovies = remember(downloads) {
        downloads.filter { it.mediaType == "Movie" }
    }

    val downloadedSeriesMap = remember(downloads) {
        downloads.filter { it.mediaType == "Series" }.groupBy { it.showTitle }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Graphite100)
            }
            Text(
                text = "OFFLINE DOWNLOADS",
                color = Graphite100,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }

        // Category Switcher: Movies vs Series (Netflix / Hotstar style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurface)
                .border(1.dp, SketchBorder, RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selectedCategory == "Series") Graphite100 else DarkSurface)
                    .clickable { selectedCategory = "Series"; expandedShowTitle = null }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Series (${downloadedSeriesMap.size})",
                    color = if (selectedCategory == "Series") PitchBlack else Graphite300,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selectedCategory == "Movies") Graphite100 else DarkSurface)
                    .clickable { selectedCategory = "Movies"; expandedShowTitle = null }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Movies (${downloadedMovies.size})",
                    color = if (selectedCategory == "Movies") PitchBlack else Graphite300,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        // Content Area
        if (selectedCategory == "Movies") {
            if (downloadedMovies.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No offline movies downloaded yet.", color = Graphite400, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(downloadedMovies) { movie ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .border(1.dp, SketchBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = movie.showTitle, color = Graphite100, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Downloaded • ${(movie.totalBytes / (1024 * 1024))} MB", color = EmeraldGreen, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(onClick = { onPlayOfflineFile(movie.localFilePath, movie.showTitle) }) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play Offline", tint = EmeraldGreen)
                                }
                                IconButton(onClick = {
                                    OfflineDownloadManager.deleteDownloadedFile(context, movie.id, movie.localFilePath)
                                    downloads = downloads.filter { it.id != movie.id }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete File", tint = DangerRed)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Series Hierarchy (Hotstar / Netflix nested folder view)
            if (downloadedSeriesMap.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No offline TV series downloaded yet.", color = Graphite400, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    downloadedSeriesMap.forEach { (showTitle, episodes) ->
                        val isExpanded = expandedShowTitle == showTitle

                        item {
                            // Show Folder Card
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurface)
                                    .border(1.dp, SketchBorder, RoundedCornerShape(8.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedShowTitle = if (isExpanded) null else showTitle }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = EmeraldGreen,
                                            modifier = Modifier.size(24.dp)
                                        )

                                        Column {
                                            Text(text = showTitle, color = Graphite100, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text(text = "${episodes.size} Episodes Downloaded", color = Graphite400, fontSize = 11.sp)
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = {
                                            OfflineDownloadManager.deleteEntireSeries(context, showTitle)
                                            downloads = downloads.filter { it.showTitle != showTitle }
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Entire Show", tint = Graphite400, modifier = Modifier.size(18.dp))
                                        }
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = Graphite300
                                        )
                                    }
                                }

                                // Expanded Episode List inside Folder
                                if (isExpanded) {
                                    Divider(color = SketchBorder, thickness = 1.dp)

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        episodes.forEach { ep ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(DarkSurfaceElevated)
                                                    .border(1.dp, SketchBorder, RoundedCornerShape(6.dp))
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "${ep.epCode} - ${ep.episodeTitle}",
                                                        color = Graphite100,
                                                        fontSize = 12.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = "Ready to play offline",
                                                        color = EmeraldGreen,
                                                        fontSize = 10.5.sp,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }

                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    IconButton(onClick = { onPlayOfflineFile(ep.localFilePath, "${ep.showTitle} ${ep.epCode}") }) {
                                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = EmeraldGreen)
                                                    }
                                                    IconButton(onClick = {
                                                        OfflineDownloadManager.deleteDownloadedFile(context, ep.id, ep.localFilePath)
                                                        downloads = downloads.filter { it.id != ep.id }
                                                    }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Delete Episode", tint = DangerRed)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

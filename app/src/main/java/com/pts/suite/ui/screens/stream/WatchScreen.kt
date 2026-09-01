package com.pts.suite.ui.screens.stream

import android.app.Activity
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.pts.suite.data.api.EpisodeItem
import com.pts.suite.data.api.MovieItem
import com.pts.suite.data.api.RetrofitClient
import com.pts.suite.data.api.SeriesItem
import com.pts.suite.data.download.OfflineDownloadManager
import com.pts.suite.ui.theme.*
import java.net.URLEncoder

@OptIn(UnstableApi::class)
@Composable
fun WatchScreen(
    movie: MovieItem?,
    series: SeriesItem?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var browseSeason by remember { mutableIntStateOf(1) }
    var playingSeason by remember { mutableIntStateOf(1) }
    var playingEpisode by remember { mutableIntStateOf(1) }

    // Initialize first season
    LaunchedEffect(series) {
        if (series != null) {
            val firstSeasonKey = series.seasons.keys.mapNotNull { it.toIntOrNull() }.minOrNull() ?: 1
            browseSeason = firstSeasonKey
            playingSeason = firstSeasonKey
            playingEpisode = series.seasons[firstSeasonKey.toString()]?.firstOrNull()?.episode ?: 1
        }
    }

    // Determine current video stream URL
    val currentStreamUrl = remember(movie, series, playingSeason, playingEpisode) {
        val token = RetrofitClient.getAuthToken(context) ?: ""
        val filePath = if (movie != null) {
            movie.filePath
        } else if (series != null) {
            val epList = series.seasons[playingSeason.toString()] ?: emptyList()
            epList.find { it.episode == playingEpisode }?.filePath ?: epList.firstOrNull()?.filePath ?: ""
        } else ""

        val serverUrl = RetrofitClient.getServerUrl(context).trimEnd('/')
        "$serverUrl/api/stream/video?file=${URLEncoder.encode(filePath, "UTF-8")}&token=${URLEncoder.encode(token, "UTF-8")}"
    }

    // Build ExoPlayer instance
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build()
    }

    LaunchedEffect(currentStreamUrl) {
        if (currentStreamUrl.isNotBlank()) {
            val mediaItem = MediaItem.fromUri(Uri.parse(currentStreamUrl))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = false // User requested: do NOT auto-play until play is tapped!
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
    ) {
        // Top Nav with Back Button and Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Graphite100)
            }
            Text(
                text = movie?.title ?: series?.title ?: "Playing Media",
                color = Graphite100,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        // 1st Half: Native Hardware-Accelerated ExoPlayer View
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(PitchBlack)
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2nd Half: Title, Metadata, Offline Download & Episode Explorer
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = movie?.title ?: series?.title ?: "",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Graphite100
                        )
                        Text(
                            text = if (movie != null) "${movie.year ?: ""} • ${movie.size ?: ""}" else "Series • ${series?.totalEpisodes ?: 0} Episodes",
                            fontSize = 12.sp,
                            color = Graphite400
                        )
                    }

                    // Download Movie / Episode for Offline
                    Button(
                        onClick = {
                            if (movie != null) {
                                OfflineDownloadManager.downloadMovie(context, movie)
                            } else if (series != null) {
                                val epList = series.seasons[playingSeason.toString()] ?: emptyList()
                                val ep = epList.find { it.episode == playingEpisode } ?: epList.firstOrNull()
                                if (ep != null) {
                                    OfflineDownloadManager.downloadEpisode(context, series.title, series.poster ?: "", ep)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = Graphite100),
                        shape = RoundedCornerShape(8.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SketchBorder))
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldGreen)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Series Season Selector & Episodes List
            if (series != null && series.seasons.isNotEmpty()) {
                item {
                    Text(
                        text = "SEASONS & EPISODES",
                        color = Graphite400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Season Tabs Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val seasonKeys = series.seasons.keys.mapNotNull { it.toIntOrNull() }.sorted()
                        items(seasonKeys) { seasonNum ->
                            val isBrowsing = browseSeason == seasonNum
                            val epCount = series.seasons[seasonNum.toString()]?.size ?: 0

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isBrowsing) Graphite100 else DarkSurface)
                                    .border(1.dp, if (isBrowsing) Graphite100 else SketchBorder, RoundedCornerShape(6.dp))
                                    .clickable { browseSeason = seasonNum }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Season $seasonNum ($epCount)",
                                    color = if (isBrowsing) PitchBlack else Graphite300,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Episode List for Selected Season
                val episodesForSeason = series.seasons[browseSeason.toString()] ?: emptyList()
                items(episodesForSeason) { ep ->
                    val isCurrent = playingSeason == ep.season && playingEpisode == ep.episode

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCurrent) DarkSurfaceElevated else DarkSurface)
                            .border(1.dp, if (isCurrent) EmeraldGreen else SketchBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                playingSeason = ep.season
                                playingEpisode = ep.episode
                                exoPlayer.playWhenReady = true // Start playback on explicit episode tap!
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isCurrent) EmeraldGreenDark else Graphite800),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${ep.episode}",
                                    color = if (isCurrent) EmeraldGreen else Graphite200,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column {
                                Text(
                                    text = ep.fileName.substringBeforeLast('.'),
                                    color = Graphite100,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${ep.epCode} • ${ep.size}",
                                    color = Graphite400,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Right side: Active playing indicator or download button
                        if (isCurrent) {
                            Text("PLAYING", color = EmeraldGreen, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        } else {
                            IconButton(onClick = {
                                OfflineDownloadManager.downloadEpisode(context, series.title, series.poster ?: "", ep)
                            }) {
                                Icon(Icons.Default.Download, contentDescription = "Download Episode", tint = Graphite400, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

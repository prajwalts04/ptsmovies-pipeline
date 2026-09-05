package com.pts.suite.ui.screens.stream

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.pts.suite.data.api.EpisodeItem
import com.pts.suite.data.api.MovieItem
import com.pts.suite.data.api.RetrofitClient
import com.pts.suite.data.api.SeriesItem
import com.pts.suite.data.download.OfflineDownloadManager
import com.pts.suite.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.URLEncoder

@OptIn(UnstableApi::class)
@Composable
fun WatchScreen(
    movie: MovieItem?,
    series: SeriesItem?,
    onBack: () -> Unit,
    onToggleWatchlist: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    var browseSeason by remember { mutableIntStateOf(1) }
    var playingSeason by remember { mutableIntStateOf(1) }
    var playingEpisode by remember { mutableIntStateOf(1) }

    // Playback state
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(1L) }
    var bufferedPositionMs by remember { mutableLongStateOf(0L) }
    var isBuffering by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var isFullscreen by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var doubleTapFeedback by remember { mutableStateOf<String?>(null) }
    var isInWatchlistState by remember { mutableStateOf(false) }

    // Initialize first season
    LaunchedEffect(series) {
        if (series != null) {
            val firstSeasonKey = series.seasons.keys.mapNotNull { it.toIntOrNull() }.minOrNull() ?: 1
            browseSeason = firstSeasonKey
            playingSeason = firstSeasonKey
            playingEpisode = series.seasons[firstSeasonKey.toString()]?.firstOrNull()?.episode ?: 1
        }
    }

    // Determine current episode item
    val currentEpisodeItem = remember(series, playingSeason, playingEpisode) {
        if (series != null) {
            val epList = series.seasons[playingSeason.toString()] ?: emptyList()
            epList.find { it.episode == playingEpisode } ?: epList.firstOrNull()
        } else null
    }

    // Check if next episode exists
    val nextEpisodeItem: Pair<Int, EpisodeItem>? = remember(series, playingSeason, playingEpisode) {
        if (series == null) null
        else {
            val currentSeasonList = series.seasons[playingSeason.toString()] ?: emptyList()
            val nextInSeason = currentSeasonList.find { it.episode == playingEpisode + 1 }
            if (nextInSeason != null) {
                playingSeason to nextInSeason
            } else {
                val nextSeasonNum = playingSeason + 1
                val nextSeasonList = series.seasons[nextSeasonNum.toString()]
                val firstInNext = nextSeasonList?.firstOrNull()
                if (firstInNext != null) nextSeasonNum to firstInNext else null
            }
        }
    }

    // Determine current video stream URL
    val currentStreamUrl = remember(movie, series, playingSeason, playingEpisode) {
        val token = RetrofitClient.getAuthToken(context) ?: ""
        val filePath = if (movie != null) {
            movie.filePath
        } else if (currentEpisodeItem != null) {
            currentEpisodeItem.filePath
        } else ""

        val serverUrl = RetrofitClient.getServerUrl(context).trimEnd('/')
        "$serverUrl/api/stream/video?file=${URLEncoder.encode(filePath, "UTF-8")}&token=${URLEncoder.encode(token, "UTF-8")}"
    }

    // ExoPlayer setup
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }
                override fun onPlaybackStateChanged(state: Int) {
                    isBuffering = state == Player.STATE_BUFFERING
                    if (state == Player.STATE_READY) {
                        durationMs = duration.coerceAtLeast(1L)
                    }
                }
            })
        }
    }

    // Prepare stream
    LaunchedEffect(currentStreamUrl) {
        if (currentStreamUrl.isNotBlank()) {
            val mediaItem = MediaItem.fromUri(Uri.parse(currentStreamUrl))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    // Time ticker for position & buffered position
    LaunchedEffect(exoPlayer) {
        while (isActive) {
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            bufferedPositionMs = exoPlayer.bufferedPosition.coerceAtLeast(0L)
            durationMs = exoPlayer.duration.coerceAtLeast(1L)
            delay(250)
        }
    }

    // Auto-hide controls overlay after 3.5s inactivity
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(3500)
            showControls = false
        }
    }

    // 5-second progress heartbeat to /api/progress/update
    LaunchedEffect(movie, series, playingSeason, playingEpisode) {
        val mediaId = movie?.id ?: series?.id ?: return@LaunchedEffect
        val mediaType = if (movie != null) "Movie" else "Series"
        val filePath = movie?.filePath ?: currentEpisodeItem?.filePath ?: ""
        val epCode = currentEpisodeItem?.epCode ?: ""

        while (isActive) {
            delay(5000)
            val posSec = (exoPlayer.currentPosition / 1000L).coerceAtLeast(0L)
            val durSec = (exoPlayer.duration / 1000L).coerceAtLeast(1L)
            if (durSec > 10 && posSec > 0) {
                try {
                    val service = RetrofitClient.getService(context)
                    val heartbeatPayload = mapOf(
                        "mediaId" to mediaId,
                        "type" to mediaType,
                        "season" to playingSeason,
                        "episode" to playingEpisode,
                        "epCode" to epCode,
                        "filePath" to filePath,
                        "positionSeconds" to posSec,
                        "durationSeconds" to durSec
                    )
                    service.toggleWatchlist(mapOf("heartbeat" to heartbeatPayload)) // backend heartbeat ping
                } catch (e: Exception) {}
            }
        }
    }

    // Fullscreen orientation handler
    LaunchedEffect(isFullscreen) {
        activity?.requestedOrientation = if (isFullscreen) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    // Clean up
    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            exoPlayer.release()
        }
    }

    // Back gesture: exit fullscreen first or go back to catalog
    BackHandler(enabled = true) {
        if (isFullscreen) {
            isFullscreen = false
        } else {
            onBack()
        }
    }

    fun seekBy(deltaSeconds: Int) {
        val newPos = (exoPlayer.currentPosition + deltaSeconds * 1000L).coerceIn(0L, durationMs)
        exoPlayer.seekTo(newPos)
        currentPositionMs = newPos
        doubleTapFeedback = if (deltaSeconds > 0) "+${deltaSeconds}s" else "${deltaSeconds}s"
        scope.launch {
            delay(800)
            doubleTapFeedback = null
        }
    }

    fun formatTime(ms: Long): String {
        val totalSec = (ms / 1000L).coerceAtLeast(0L)
        val hours = totalSec / 3600
        val minutes = (totalSec % 3600) / 60
        val seconds = totalSec % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
    ) {
        // Player Surface + Custom Overlay Controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isFullscreen) Modifier.fillMaxSize()
                    else Modifier.aspectRatio(16f / 9f)
                )
                .background(PitchBlack)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            showControls = !showControls
                        },
                        onDoubleTap = { offset ->
                            val width = size.width
                            if (offset.x < width / 2) {
                                seekBy(-10) // Left double tap: -10s
                            } else {
                                seekBy(10)  // Right double tap: +10s
                            }
                        }
                    )
                }
        ) {
            // Android Media3 PlayerView (Native ExoPlayer surface without default controllers)
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Buffering Spinner
            if (isBuffering) {
                CircularProgressIndicator(
                    color = EmeraldGreen,
                    strokeWidth = 3.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }

            // Double-Tap Seek Ripple Feedback
            AnimatedVisibility(
                visible = doubleTapFeedback != null,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(250)),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Surface(
                    shape = CircleShape,
                    color = DarkSurface.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, EmeraldGreen)
                ) {
                    Text(
                        text = doubleTapFeedback ?: "",
                        color = EmeraldGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            // Custom Compose Controls Overlay
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    PitchBlack.copy(alpha = 0.85f),
                                    Color.Transparent,
                                    PitchBlack.copy(alpha = 0.92f)
                                )
                            )
                        )
                ) {
                    // TOP BAR OVERLAY: Back, Title, Speed, Watchlist, Fullscreen
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(onClick = {
                                if (isFullscreen) isFullscreen = false else onBack()
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Graphite100)
                            }

                            Column(modifier = Modifier.padding(start = 4.dp)) {
                                Text(
                                    text = movie?.title ?: series?.title ?: "Playing Media",
                                    color = Graphite100,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (currentEpisodeItem != null) {
                                    Text(
                                        text = "${currentEpisodeItem.epCode} • ${currentEpisodeItem.fileName.substringBeforeLast('.')}",
                                        color = EmeraldGreen,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Playback Speed Selector Button
                            Box {
                                TextButton(onClick = { showSpeedMenu = true }) {
                                    Text(
                                        text = "${playbackSpeed}x",
                                        color = EmeraldGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                DropdownMenu(
                                    expanded = showSpeedMenu,
                                    onDismissRequest = { showSpeedMenu = false },
                                    modifier = Modifier.background(DarkSurfaceElevated).border(1.dp, SketchBorder, RoundedCornerShape(8.dp))
                                ) {
                                    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                                    speeds.forEach { speed ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "${speed}x",
                                                    color = if (playbackSpeed == speed) EmeraldGreen else Graphite200,
                                                    fontWeight = if (playbackSpeed == speed) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                playbackSpeed = speed
                                                exoPlayer.playbackParameters = PlaybackParameters(speed)
                                                showSpeedMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Watchlist Toggle Button
                            IconButton(onClick = {
                                isInWatchlistState = !isInWatchlistState
                                val id = movie?.id ?: series?.id ?: ""
                                val type = if (movie != null) "Movie" else "Series"
                                onToggleWatchlist(id, type)
                            }) {
                                Icon(
                                    imageVector = if (isInWatchlistState) Icons.Default.BookmarkCheck else Icons.Default.BookmarkBorder,
                                    contentDescription = "Watchlist",
                                    tint = if (isInWatchlistState) EmeraldGreen else Graphite300
                                )
                            }

                            // Fullscreen Toggle
                            IconButton(onClick = { isFullscreen = !isFullscreen }) {
                                Icon(
                                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = Graphite100
                                )
                            }
                        }
                    }

                    // CENTER PLAY/PAUSE & 10s SEEK CONTROLS
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rewind 10s
                        IconButton(
                            onClick = { seekBy(-10) },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(DarkSurface.copy(alpha = 0.6f))
                        ) {
                            Icon(Icons.Default.Replay10, contentDescription = "Rewind 10s", tint = Graphite100, modifier = Modifier.size(26.dp))
                        }

                        // Center Play / Pause
                        IconButton(
                            onClick = {
                                if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = PitchBlack,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Fast Forward 10s
                        IconButton(
                            onClick = { seekBy(10) },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(DarkSurface.copy(alpha = 0.6f))
                        ) {
                            Icon(Icons.Default.Forward10, contentDescription = "Forward 10s", tint = Graphite100, modifier = Modifier.size(26.dp))
                        }

                        // Next Episode Button (Series only)
                        if (nextEpisodeItem != null) {
                            IconButton(
                                onClick = {
                                    val (nextS, nextEp) = nextEpisodeItem
                                    playingSeason = nextS
                                    playingEpisode = nextEp.episode
                                    browseSeason = nextS
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurface.copy(alpha = 0.6f))
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Next Episode", tint = EmeraldGreen, modifier = Modifier.size(26.dp))
                            }
                        }
                    }

                    // BOTTOM SCRUBBER & TIMESTAMPS OVERLAY
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        // Dual-Track Scrubber (Buffered + Current Progress)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Track Background
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Graphite600)
                            ) {
                                // Buffered Progress Track
                                val bufferRatio = (bufferedPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(bufferRatio)
                                        .fillMaxHeight()
                                        .background(Graphite400.copy(alpha = 0.6f))
                                )
                            }

                            // Interactive Position Slider
                            Slider(
                                value = currentPositionMs.toFloat().coerceIn(0f, durationMs.toFloat()),
                                onValueChange = { newPos ->
                                    currentPositionMs = newPos.toLong()
                                    exoPlayer.seekTo(currentPositionMs)
                                },
                                valueRange = 0f..durationMs.toFloat(),
                                colors = SliderDefaults.colors(
                                    thumbColor = EmeraldGreen,
                                    activeTrackColor = EmeraldGreen,
                                    inactiveTrackColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Time Display: 00:00 / 00:00
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatTime(currentPositionMs),
                                color = Graphite200,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = formatTime(durationMs),
                                color = Graphite400,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Details and Episode Explorer (visible when not in fullscreen mode)
        if (!isFullscreen) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Media Title & Action Buttons
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = movie?.title ?: series?.title ?: "",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Graphite100
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (!movie?.rating.isNullOrBlank() || !series?.rating.isNullOrBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = GoldenYellow, modifier = Modifier.size(13.dp))
                                    Text(
                                        text = " ${movie?.rating ?: series?.rating}",
                                        color = GoldenYellow,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            val year = movie?.year ?: series?.year
                            if (!year.isNullOrBlank()) {
                                Text(text = year, color = Graphite400, fontSize = 12.sp)
                            }

                            val typeTag = if (movie != null) "Movie" else "Series"
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = DarkSurfaceElevated,
                                border = BorderStroke(0.5.dp, SketchBorder)
                            ) {
                                Text(
                                    text = typeTag,
                                    color = EmeraldGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            // Offline Download Button
                            IconButton(onClick = {
                                if (movie != null) {
                                    OfflineDownloadManager.downloadMovie(context, movie)
                                    Toast.makeText(context, "Download started for ${movie.title}", Toast.LENGTH_SHORT).show()
                                } else if (currentEpisodeItem != null && series != null) {
                                    OfflineDownloadManager.downloadEpisode(context, series.title, series.poster ?: "", currentEpisodeItem)
                                    Toast.makeText(context, "Download started for ${currentEpisodeItem.epCode}", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.Default.Download, contentDescription = "Download Offline", tint = EmeraldGreen)
                            }
                        }

                        val description = movie?.description ?: series?.description
                        if (!description.isNullOrBlank()) {
                            Text(
                                text = description,
                                color = Graphite300,
                                fontSize = 12.5.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // Series Season & Episode Picker
                if (series != null && series.seasons.isNotEmpty()) {
                    item {
                        Text(
                            text = "SEASONS & EPISODES",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Graphite200
                        )
                    }

                    // Season selection chips
                    item {
                        val seasonKeys = series.seasons.keys.mapNotNull { it.toIntOrNull() }.sorted()
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(seasonKeys) { sNum ->
                                val isSelected = browseSeason == sNum
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) EmeraldGreen else DarkSurface)
                                        .border(1.dp, if (isSelected) EmeraldGreen else SketchBorder, RoundedCornerShape(8.dp))
                                        .clickable { browseSeason = sNum }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Season $sNum",
                                        color = if (isSelected) PitchBlack else Graphite200,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Episodes list in current season
                    val epList = series.seasons[browseSeason.toString()] ?: emptyList()
                    items(epList, key = { it.id }) { ep ->
                        val isPlayingThis = playingSeason == browseSeason && playingEpisode == ep.episode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPlayingThis) DarkSurfaceElevated else DarkSurface)
                                .border(1.dp, if (isPlayingThis) EmeraldGreen else SketchBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    playingSeason = browseSeason
                                    playingEpisode = ep.episode
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isPlayingThis) EmeraldGreen else Graphite800),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingThis) Icons.Default.PlayArrow else Icons.Default.Movie,
                                        contentDescription = null,
                                        tint = if (isPlayingThis) PitchBlack else Graphite300,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "${ep.epCode} • ${ep.fileName.substringBeforeLast('.')}",
                                        fontSize = 12.5.sp,
                                        fontWeight = if (isPlayingThis) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isPlayingThis) EmeraldGreen else Graphite100,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = ep.size,
                                        fontSize = 11.sp,
                                        color = Graphite400
                                    )
                                }
                            }

                            // Episode Download Icon
                            IconButton(onClick = {
                                OfflineDownloadManager.downloadEpisode(context, series.title, series.poster ?: "", ep)
                                Toast.makeText(context, "Download queued: ${ep.epCode}", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Download, contentDescription = "Download Episode", tint = Graphite300, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

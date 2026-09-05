package com.pts.suite.ui.screens.files

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.pts.suite.data.api.FileItem
import com.pts.suite.data.api.MovieItem
import com.pts.suite.data.api.RetrofitClient
import com.pts.suite.data.download.OfflineDownloadManager
import com.pts.suite.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.net.URLEncoder
import java.util.Locale

@OptIn(UnstableApi::class)
@Composable
fun MediaPreviewDialog(
    fileItem: FileItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val token = remember { RetrofitClient.getAuthToken(context) ?: "" }
    val serverUrl = remember { RetrofitClient.getServerUrl(context).trimEnd('/') }

    val encodedPath = remember(fileItem.path) {
        URLEncoder.encode(fileItem.path, "UTF-8")
    }

    val streamUrl = remember(fileItem.path, serverUrl, token) {
        "$serverUrl/api/stream/video?file=$encodedPath&token=${URLEncoder.encode(token, "UTF-8")}"
    }

    val ext = remember(fileItem.name) {
        fileItem.name.substringAfterLast('.', "").lowercase(Locale.ROOT)
    }

    val isVideo = ext in setOf("mp4", "mkv", "avi", "webm", "mov", "m4v")
    val isAudio = ext in setOf("mp3", "flac", "wav", "aac", "m4a", "ogg")
    val isImage = ext in setOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "svg")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PitchBlack.copy(alpha = 0.95f))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.5.dp, SketchBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Bar: File Name, Size, Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = fileItem.name,
                            color = Graphite100,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                        Text(
                            text = "${fileItem.formattedSize} • ${fileItem.permissions}",
                            color = Graphite400,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Download Offline Button
                        IconButton(
                            onClick = {
                                val dummyMovie = MovieItem(
                                    id = "file_" + System.currentTimeMillis(),
                                    title = fileItem.name,
                                    year = "2026",
                                    rating = null,
                                    poster = null,
                                    description = null,
                                    genres = emptyList(),
                                    filePath = fileItem.path,
                                    fileName = fileItem.name,
                                    size = fileItem.formattedSize
                                )
                                OfflineDownloadManager.downloadMovie(context, dummyMovie)
                                Toast.makeText(context, "Download started for ${fileItem.name}", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Download", tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                        }

                        // Close Button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Graphite300, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Media Content Surface
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PitchBlack)
                        .border(1.dp, SketchBorder, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isImage -> {
                            AsyncImage(
                                model = streamUrl,
                                contentDescription = fileItem.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        isVideo -> {
                            VideoPreviewPlayer(streamUrl = streamUrl)
                        }

                        isAudio -> {
                            AudioPreviewPlayer(streamUrl = streamUrl, fileName = fileItem.name)
                        }

                        else -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = Graphite400, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "Preview not available for this file type (${ext.uppercase()})", color = Graphite300, fontSize = 12.sp)
                                Text(text = "Use text editor or download to view", color = Graphite400, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoPreviewPlayer(streamUrl: String) {
    val context = LocalContext.current
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build()
    }

    LaunchedEffect(streamUrl) {
        val mediaItem = MediaItem.fromUri(Uri.parse(streamUrl))
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

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

@Composable
private fun AudioPreviewPlayer(streamUrl: String, fileName: String) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(1L) }

    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        durationMs = duration.coerceAtLeast(1L)
                    }
                }
            })
        }
    }

    LaunchedEffect(streamUrl) {
        val mediaItem = MediaItem.fromUri(Uri.parse(streamUrl))
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    LaunchedEffect(exoPlayer) {
        while (isActive) {
            currentPosMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            durationMs = exoPlayer.duration.coerceAtLeast(1L)
            delay(250)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Audiotrack,
            contentDescription = null,
            tint = Color(0xFFC084FC),
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = fileName,
            color = Graphite100,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )

        // Progress Slider
        Slider(
            value = currentPosMs.toFloat().coerceIn(0f, durationMs.toFloat()),
            onValueChange = { newPos ->
                currentPosMs = newPos.toLong()
                exoPlayer.seekTo(currentPosMs)
            },
            valueRange = 0f..durationMs.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = EmeraldGreen,
                activeTrackColor = EmeraldGreen,
                inactiveTrackColor = Graphite700
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Play/Pause Button
        IconButton(
            onClick = {
                if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
            },
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(EmeraldGreen)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = PitchBlack,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

package com.pts.suite.data.download

import android.content.Context
import android.os.Environment
import com.pts.suite.data.api.EpisodeItem
import com.pts.suite.data.api.MovieItem
import com.pts.suite.data.api.RetrofitClient
import com.pts.suite.data.db.AppDatabase
import com.pts.suite.data.db.LocalDownloadEntity
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URLEncoder

object OfflineDownloadManager {

    private val activeJobs = mutableMapOf<String, Job>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private fun getBaseDownloadDir(): File {
        val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val ptsDir = File(publicDir, "PTS")
        if (!ptsDir.exists()) ptsDir.mkdirs()
        return ptsDir
    }

    // Start or Restart Movie Download
    fun downloadMovie(context: Context, movie: MovieItem) {
        val db = AppDatabase.getDatabase(context)
        val targetDir = File(getBaseDownloadDir(), "Movies")
        if (!targetDir.exists()) targetDir.mkdirs()

        val safeTitle = movie.title.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
        val targetFile = File(targetDir, "$safeTitle.mp4")

        val entity = LocalDownloadEntity(
            id = movie.id,
            mediaType = "Movie",
            showTitle = movie.title,
            episodeTitle = movie.title,
            localFilePath = targetFile.absolutePath,
            posterUrl = movie.poster ?: "",
            status = "DOWNLOADING"
        )

        scope.launch {
            db.mediaDao().insertOrUpdateDownload(entity)
            startFileDownload(context, movie.id, movie.filePath, targetFile)
        }
    }

    // Start or Restart Series Episode Download
    fun downloadEpisode(context: Context, showTitle: String, posterUrl: String, episode: EpisodeItem) {
        val db = AppDatabase.getDatabase(context)
        val safeShow = showTitle.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
        val seriesDir = File(getBaseDownloadDir(), "Series/$safeShow/Season ${String.format("%02d", episode.season)}")
        if (!seriesDir.exists()) seriesDir.mkdirs()

        val safeEp = "${episode.epCode}_${episode.fileName.replace("[^a-zA-Z0-9.-]".toRegex(), "_")}"
        val targetFile = File(seriesDir, safeEp)

        val entity = LocalDownloadEntity(
            id = episode.id,
            mediaType = "Series",
            showTitle = showTitle,
            seasonNumber = episode.season,
            episodeNumber = episode.episode,
            epCode = episode.epCode,
            episodeTitle = episode.fileName.substringBeforeLast('.'),
            localFilePath = targetFile.absolutePath,
            posterUrl = posterUrl,
            status = "DOWNLOADING"
        )

        scope.launch {
            db.mediaDao().insertOrUpdateDownload(entity)
            startFileDownload(context, episode.id, episode.filePath, targetFile)
        }
    }

    private suspend fun startFileDownload(context: Context, downloadId: String, remotePath: String, targetFile: File) {
        activeJobs[downloadId]?.cancel()

        val job = scope.launch {
            val db = AppDatabase.getDatabase(context)
            try {
                val service = RetrofitClient.getService(context)
                val token = RetrofitClient.getAuthToken(context) ?: ""
                val streamUrl = "/api/stream/video?file=${URLEncoder.encode(remotePath, "UTF-8")}&token=${URLEncoder.encode(token, "UTF-8")}"

                val response = service.downloadFileStream(streamUrl)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val totalSize = body.contentLength()
                    var downloaded = 0L

                    body.byteStream().use { input ->
                        FileOutputStream(targetFile).use { output ->
                            val buffer = ByteArray(8192)
                            var read: Int
                            while (input.read(buffer).also { read = it } != -1) {
                                ensureActive()
                                output.write(buffer, 0, read)
                                downloaded += read
                            }
                            output.flush()
                        }
                    }

                    // Download complete
                    val completedEntity = LocalDownloadEntity(
                        id = downloadId,
                        mediaType = if (targetFile.absolutePath.contains("/Series/")) "Series" else "Movie",
                        showTitle = targetFile.parentFile?.parentFile?.name ?: targetFile.nameWithoutExtension,
                        localFilePath = targetFile.absolutePath,
                        totalBytes = totalSize,
                        downloadedBytes = downloaded,
                        status = "COMPLETED"
                    )
                    db.mediaDao().insertOrUpdateDownload(completedEntity)
                } else {
                    markFailed(db, downloadId)
                }
            } catch (e: CancellationException) {
                // Cancelled or paused
            } catch (e: Exception) {
                markFailed(db, downloadId)
            } finally {
                activeJobs.remove(downloadId)
            }
        }
        activeJobs[downloadId] = job
    }

    private suspend fun markFailed(db: AppDatabase, downloadId: String) {
        // Mark as failed in DB
    }

    // Cancel active download
    fun cancelDownload(context: Context, downloadId: String) {
        activeJobs[downloadId]?.cancel()
        activeJobs.remove(downloadId)
        scope.launch {
            val db = AppDatabase.getDatabase(context)
            db.mediaDao().deleteDownload(downloadId)
        }
    }

    // Delete completed local file from device storage
    fun deleteDownloadedFile(context: Context, downloadId: String, localPath: String) {
        activeJobs[downloadId]?.cancel()
        activeJobs.remove(downloadId)

        scope.launch {
            val file = File(localPath)
            if (file.exists()) file.delete()
            val db = AppDatabase.getDatabase(context)
            db.mediaDao().deleteDownload(downloadId)
        }
    }

    // Delete entire series folder
    fun deleteEntireSeries(context: Context, showTitle: String) {
        scope.launch {
            val safeShow = showTitle.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
            val seriesDir = File(getBaseDownloadDir(), "Series/$safeShow")
            if (seriesDir.exists()) seriesDir.deleteRecursively()
            val db = AppDatabase.getDatabase(context)
            db.mediaDao().deleteEntireSeries(showTitle)
        }
    }
}

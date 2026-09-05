package com.pts.suite.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_movies")
data class CachedMovieEntity(
    @PrimaryKey val id: String,
    val title: String,
    val year: String? = null,
    val rating: String? = null,
    val poster: String? = null,
    val backdrop: String? = null,
    val description: String? = null,
    val duration: String? = null,
    val genresJson: String = "[]",
    val cast: String? = null,
    val filePath: String = "",
    val fileName: String = "",
    val size: String? = null,
    val mtime: Long = 0
)

@Entity(tableName = "cached_series")
data class CachedSeriesEntity(
    @PrimaryKey val id: String,
    val title: String,
    val year: String? = null,
    val rating: String? = null,
    val poster: String? = null,
    val backdrop: String? = null,
    val description: String? = null,
    val genresJson: String = "[]",
    val cast: String? = null,
    val seasonsJson: String = "{}",
    val totalEpisodes: Int = 0,
    val mtime: Long = 0
)

@Entity(tableName = "cached_watchlist")
data class CachedWatchlistEntity(
    @PrimaryKey val id: String,
    val imdbId: String? = null,
    val title: String,
    val type: String = "Movie",
    val year: String? = null,
    val poster: String? = null,
    val rating: String? = null,
    val description: String? = null,
    val filePath: String? = null
)

@Entity(tableName = "cached_vault_docs")
data class CachedVaultDocEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val filename: String = "",
    val categoryId: Int? = null,
    val categoryName: String? = null,
    val docType: String = "other",
    val holderName: String = "",
    val docNumber: String = "",
    val issuer: String = "",
    val expiryDate: String = "",
    val extraInfo: String = "",
    val createdAt: String? = null
)

@Entity(tableName = "cached_vault_notes")
data class CachedVaultNoteEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val content: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Entity(tableName = "cached_download_tasks")
data class CachedDownloadTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String = "Movie",
    val stage: String = "queued",
    val status: String = "ACTIVE",
    val progress: Int = 0,
    val speed: String = "--",
    val eta: String = "--",
    val transferred: String = "--",
    val total: String = "--",
    val message: String = "",
    val downloadUrl: String? = null,
    val error: String? = null,
    val createdAt: String? = null
)

@Entity(tableName = "downloaded_media")
data class LocalDownloadEntity(
    @PrimaryKey val id: String,
    val mediaType: String, // "Movie" or "Series"
    val showTitle: String, // e.g. "Lucifer"
    val seasonNumber: Int = 0,
    val episodeNumber: Int = 0,
    val epCode: String = "",
    val episodeTitle: String = "",
    val localFilePath: String, // e.g. "/storage/emulated/0/Download/PTS/Series/Lucifer/Season 01/S01E01.mp4"
    val posterUrl: String = "",
    val totalBytes: Long = 0,
    val downloadedBytes: Long = 0,
    val status: String = "COMPLETED", // "QUEUED", "DOWNLOADING", "PAUSED", "COMPLETED", "FAILED"
    val createdAt: Long = System.currentTimeMillis()
)

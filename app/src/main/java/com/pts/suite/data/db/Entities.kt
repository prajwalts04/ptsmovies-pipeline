package com.pts.suite.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_movies")
data class CachedMovieEntity(
    @PrimaryKey val id: String,
    val title: String,
    val year: String?,
    val rating: String?,
    val poster: String?,
    val description: String?,
    val genresJson: String,
    val filePath: String,
    val fileName: String,
    val size: String?,
    val mtime: Long
)

@Entity(tableName = "cached_series")
data class CachedSeriesEntity(
    @PrimaryKey val id: String,
    val title: String,
    val year: String?,
    val rating: String?,
    val poster: String?,
    val description: String?,
    val genresJson: String,
    val seasonsJson: String,
    val totalEpisodes: Int,
    val mtime: Long
)

@Entity(tableName = "cached_vault_docs")
data class CachedVaultDocEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val filename: String,
    val categoryName: String?,
    val docType: String,
    val holderName: String,
    val docNumber: String,
    val issuer: String,
    val expiryDate: String,
    val extraInfo: String
)

@Entity(tableName = "cached_vault_notes")
data class CachedVaultNoteEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val content: String,
    val updatedAt: String?
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

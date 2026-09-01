package com.pts.suite.data.api

import com.google.gson.annotations.SerializedName

// --- Authentication Models ---
data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val token: String?,
    val user: UserInfo?,
    val error: String?
)

data class UserInfo(
    val id: String,
    val username: String,
    val role: String,
    val avatarUrl: String? = null
)

data class UpdateProfileRequest(
    val username: String? = null,
    val avatarBase64: String? = null
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class AddUserRequest(
    val username: String,
    val password: String,
    val role: String = "user"
)

// --- System Telemetry Models ---
data class SystemStatsResponse(
    val success: Boolean,
    val stats: SystemStats?
)

data class SystemStats(
    val cpuPercent: Float = 0f,
    val ramUsedPercent: Float = 0f,
    val ramUsedMB: Long = 0,
    val ramTotalMB: Long = 0,
    val diskFreeGB: Float = 0f,
    val diskTotalGB: Float = 0f,
    val diskUsedPercent: Float = 0f,
    val netDownloadSpeed: String = "0 KB/s",
    val netUploadSpeed: String = "0 KB/s"
)

// --- Stream Catalog Models ---
data class MediaLibraryResponse(
    val success: Boolean,
    val movies: List<MovieItem> = emptyList(),
    val series: List<SeriesItem> = emptyList(),
    val watchlist: List<WatchlistItem> = emptyList()
)

data class MovieItem(
    val id: String,
    val title: String,
    val year: String?,
    val rating: String?,
    val poster: String?,
    val description: String?,
    val genres: List<String> = emptyList(),
    val filePath: String,
    val fileName: String,
    val size: String?,
    val mtime: Long? = 0
)

data class SeriesItem(
    val id: String,
    val title: String,
    val year: String?,
    val rating: String?,
    val poster: String?,
    val description: String?,
    val genres: List<String> = emptyList(),
    val seasons: Map<String, List<EpisodeItem>> = emptyMap(),
    val totalEpisodes: Int = 0,
    val mtime: Long? = 0
)

data class EpisodeItem(
    val id: String,
    val season: Int,
    val episode: Int,
    val epCode: String,
    val fileName: String,
    val filePath: String,
    val size: String,
    val mtime: Long? = 0
)

data class WatchlistItem(
    val id: String,
    val imdbId: String?,
    val title: String,
    val type: String,
    val year: String?,
    val poster: String?
)

// --- Hub Queue Models ---
data class DownloadsQueueResponse(
    val success: Boolean,
    val downloads: List<DownloadTask> = emptyList()
)

data class DownloadTask(
    val id: String,
    val title: String,
    val type: String,
    val stage: String,
    val status: String,
    val progress: Int = 0,
    val speed: String = "--",
    val eta: String = "--",
    val transferred: String = "--",
    val total: String = "--",
    val message: String = "",
    val error: String? = null,
    val createdAt: String? = null
)

data class DispatchLinkRequest(
    val downloadUrl: String,
    val title: String,
    val type: String = "Movie"
)

// --- Files App Models ---
data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0,
    val sizeFormatted: String = "",
    val mtime: Long = 0,
    val extension: String = ""
)

// --- Vault Digital Wallet & Notes Models ---
data class VaultStats(
    val totalDocuments: Int = 0,
    val totalNotes: Int = 0,
    val totalCategories: Int = 0,
    val storageUsed: Long = 0
)

data class VaultDocument(
    val id: Int,
    val title: String,
    val filename: String,
    @SerializedName("category_id") val categoryId: Int?,
    @SerializedName("category_name") val categoryName: String?,
    @SerializedName("doc_type") val docType: String = "other",
    @SerializedName("holder_name") val holderName: String = "",
    @SerializedName("doc_number") val docNumber: String = "",
    val issuer: String = "",
    @SerializedName("expiry_date") val expiryDate: String = "",
    @SerializedName("extra_info") val extraInfo: String = "",
    @SerializedName("created_at") val createdAt: String? = null
)

data class VaultNote(
    val id: Int,
    val title: String,
    val content: String,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class VaultCategory(
    val id: Int,
    val name: String
)

// --- In-App Auto-Updater Model ---
data class UpdateManifest(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val changelog: String = "Bug fixes and performance improvements."
)

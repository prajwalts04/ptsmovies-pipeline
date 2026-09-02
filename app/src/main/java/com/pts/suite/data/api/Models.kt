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
data class SystemStats(
    val cpu: CpuStat? = null,
    val memory: MemoryStat? = null,
    val disk: DiskStat? = null,
    val network: NetworkStat? = null
)

data class CpuStat(
    val percent: Float = 0f,
    val cores: Int = 4,
    val temp: Float = 0f,
    val loadAvg: String = "0.00"
)

data class MemoryStat(
    val total: String = "--",
    val used: String = "--",
    val free: String = "--",
    val percent: Int = 0
)

data class DiskStat(
    val size: String = "--",
    val used: String = "--",
    val avail: String = "--",
    val percent: Int = 0
)

data class NetworkStat(
    val downSpeed: String = "0 KB/s",
    val upSpeed: String = "0 KB/s"
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
data class FilesListResponse(
    val success: Boolean = true,
    val currentPath: String = "/Data",
    val parentPath: String? = null,
    val itemsCount: Int = 0,
    val items: List<FileItem> = emptyList()
)

data class FileItem(
    val name: String = "",
    val path: String = "",
    val isDir: Boolean = false,
    val size: Long = 0,
    val formattedSize: String = "--",
    val mtime: String = "",
    val permissions: String = "",
    val ext: String = "",
    val isVideo: Boolean = false,
    val isImage: Boolean = false
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

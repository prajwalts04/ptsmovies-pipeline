package com.pts.suite.data.api

import com.google.gson.annotations.SerializedName

// ============================================================================
// 1. Authentication & User Management Models
// ============================================================================

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val token: String? = null,
    val user: UserInfo? = null,
    val error: String? = null
)

data class UserInfo(
    val id: String? = null,
    val username: String,
    val role: String = "user",
    @SerializedName("avatar_url", alternate = ["avatarUrl"])
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

// ============================================================================
// 2. System Telemetry & Hardware Stats Models
// ============================================================================

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

// ============================================================================
// 3. Stream Media Catalog & Playback Models
// ============================================================================

data class MediaLibraryResponse(
    val success: Boolean,
    val movies: List<MovieItem> = emptyList(),
    val series: List<SeriesItem> = emptyList(),
    val watchlist: List<WatchlistItem> = emptyList(),
    val recentlyAdded: List<MovieItem> = emptyList()
)

data class MovieItem(
    val id: String,
    val title: String,
    val year: String? = null,
    val rating: String? = null,
    val poster: String? = null,
    val backdrop: String? = null,
    val description: String? = null,
    val duration: String? = null,
    val genres: List<String> = emptyList(),
    val cast: String? = null,
    val filePath: String = "",
    val fileName: String = "",
    val size: String? = null,
    val mtime: Long? = 0
)

data class SeriesItem(
    val id: String,
    val title: String,
    val year: String? = null,
    val rating: String? = null,
    val poster: String? = null,
    val backdrop: String? = null,
    val description: String? = null,
    val genres: List<String> = emptyList(),
    val cast: String? = null,
    val seasons: Map<String, List<EpisodeItem>> = emptyMap(),
    val totalEpisodes: Int = 0,
    val mtime: Long? = 0
)

data class EpisodeItem(
    val id: String,
    val season: Int = 1,
    val episode: Int = 1,
    val epCode: String = "S01E01",
    val title: String? = null,
    val fileName: String = "",
    val filePath: String = "",
    val size: String = "--",
    val duration: String? = null,
    val mtime: Long? = 0
)

data class WatchlistItem(
    val id: String,
    val imdbId: String? = null,
    val title: String,
    val type: String = "Movie",
    val year: String? = null,
    val poster: String? = null,
    val rating: String? = null,
    val description: String? = null,
    val filePath: String? = null
)

data class ProgressResponse(
    val continueWatching: List<ProgressItem> = emptyList()
)

data class ProgressItem(
    val id: String? = null,
    val mediaId: String,
    val title: String,
    val type: String = "Movie",
    val year: String? = null,
    val poster: String? = null,
    val season: Int? = null,
    val episode: Int? = null,
    val epCode: String? = null,
    val filePath: String,
    val positionSeconds: Long = 0,
    val durationSeconds: Long = 0,
    val percentage: Float = 0f,
    val updatedAt: Long = 0
)

data class ProgressUpdateRequest(
    val mediaId: String,
    val type: String = "Movie",
    val season: Int? = null,
    val episode: Int? = null,
    val epCode: String? = null,
    val filePath: String,
    val positionSeconds: Long,
    val durationSeconds: Long
)

// ============================================================================
// 4. Hub Queue & Ingestion Models
// ============================================================================

data class DownloadsQueueResponse(
    val success: Boolean,
    val downloads: List<DownloadTask> = emptyList()
)

data class DownloadTask(
    val id: String,
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

data class QueueDownloadRequest(
    val type: String = "Movie",
    val metadata: Map<String, Any>? = null,
    val downloadLink: String? = null,
    val quality: String? = "480p x265",
    val title: String? = null,
    val items: List<Map<String, Any>>? = null
)

data class EditDownloadUrlRequest(
    val downloadUrl: String
)

data class CheckDuplicateRequest(
    val imdbId: String? = null,
    val title: String,
    val year: String? = null,
    val type: String = "Movie",
    val season: Int? = null,
    val episode: Int? = null
)

data class DuplicateCheckResponse(
    val onDisk: Boolean = false,
    val inQueue: Boolean = false,
    val diskPath: String? = null,
    val diskSize: String? = null,
    val queueTaskId: String? = null,
    val existingEpisodes: List<String> = emptyList(),
    val inQueueEpisodes: List<String> = emptyList()
)

data class ImdbFetchRequest(
    val query: String,
    val year: String? = null,
    val type: String? = null
)

data class ImdbFetchResponse(
    val success: Boolean,
    val imdbId: String? = null,
    val title: String? = null,
    val year: String? = null,
    val poster: String? = null,
    val rating: String? = null,
    val description: String? = null,
    val duplicateStatus: DuplicateCheckResponse? = null
)

data class HubSettingsResponse(
    val githubAccount1: Map<String, Any>? = null,
    val githubAccount2: Map<String, Any>? = null,
    val huggingFace: Map<String, Any>? = null
)

data class SaveSettingsRequest(
    val githubAccount1: Map<String, Any>? = null,
    val githubAccount2: Map<String, Any>? = null,
    val huggingFace: Map<String, Any>? = null
)

// ============================================================================
// 5. Vault Digital Wallet & Secure Notes Models
// ============================================================================

data class VaultStats(
    val totalDocuments: Int = 0,
    val totalNotes: Int = 0,
    val totalCategories: Int = 0,
    val storageUsed: Long = 0
)

data class VaultCategory(
    val id: Int,
    val name: String
)

data class VaultDocument(
    val id: Int,
    val title: String,
    val filename: String = "",
    @SerializedName("category_id") val categoryId: Int? = null,
    @SerializedName("category_name") val categoryName: String? = null,
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

data class CreateVaultNoteRequest(
    val title: String,
    val content: String
)

data class UpdateVaultNoteRequest(
    val title: String,
    val content: String
)

// ============================================================================
// 6. Files App (MergerFS Browser) Models
// ============================================================================

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
    val isImage: Boolean = false,
    val isAudio: Boolean = false,
    val isPdf: Boolean = false
)

data class MkdirRequest(
    val path: String,
    val name: String
)

data class TouchRequest(
    val dirPath: String,
    val name: String,
    val content: String = ""
)

data class FileContentResponse(
    val success: Boolean = true,
    val path: String,
    val content: String
)

data class WriteFileRequest(
    val filePath: String,
    val content: String
)

data class RenameRequest(
    val oldPath: String,
    val newName: String? = null,
    val newPath: String? = null
)

data class CopyMoveRequest(
    val sources: List<String>,
    val targetDir: String
)

data class DeleteFilesRequest(
    val paths: List<String>
)

data class ZipRequest(
    val sources: List<String>,
    val targetDir: String,
    val zipName: String
)

data class UnzipRequest(
    val archivePath: String,
    val targetDir: String
)

data class ChmodRequest(
    val targetPath: String,
    val mode: String,
    val recursive: Boolean = false
)

// ============================================================================
// 7. In-App Auto-Updater Model
// ============================================================================

data class UpdateManifest(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val changelog: String = "Bug fixes and performance improvements."
)

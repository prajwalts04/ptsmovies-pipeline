package com.pts.suite.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ========================================================================
    // 1. Authentication & User Management Endpoints
    // ========================================================================

    @POST("/api/auth/login")
    suspend fun login(@Body req: LoginRequest): Response<LoginResponse>

    @GET("/api/auth/me")
    suspend fun getProfile(): Response<LoginResponse>

    @POST("/api/user/profile")
    suspend fun updateProfile(@Body req: UpdateProfileRequest): Response<Map<String, Any>>

    @POST("/api/user/change-password")
    suspend fun changePassword(@Body req: ChangePasswordRequest): Response<Map<String, Any>>

    @GET("/api/users")
    suspend fun listUsers(): Response<List<UserInfo>>

    @POST("/api/users")
    suspend fun addUser(@Body req: AddUserRequest): Response<Map<String, Any>>

    @DELETE("/api/users/{id}")
    suspend fun deleteUser(@Path("id") userId: String): Response<Map<String, Any>>

    @Multipart
    @POST("/api/user/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): Response<Map<String, Any>>

    // ========================================================================
    // 2. System Stats & Hardware Telemetry Endpoint
    // ========================================================================

    @GET("/api/system/stats")
    suspend fun getSystemStats(): Response<SystemStats>

    // ========================================================================
    // 3. PTS Stream Catalog, Watchlist & Playback Sync Endpoints
    // ========================================================================

    @GET("/api/media/library")
    suspend fun getMediaLibrary(): Response<MediaLibraryResponse>

    @POST("/api/media/watchlist/toggle")
    suspend fun toggleWatchlist(@Body item: Map<String, Any>): Response<Map<String, Any>>

    @POST("/api/media/watchlist")
    suspend fun addToWatchlist(@Body item: Map<String, Any>): Response<Map<String, Any>>

    @GET("/api/progress/all")
    suspend fun getAllProgress(): Response<ProgressResponse>

    @POST("/api/progress/update")
    suspend fun updateProgress(@Body req: ProgressUpdateRequest): Response<Map<String, Any>>

    @Streaming
    @GET("/api/stream/video")
    suspend fun streamVideo(
        @Query("file") filePath: String,
        @Query("token") token: String? = null
    ): Response<ResponseBody>

    // ========================================================================
    // 4. PTS Hub Queue, Duplicate Checking & Settings Endpoints
    // ========================================================================

    @GET("/api/downloads")
    suspend fun getDownloadsQueue(): Response<DownloadsQueueResponse>

    @POST("/api/downloads/queue")
    suspend fun queueDownload(@Body req: Map<String, Any>): Response<Map<String, Any>>

    @PUT("/api/downloads/{id}")
    suspend fun editDownloadUrl(
        @Path("id") taskId: String,
        @Body req: EditDownloadUrlRequest
    ): Response<Map<String, Any>>

    @POST("/api/downloads/{id}/retry")
    suspend fun retryDownloadTask(@Path("id") taskId: String): Response<Map<String, Any>>

    @DELETE("/api/downloads/{id}")
    suspend fun cancelDownloadTask(@Path("id") taskId: String): Response<Map<String, Any>>

    @POST("/api/downloads/clear-all")
    suspend fun clearCompletedDownloads(): Response<Map<String, Any>>

    @POST("/api/downloads/check-duplicate")
    suspend fun checkDuplicate(@Body req: CheckDuplicateRequest): Response<DuplicateCheckResponse>

    @POST("/api/imdb/fetch")
    suspend fun fetchImdbMetadata(@Body req: ImdbFetchRequest): Response<ImdbFetchResponse>

    @GET("/api/settings")
    suspend fun getHubSettings(): Response<HubSettingsResponse>

    @POST("/api/settings")
    suspend fun saveHubSettings(@Body req: SaveSettingsRequest): Response<Map<String, Any>>

    @POST("/api/settings/clean-deploy-repos")
    suspend fun cleanDeployRepos(): Response<Map<String, Any>>

    // ========================================================================
    // 5. PTS Vault Digital Wallet & Notes Endpoints
    // ========================================================================

    @GET("/api/vault/stats")
    suspend fun getVaultStats(): Response<VaultStats>

    @GET("/api/vault/categories")
    suspend fun getVaultCategories(): Response<List<VaultCategory>>

    @GET("/api/vault/documents")
    suspend fun getVaultDocuments(): Response<List<VaultDocument>>

    @Multipart
    @POST("/api/vault/documents")
    @POST("/api/vault/documents")
    suspend fun createVaultDocument(@Body fields: Map<String, String>): Response<VaultDocument>
    suspend fun uploadVaultDocument(
        @Part("title") title: RequestBody,
        @Part("category_id") categoryId: RequestBody,
        @Part("doc_type") docType: RequestBody,
        @Part("holder_name") holderName: RequestBody,
        @Part("doc_number") docNumber: RequestBody,
        @Part("issuer") issuer: RequestBody,
        @Part("expiry_date") expiryDate: RequestBody,
        @Part("extra_info") extraInfo: RequestBody? = null,
        @Part file: MultipartBody.Part? = null
    ): Response<Map<String, Any>>

    @DELETE("/api/vault/documents/{id}")
    suspend fun deleteVaultDocument(@Path("id") id: Int): Response<Map<String, Any>>

    @GET("/api/vault/notes")
    suspend fun getVaultNotes(): Response<List<VaultNote>>

    @POST("/api/vault/notes")
    suspend fun createVaultNote(@Body note: Map<String, String>): Response<VaultNote>

    @PUT("/api/vault/notes/{id}")
    suspend fun updateVaultNote(
        @Path("id") id: Int,
        @Body note: Map<String, String>
    ): Response<Map<String, Any>>

    @DELETE("/api/vault/notes/{id}")
    suspend fun deleteVaultNote(@Path("id") id: Int): Response<Map<String, Any>>

    // ========================================================================
    // 6. PTS Files Explorer Endpoints
    // ========================================================================

    @GET("/api/fs/list")
    suspend fun browseDirectory(@Query("path") path: String = "/Data"): Response<FilesListResponse>

    @POST("/api/fs/mkdir")
    suspend fun createDirectory(@Body req: Map<String, String>): Response<Map<String, Any>>

    @POST("/api/fs/touch")
    suspend fun touchFile(@Body req: Map<String, String>): Response<Map<String, Any>>

    @GET("/api/fs/read")
    suspend fun readFile(@Query("path") path: String): Response<FileContentResponse>

    @POST("/api/fs/write")
    suspend fun writeFile(@Body req: Map<String, String>): Response<Map<String, Any>>

    @POST("/api/fs/rename")
    suspend fun renameFile(@Body req: Map<String, String>): Response<Map<String, Any>>

    @POST("/api/fs/copy")
    suspend fun copyFiles(@Body req: Map<String, Any>): Response<Map<String, Any>>

    @POST("/api/fs/move")
    suspend fun moveFiles(@Body req: Map<String, Any>): Response<Map<String, Any>>

    @POST("/api/fs/delete")
    suspend fun deleteFiles(@Body req: Map<String, List<String>>): Response<Map<String, Any>>

    @POST("/api/fs/zip")
    suspend fun zipFiles(@Body req: Map<String, Any>): Response<Map<String, Any>>

    @POST("/api/fs/unzip")
    suspend fun unzipArchive(@Body req: Map<String, String>): Response<Map<String, Any>>

    @POST("/api/fs/chmod")
    suspend fun chmodFile(@Body req: Map<String, Any>): Response<Map<String, Any>>

    @Streaming
    @GET("/api/fs/download")
    suspend fun downloadFile(@Query("path") path: String): Response<ResponseBody>

    @Streaming
    @POST("/api/fs/download-zip")
    suspend fun downloadZip(@Body req: Map<String, List<String>>): Response<ResponseBody>

    @Streaming
    @GET("/api/fs/preview")
    suspend fun previewFile(@Query("path") path: String): Response<ResponseBody>

    @Multipart
    @POST("/api/fs/upload")
    suspend fun uploadFiles(
        @Part("targetDir") targetDir: RequestBody,
        @Part files: List<MultipartBody.Part>
    ): Response<Map<String, Any>>

    // ========================================================================
    // 7. In-App Auto-Update & Generic Streaming Endpoints
    // ========================================================================

    @GET("/api/app/update-manifest.json")
    suspend fun checkAppUpdate(): Response<UpdateManifest>

    @Streaming
    @GET
    suspend fun downloadFileStream(@Url fileUrl: String): Response<ResponseBody>
}

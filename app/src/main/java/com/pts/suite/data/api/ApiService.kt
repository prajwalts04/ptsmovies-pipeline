package com.pts.suite.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- Authentication & User Management ---
    @POST("/api/auth/login")
    async suspend fun login(@Body req: LoginRequest): Response<LoginResponse>

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

    // --- System Stats Telemetry ---
    @GET("/api/system/stats")
    suspend fun getSystemStats(): Response<SystemStats>

    // --- Stream Catalog ---
    @GET("/api/media/library")
    suspend fun getMediaLibrary(): Response<MediaLibraryResponse>

    @POST("/api/media/watchlist/toggle")
    suspend fun toggleWatchlist(@Body item: Map<String, Any>): Response<Map<String, Any>>

    // --- Hub Queue & GHA Dispatch ---
    @GET("/api/downloads")
    suspend fun getDownloadsQueue(): Response<DownloadsQueueResponse>

    @POST("/api/downloads/dispatch")
    suspend fun dispatchLink(@Body req: DispatchLinkRequest): Response<Map<String, Any>>

    @DELETE("/api/downloads/{id}")
    suspend fun cancelDownloadTask(@Path("id") taskId: String): Response<Map<String, Any>>

    // --- Vault Digital Wallet & Notes ---
    @GET("/api/vault/stats")
    suspend fun getVaultStats(): Response<VaultStats>

    @GET("/api/vault/documents")
    suspend fun getVaultDocuments(): Response<List<VaultDocument>>

    @Multipart
    @POST("/api/vault/documents")
    suspend fun uploadVaultDocument(
        @Part("title") title: RequestBody,
        @Part("category_id") categoryId: RequestBody,
        @Part("doc_type") docType: RequestBody,
        @Part("holder_name") holderName: RequestBody,
        @Part("doc_number") docNumber: RequestBody,
        @Part("issuer") issuer: RequestBody,
        @Part("expiry_date") expiryDate: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<Map<String, Any>>

    @DELETE("/api/vault/documents/{id}")
    suspend fun deleteVaultDocument(@Path("id") id: Int): Response<Map<String, Any>>

    @GET("/api/vault/notes")
    suspend fun getVaultNotes(): Response<List<VaultNote>>

    @POST("/api/vault/notes")
    suspend fun createVaultNote(@Body note: Map<String, String>): Response<VaultNote>

    @PUT("/api/vault/notes/{id}")
    suspend fun updateVaultNote(@Path("id") id: Int, @Body note: Map<String, String>): Response<Map<String, Any>>

    @DELETE("/api/vault/notes/{id}")
    suspend fun deleteVaultNote(@Path("id") id: Int): Response<Map<String, Any>>

    @GET("/api/vault/categories")
    suspend fun getVaultCategories(): Response<List<VaultCategory>>

    // --- Files App ---
    @GET("/api/files/browse")
    suspend fun browseDirectory(@Query("path") path: String = "/Data"): Response<List<FileItem>>

    // --- In-App Auto-Update Manifest ---
    @GET("/api/app/update-manifest.json")
    suspend fun checkAppUpdate(): Response<UpdateManifest>

    @Streaming
    @GET
    suspend fun downloadFileStream(@Url fileUrl: String): Response<ResponseBody>
}

package com.pts.suite.data.db

import android.content.Context
import androidx.room.*

@Dao
interface MediaDao {
    @Query("SELECT * FROM cached_movies ORDER BY mtime DESC")
    suspend fun getMovies(): List<CachedMovieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<CachedMovieEntity>)

    @Query("DELETE FROM cached_movies")
    suspend fun clearMovies()

    @Query("SELECT * FROM cached_series ORDER BY mtime DESC")
    suspend fun getSeries(): List<CachedSeriesEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: List<CachedSeriesEntity>)

    @Query("DELETE FROM cached_series")
    suspend fun clearSeries()

    @Query("SELECT * FROM cached_watchlist ORDER BY title ASC")
    suspend fun getWatchlist(): List<CachedWatchlistEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(items: List<CachedWatchlistEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistItem(item: CachedWatchlistEntity)

    @Query("DELETE FROM cached_watchlist WHERE id = :id")
    suspend fun deleteWatchlistItem(id: String)

    @Query("DELETE FROM cached_watchlist")
    suspend fun clearWatchlist()

    // Offline Local Downloads
    @Query("SELECT * FROM downloaded_media ORDER BY createdAt DESC")
    suspend fun getAllDownloads(): List<LocalDownloadEntity>

    @Query("SELECT * FROM downloaded_media WHERE mediaType = 'Movie' ORDER BY createdAt DESC")
    suspend fun getDownloadedMovies(): List<LocalDownloadEntity>

    @Query("SELECT * FROM downloaded_media WHERE mediaType = 'Series' ORDER BY showTitle ASC, seasonNumber ASC, episodeNumber ASC")
    suspend fun getDownloadedSeries(): List<LocalDownloadEntity>

    @Query("SELECT DISTINCT showTitle FROM downloaded_media WHERE mediaType = 'Series'")
    suspend fun getDownloadedShowTitles(): List<String>

    @Query("SELECT * FROM downloaded_media WHERE mediaType = 'Series' AND showTitle = :showTitle ORDER BY seasonNumber ASC, episodeNumber ASC")
    suspend fun getEpisodesForShow(showTitle: String): List<LocalDownloadEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDownload(download: LocalDownloadEntity)

    @Query("DELETE FROM downloaded_media WHERE id = :id")
    suspend fun deleteDownload(id: String)

    @Query("DELETE FROM downloaded_media WHERE showTitle = :showTitle")
    suspend fun deleteEntireSeries(showTitle: String)
}

@Dao
interface VaultDao {
    @Query("SELECT * FROM cached_vault_docs ORDER BY title ASC")
    suspend fun getDocuments(): List<CachedVaultDocEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(docs: List<CachedVaultDocEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: CachedVaultDocEntity)

    @Query("DELETE FROM cached_vault_docs WHERE id = :id")
    suspend fun deleteDocument(id: Int)

    @Query("DELETE FROM cached_vault_docs")
    suspend fun clearDocuments()

    @Query("SELECT * FROM cached_vault_notes ORDER BY updatedAt DESC")
    suspend fun getNotes(): List<CachedVaultNoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<CachedVaultNoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: CachedVaultNoteEntity)

    @Query("DELETE FROM cached_vault_notes WHERE id = :id")
    suspend fun deleteNote(id: Int)

    @Query("DELETE FROM cached_vault_notes")
    suspend fun clearNotes()
}

@Dao
interface DownloadQueueDao {
    @Query("SELECT * FROM cached_download_tasks ORDER BY createdAt DESC")
    suspend fun getTasks(): List<CachedDownloadTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<CachedDownloadTaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: CachedDownloadTaskEntity)

    @Query("DELETE FROM cached_download_tasks WHERE id = :id")
    suspend fun deleteTask(id: String)

    @Query("DELETE FROM cached_download_tasks")
    suspend fun clearTasks()
}

@Database(
    entities = [
        CachedMovieEntity::class,
        CachedSeriesEntity::class,
        CachedWatchlistEntity::class,
        CachedVaultDocEntity::class,
        CachedVaultNoteEntity::class,
        CachedDownloadTaskEntity::class,
        LocalDownloadEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun vaultDao(): VaultDao
    abstract fun downloadQueueDao(): DownloadQueueDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pts_suite_local.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

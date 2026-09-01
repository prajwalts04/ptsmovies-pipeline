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

    // Offline Downloads
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

@Database(
    entities = [
        CachedMovieEntity::class,
        CachedSeriesEntity::class,
        CachedVaultDocEntity::class,
        CachedVaultNoteEntity::class,
        LocalDownloadEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao

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

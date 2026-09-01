package com.pts.suite

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.pts.suite.data.api.*
import com.pts.suite.ui.components.*
import com.pts.suite.ui.screens.dashboard.DashboardScreen
import com.pts.suite.ui.screens.downloads.OfflineDownloadsScreen
import com.pts.suite.ui.screens.files.FilesScreen
import com.pts.suite.ui.screens.hub.HubQueueScreen
import com.pts.suite.ui.screens.login.LoginScreen
import com.pts.suite.ui.screens.profile.ProfileScreen
import com.pts.suite.ui.screens.ssh.TerminalScreen
import com.pts.suite.ui.screens.stream.StreamCatalogScreen
import com.pts.suite.ui.screens.stream.WatchScreen
import com.pts.suite.ui.screens.vault.VaultDeckScreen
import com.pts.suite.ui.theme.PTSSuiteTheme
import com.pts.suite.ui.theme.PitchBlack
import com.pts.suite.updater.AppUpdater
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PTSSuiteTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                var authToken by remember { mutableStateOf(RetrofitClient.getAuthToken(this@MainActivity)) }
                var currentUser by remember { mutableStateOf<UserInfo?>(null) }
                var currentDestination by remember { mutableStateOf(AppDestination.DASHBOARD) }

                // Telemetry & Media State
                var systemStats by remember { mutableStateOf(SystemStats()) }
                var movies by remember { mutableStateOf<List<MovieItem>>(emptyList()) }
                var series by remember { mutableStateOf<List<SeriesItem>>(emptyList()) }
                var watchlist by remember { mutableStateOf<List<WatchlistItem>>(emptyList()) }
                var hubTasks by remember { mutableStateOf<List<DownloadTask>>(emptyList()) }
                var vaultDocs by remember { mutableStateOf<List<VaultDocument>>(emptyList()) }
                var vaultNotes by remember { mutableStateOf<List<VaultNote>>(emptyList()) }
                var vaultCategories by remember { mutableStateOf<List<VaultCategory>>(emptyList()) }
                var currentFilePath by remember { mutableStateOf("/Data") }
                var filesList by remember { mutableStateOf<List<FileItem>>(emptyList()) }

                // Watch Screen Active Media
                var activeMovie by remember { mutableStateOf<MovieItem?>(null) }
                var activeSeries by remember { mutableStateOf<SeriesItem?>(null) }

                // In-App Update Banner State
                var updateManifest by remember { mutableStateOf<UpdateManifest?>(null) }

                // Fetch Data function
                fun refreshAllData() {
                    scope.launch {
                        try {
                            val service = RetrofitClient.getService(this@MainActivity)

                            // Telemetry Stats
                            val statsRes = service.getSystemStats()
                            if (statsRes.isSuccessful && statsRes.body() != null) {
                                systemStats = statsRes.body()!!
                            }

                            // Stream Library
                            val libRes = service.getMediaLibrary()
                            if (libRes.isSuccessful && libRes.body() != null) {
                                movies = libRes.body()!!.movies
                                series = libRes.body()!!.series
                                watchlist = libRes.body()!!.watchlist
                            }

                            // Hub Queue
                            val queueRes = service.getDownloadsQueue()
                            if (queueRes.isSuccessful && queueRes.body() != null) {
                                hubTasks = queueRes.body()!!.downloads
                            }

                            // Vault Docs
                            val vDocsRes = service.getVaultDocuments()
                            if (vDocsRes.isSuccessful && vDocsRes.body() != null) {
                                vaultDocs = vDocsRes.body()!!
                            }

                            // Vault Notes
                            val vNotesRes = service.getVaultNotes()
                            if (vNotesRes.isSuccessful && vNotesRes.body() != null) {
                                vaultNotes = vNotesRes.body()!!
                            }

                            // Check In-App Update
                            val manifest = AppUpdater.checkForUpdate(this@MainActivity)
                            if (manifest != null) {
                                updateManifest = manifest
                            }
                        } catch (e: Exception) {
                            // Handled silently
                        }
                    }
                }

                LaunchedEffect(authToken) {
                    if (!authToken.isNullOrEmpty()) {
                        refreshAllData()
                    }
                }

                // If not logged in -> Show LoginScreen
                if (authToken.isNullOrEmpty()) {
                    LoginScreen(
                        initialServerUrl = RetrofitClient.getServerUrl(this@MainActivity),
                        onLogin = { serverUrl, username, password ->
                            RetrofitClient.setServerUrl(this@MainActivity, serverUrl)
                            scope.launch {
                                try {
                                    val res = RetrofitClient.getService(this@MainActivity).login(
                                        LoginRequest(username = username, password = password)
                                    )
                                    if (res.isSuccessful && res.body()?.success == true) {
                                        val token = res.body()?.token
                                        RetrofitClient.setAuthToken(this@MainActivity, token)
                                        authToken = token
                                        currentUser = res.body()?.user
                                        refreshAllData()
                                    }
                                } catch (e: Exception) {}
                            }
                        },
                        onBiometricLogin = { },
                        canUseBiometric = false,
                        isLoading = false,
                        errorMessage = null
                    )
                } else if (activeMovie != null || activeSeries != null) {
                    // Watch Screen (ExoPlayer & Episode explorer)
                    WatchScreen(
                        movie = activeMovie,
                        series = activeSeries,
                        onBack = {
                            activeMovie = null
                            activeSeries = null
                        }
                    )
                } else {
                    // Main Shell with TopBar, Slide-out Drawer, and App Screens
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            PTSNavigationDrawerContent(
                                user = currentUser,
                                currentDestination = currentDestination,
                                onNavigate = { destination ->
                                    currentDestination = destination
                                },
                                onCloseDrawer = {
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }
                    ) {
                        Scaffold(
                            topBar = {
                                TopBar(
                                    user = currentUser,
                                    onOpenDrawer = {
                                        scope.launch { drawerState.open() }
                                    },
                                    onOpenProfile = {
                                        currentDestination = AppDestination.PROFILE
                                    }
                                )
                            },
                            containerColor = PitchBlack
                        ) { padding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(padding)
                                    .background(PitchBlack)
                            ) {
                                when (currentDestination) {
                                    AppDestination.DASHBOARD -> DashboardScreen(
                                        stats = systemStats,
                                        onNavigate = { currentDestination = it }
                                    )
                                    AppDestination.STREAM -> StreamCatalogScreen(
                                        movies = movies,
                                        series = series,
                                        watchlist = watchlist,
                                        onSelectMovie = { activeMovie = it },
                                        onSelectSeries = { activeSeries = it }
                                    )
                                    AppDestination.HUB -> HubQueueScreen(
                                        tasks = hubTasks,
                                        onRefresh = { refreshAllData() }
                                    )
                                    AppDestination.FILES -> FilesScreen(
                                        currentPath = currentFilePath,
                                        files = filesList,
                                        onNavigateDir = { currentFilePath = it },
                                        onBackDir = { currentFilePath = "/Data" }
                                    )
                                    AppDestination.VAULT -> VaultDeckScreen(
                                        documents = vaultDocs,
                                        notes = vaultNotes,
                                        categories = vaultCategories,
                                        onRefresh = { refreshAllData() }
                                    )
                                    AppDestination.TERMINAL -> TerminalScreen(
                                        onBack = { currentDestination = AppDestination.DASHBOARD }
                                    )
                                    AppDestination.DOWNLOADS -> OfflineDownloadsScreen(
                                        onPlayOfflineFile = { filePath, title ->
                                            activeMovie = MovieItem(
                                                id = "local_${title}",
                                                title = title,
                                                year = "",
                                                rating = "",
                                                poster = null,
                                                description = "Offline Media",
                                                genres = listOf("Offline"),
                                                filePath = filePath,
                                                fileName = filePath.substringAfterLast('/'),
                                                size = ""
                                            )
                                        },
                                        onBack = { currentDestination = AppDestination.PROFILE }
                                    )
                                    AppDestination.PROFILE -> ProfileScreen(
                                        user = currentUser,
                                        onNavigateToDownloads = { currentDestination = AppDestination.DOWNLOADS },
                                        onLogout = {
                                            RetrofitClient.setAuthToken(this@MainActivity, null)
                                            authToken = null
                                            currentUser = null
                                        },
                                        onBack = { currentDestination = AppDestination.DASHBOARD }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

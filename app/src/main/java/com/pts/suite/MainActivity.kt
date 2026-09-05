package com.pts.suite

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.pts.suite.data.api.*
import com.pts.suite.ui.components.*
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
import com.pts.suite.ui.theme.sketchCanvasBackground
import com.pts.suite.updater.AppUpdater
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var lastBackPressTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Run auto-updater check in background
        AppUpdater.checkForUpdate(this)

        setContent {
            PTSSuiteTheme {
                val scope = rememberCoroutineScope()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

                var authToken by remember { mutableStateOf(RetrofitClient.getAuthToken(this@MainActivity)) }
                var currentUser by remember { mutableStateOf<UserInfo?>(null) }
                var systemStats by remember { mutableStateOf(SystemStats()) }
                var currentDestination by remember { mutableStateOf(AppDestination.STREAM) }

                // Stream screen state
                var movies by remember { mutableStateOf<List<MovieItem>>(emptyList()) }
                var series by remember { mutableStateOf<List<SeriesItem>>(emptyList()) }
                var watchlist by remember { mutableStateOf<List<WatchlistItem>>(emptyList()) }
                var selectedMovie by remember { mutableStateOf<MovieItem?>(null) }
                var selectedSeries by remember { mutableStateOf<SeriesItem?>(null) }

                // Hub screen state
                var downloadTasks by remember { mutableStateOf<List<DownloadTask>>(emptyList()) }

                // Vault screen state
                var vaultDocuments by remember { mutableStateOf<List<VaultDocument>>(emptyList()) }
                var vaultNotes by remember { mutableStateOf<List<VaultNote>>(emptyList()) }
                var vaultCategories by remember { mutableStateOf<List<VaultCategory>>(emptyList()) }

                // --- Background Polling Engine ---
                LaunchedEffect(authToken) {
                    if (!authToken.isNullOrEmpty()) {
                        // 1. Fetch user profile
                        try {
                            val profileRes = RetrofitClient.getService(this@MainActivity).getProfile()
                            if (profileRes.isSuccessful) {
                                currentUser = profileRes.body()?.user
                            }
                        } catch (e: Exception) {
                            // Offline or network error
                        }

                        // 2. Fetch initial Stream library
                        try {
                            val libRes = RetrofitClient.getService(this@MainActivity).getMediaLibrary()
                            if (libRes.isSuccessful) {
                                val body = libRes.body()
                                movies = body?.movies ?: emptyList()
                                series = body?.series ?: emptyList()
                                watchlist = body?.watchlist ?: emptyList()
                            }
                        } catch (e: Exception) {}

                        // 3. Periodic 3.5s Telemetry & Queue Polling
                        while (true) {
                            try {
                                val statsRes = RetrofitClient.getService(this@MainActivity).getSystemStats()
                                if (statsRes.isSuccessful) {
                                    statsRes.body()?.let { systemStats = it }
                                }
                            } catch (e: Exception) {}

                            try {
                                val dlRes = RetrofitClient.getService(this@MainActivity).getDownloadsQueue()
                                if (dlRes.isSuccessful) {
                                    downloadTasks = dlRes.body()?.downloads ?: emptyList()
                                }
                            } catch (e: Exception) {}

                            delay(3500)
                        }
                    }
                }

                // --- Robust Multi-level BackHandler ---
                val isWatching = selectedMovie != null || selectedSeries != null
                val isDrawerOpen = drawerState.isOpen
                val isRootScreen = currentDestination == AppDestination.STREAM && !isWatching

                BackHandler(enabled = true) {
                    when {
                        isDrawerOpen -> {
                            scope.launch { drawerState.close() }
                        }
                        isWatching -> {
                            selectedMovie = null
                            selectedSeries = null
                        }
                        currentDestination != AppDestination.STREAM -> {
                            currentDestination = AppDestination.STREAM
                        }
                        else -> {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastBackPressTime < 2000) {
                                finish()
                            } else {
                                lastBackPressTime = currentTime
                                Toast.makeText(this@MainActivity, "Press back again to exit", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                if (authToken.isNullOrEmpty()) {
                    // Login Screen
                    LoginScreen(
                        onLoginSuccess = { token, user ->
                            RetrofitClient.setAuthToken(this@MainActivity, token)
                            authToken = token
                            currentUser = user
                        }
                    )
                } else {
                    // Main App Shell with Drawer & Navigation Dock
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            PTSNavigationDrawerContent(
                                user = currentUser,
                                stats = systemStats,
                                currentDestination = currentDestination,
                                onNavigate = { destination ->
                                    currentDestination = destination
                                    selectedMovie = null
                                    selectedSeries = null
                                },
                                onCloseDrawer = {
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }
                    ) {
                        Scaffold(
                            topBar = {
                                if (!isWatching) {
                                    TopBar(
                                        user = currentUser,
                                        stats = systemStats,
                                        onOpenDrawer = {
                                            scope.launch { drawerState.open() }
                                        },
                                        onOpenProfile = {
                                            currentDestination = AppDestination.PROFILE
                                        }
                                    )
                                }
                            },
                            bottomBar = {
                                if (!isWatching) {
                                    val activeDownloadsCount = downloadTasks.count {
                                        it.status.equals("ACTIVE", ignoreCase = true) ||
                                        it.stage.contains("download", ignoreCase = true) ||
                                        it.stage.contains("compress", ignoreCase = true)
                                    }

                                    val mainTabs = listOf(
                                        DockTabItem("stream", "Stream", Icons.Default.Movie),
                                        DockTabItem("hub", "Hub", Icons.Default.Bolt, badgeCount = activeDownloadsCount),
                                        DockTabItem("files", "Files", Icons.Default.Folder),
                                        DockTabItem("vault", "Vault", Icons.Default.CreditCard),
                                        DockTabItem("terminal", "Terminal", Icons.Default.Terminal)
                                    )

                                    val activeTabId = when (currentDestination) {
                                        AppDestination.STREAM -> "stream"
                                        AppDestination.HUB -> "hub"
                                        AppDestination.FILES -> "files"
                                        AppDestination.VAULT -> "vault"
                                        AppDestination.TERMINAL -> "terminal"
                                        else -> "stream"
                                    }

                                    DynamicBottomDock(
                                        tabs = mainTabs,
                                        selectedTabId = activeTabId,
                                        onTabSelected = { tabId ->
                                            when (tabId) {
                                                "stream" -> currentDestination = AppDestination.STREAM
                                                "hub" -> currentDestination = AppDestination.HUB
                                                "files" -> currentDestination = AppDestination.FILES
                                                "vault" -> currentDestination = AppDestination.VAULT
                                                "terminal" -> currentDestination = AppDestination.TERMINAL
                                            }
                                        }
                                    )
                                }
                            },
                            containerColor = PitchBlack
                        ) { padding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(padding)
                                    .sketchCanvasBackground()
                            ) {
                                when (currentDestination) {
                                    AppDestination.STREAM -> {
                                        if (selectedMovie != null || selectedSeries != null) {
                                            WatchScreen(
                                                movie = selectedMovie,
                                                series = selectedSeries,
                                                onBack = {
                                                    selectedMovie = null
                                                    selectedSeries = null
                                                }
                                            )
                                        } else {
                                            StreamCatalogScreen(
                                                movies = movies,
                                                series = series,
                                                watchlist = watchlist,
                                                onSelectMovie = { selectedMovie = it },
                                                onSelectSeries = { selectedSeries = it }
                                            )
                                        }
                                    }

                                    AppDestination.HUB -> {
                                        HubQueueScreen(
                                            tasks = downloadTasks,
                                            onRefresh = {
                                                scope.launch {
                                                    try {
                                                        val res = RetrofitClient.getService(this@MainActivity).getDownloadsQueue()
                                                        if (res.isSuccessful) downloadTasks = res.body()?.downloads ?: emptyList()
                                                    } catch (e: Exception) {}
                                                }
                                            }
                                        )
                                    }

                                    AppDestination.FILES -> {
                                        FilesScreen(
                                            onNavigateBack = { currentDestination = AppDestination.STREAM }
                                        )
                                    }

                                    AppDestination.VAULT -> {
                                        VaultDeckScreen(
                                            documents = vaultDocuments,
                                            notes = vaultNotes,
                                            categories = vaultCategories,
                                            onRefresh = {
                                                scope.launch {
                                                    try {
                                                        val service = RetrofitClient.getService(this@MainActivity)
                                                        val docs = service.getVaultDocuments()
                                                        if (docs.isSuccessful) vaultDocuments = docs.body() ?: emptyList()
                                                        val notes = service.getVaultNotes()
                                                        if (notes.isSuccessful) vaultNotes = notes.body() ?: emptyList()
                                                    } catch (e: Exception) {}
                                                }
                                            }
                                        )
                                    }

                                    AppDestination.TERMINAL -> {
                                        TerminalScreen(
                                            onBack = { currentDestination = AppDestination.STREAM }
                                        )
                                    }

                                    AppDestination.DOWNLOADS -> {
                                        OfflineDownloadsScreen(
                                            onPlayOfflineFile = { _, _ -> },
                                            onBack = { currentDestination = AppDestination.STREAM }
                                        )
                                    }

                                    AppDestination.PROFILE -> {
                                        ProfileScreen(
                                            user = currentUser,
                                            onNavigateToDownloads = { currentDestination = AppDestination.DOWNLOADS },
                                            onLogout = {
                                                RetrofitClient.setAuthToken(this@MainActivity, null)
                                                authToken = null
                                                currentUser = null
                                            },
                                            onBack = { currentDestination = AppDestination.STREAM }
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
}

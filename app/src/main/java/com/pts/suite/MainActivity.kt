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
import com.pts.suite.updater.AppUpdater
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PTSSuiteTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                var authToken by remember { mutableStateOf(RetrofitClient.getAuthToken(this@MainActivity)) }
                var currentUser by remember { mutableStateOf<UserInfo?>(null) }
                var currentDestination by remember { mutableStateOf(AppDestination.STREAM) }

                // Live Telemetry Stats State
                var systemStats by remember { mutableStateOf(SystemStats()) }

                // Media & Library State
                var movies by remember { mutableStateOf<List<MovieItem>>(emptyList()) }
                var series by remember { mutableStateOf<List<SeriesItem>>(emptyList()) }
                var watchlist by remember { mutableStateOf<List<WatchlistItem>>(emptyList()) }
                var selectedMovie by remember { mutableStateOf<MovieItem?>(null) }
                var selectedSeries by remember { mutableStateOf<SeriesItem?>(null) }

                // Hub Queue State
                var downloadTasks by remember { mutableStateOf<List<DownloadTask>>(emptyList()) }

                // Vault State
                var vaultDocuments by remember { mutableStateOf<List<VaultDocument>>(emptyList()) }
                var vaultNotes by remember { mutableStateOf<List<VaultNote>>(emptyList()) }
                var vaultCategories by remember { mutableStateOf<List<VaultCategory>>(emptyList()) }

                // Back press tracking for double-tap to exit
                var lastBackPressTime by remember { mutableLongStateOf(0L) }

                // Load all initial data once logged in
                fun loadInitialData() {
                    scope.launch {
                        try {
                            val service = RetrofitClient.getService(this@MainActivity)

                            // 1. Profile
                            launch {
                                try {
                                    val profileRes = service.getProfile()
                                    if (profileRes.isSuccessful) currentUser = profileRes.body()?.user
                                } catch (e: Exception) {}
                            }

                            // 2. Stream Catalog
                            launch {
                                try {
                                    val libRes = service.getMediaLibrary()
                                    if (libRes.isSuccessful && libRes.body() != null) {
                                        movies = libRes.body()!!.movies
                                        series = libRes.body()!!.series
                                        watchlist = libRes.body()!!.watchlist
                                    }
                                } catch (e: Exception) {}
                            }

                            // 3. Downloads Queue
                            launch {
                                try {
                                    val queueRes = service.getDownloadsQueue()
                                    if (queueRes.isSuccessful) downloadTasks = queueRes.body()?.downloads ?: emptyList()
                                } catch (e: Exception) {}
                            }

                            // 4. Vault
                            launch {
                                try {
                                    val docsRes = service.getVaultDocuments()
                                    if (docsRes.isSuccessful) vaultDocuments = docsRes.body() ?: emptyList()
                                    val notesRes = service.getVaultNotes()
                                    if (notesRes.isSuccessful) vaultNotes = notesRes.body() ?: emptyList()
                                    val catsRes = service.getVaultCategories()
                                    if (catsRes.isSuccessful) vaultCategories = catsRes.body() ?: emptyList()
                                } catch (e: Exception) {}
                            }
                        } catch (e: Exception) {}
                    }
                }

                // Poll live telemetry stats and downloads queue every 3.5 seconds
                LaunchedEffect(authToken) {
                    if (!authToken.isNullOrEmpty()) {
                        loadInitialData()
                        while (true) {
                            try {
                                val service = RetrofitClient.getService(this@MainActivity)
                                val statsRes = service.getSystemStats()
                                if (statsRes.isSuccessful && statsRes.body() != null) {
                                    systemStats = statsRes.body()!!
                                }
                                if (currentDestination == AppDestination.HUB) {
                                    val queueRes = service.getDownloadsQueue()
                                    if (queueRes.isSuccessful) downloadTasks = queueRes.body()?.downloads ?: emptyList()
                                }
                            } catch (e: Exception) {}
                            delay(3500)
                        }
                    }
                }

                // Check for updates on startup
                LaunchedEffect(Unit) {
                    try {
                        AppUpdater.checkForUpdate(this@MainActivity)
                    } catch (e: Exception) {}
                }

                // ── SYSTEM BACK-GESTURE HANDLING ──
                // Prevents accidental app exit when swiping back!
                BackHandler(enabled = true) {
                    if (selectedMovie != null || selectedSeries != null) {
                        // Return to Stream Catalog
                        selectedMovie = null
                        selectedSeries = null
                    } else if (drawerState.isOpen) {
                        scope.launch { drawerState.close() }
                    } else if (currentDestination != AppDestination.STREAM) {
                        currentDestination = AppDestination.STREAM
                    } else {
                        val now = System.currentTimeMillis()
                        if (now - lastBackPressTime < 2000) {
                            finish()
                        } else {
                            lastBackPressTime = now
                            Toast.makeText(this@MainActivity, "Press back again to exit", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                // If not logged in -> Show Pitch Black Login Screen
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
                                        loadInitialData()
                                    } else {
                                        Toast.makeText(this@MainActivity, res.body()?.error ?: "Login failed", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(this@MainActivity, "Connection error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onBiometricLogin = { },
                        canUseBiometric = false,
                        isLoading = false,
                        errorMessage = null
                    )
                } else {
                    // Main Native App Shell
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            PTSNavigationDrawerContent(
                                user = currentUser,
                                stats = systemStats,
                                currentDestination = currentDestination,
                                onNavigate = { destination ->
                                    selectedMovie = null
                                    selectedSeries = null
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
                                if (selectedMovie == null && selectedSeries == null) {
                                    TopBar(
                                        user = currentUser,
                                        stats = systemStats,
                                        onOpenDrawer = { scope.launch { drawerState.open() } },
                                        onOpenProfile = { currentDestination = AppDestination.PROFILE }
                                    )
                                }
                            },
                            bottomBar = {
                                // SINGLE Native Bottom Dock (only visible when not watching video)
                                if (selectedMovie == null && selectedSeries == null && currentDestination != AppDestination.PROFILE) {
                                    val mainTabs = listOf(
                                        DockTabItem("stream", "Stream", Icons.Default.Movie),
                                        DockTabItem("hub", "Hub", Icons.Default.Bolt),
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
                                    .background(PitchBlack)
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

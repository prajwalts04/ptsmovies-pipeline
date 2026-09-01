package com.pts.suite

import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
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
import com.pts.suite.ui.screens.login.LoginScreen
import com.pts.suite.ui.screens.profile.ProfileScreen
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
                var currentDestination by remember { mutableStateOf(AppDestination.HUB) }

                // Live Telemetry Stats State
                var systemStats by remember { mutableStateOf(SystemStats()) }

                // Map of WebViews per app to maintain state and fast switching
                val webViews = remember { mutableMapOf<AppDestination, WebView>() }

                // Back press tracking for double-tap to exit
                var lastBackPressTime by remember { mutableLongStateOf(0L) }

                // In-App Update Banner State
                var updateManifest by remember { mutableStateOf<UpdateManifest?>(null) }

                // Helper to inject SSO cookie into CookieManager for all PTS subdomains
                fun injectSsoCookie(token: String?) {
                    if (token.isNullOrEmpty()) return
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    val domains = listOf(
                        "https://hub.ptsmovies.online",
                        "https://stream.ptsmovies.online",
                        "https://files.ptsmovies.online",
                        "https://vault.ptsmovies.online",
                        "https://ssh.ptsmovies.online"
                    )
                    domains.forEach { domainUrl ->
                        cookieManager.setCookie(domainUrl, "pts_token=$token; Domain=.ptsmovies.online; Path=/; Secure; SameSite=Lax")
                    }
                    cookieManager.flush()
                }

                // Poll live telemetry stats every 3 seconds
                LaunchedEffect(authToken) {
                    if (!authToken.isNullOrEmpty()) {
                        injectSsoCookie(authToken)
                        while (true) {
                            try {
                                val service = RetrofitClient.getService(this@MainActivity)
                                val statsRes = service.getSystemStats()
                                if (statsRes.isSuccessful && statsRes.body() != null) {
                                    systemStats = statsRes.body()!!
                                }
                            } catch (e: Exception) {}
                            delay(3000)
                        }
                    }
                }

                // Check for updates on startup
                LaunchedEffect(Unit) {
                    try {
                        val manifest = AppUpdater.checkForUpdate(this@MainActivity)
                        if (manifest != null) {
                            updateManifest = manifest
                        }
                    } catch (e: Exception) {}
                }

                // ── SYSTEM BACK-GESTURE HANDLING ──
                // Prevents accidental app exit when swiping back!
                val currentWebView = webViews[currentDestination]
                BackHandler(enabled = true) {
                    if (drawerState.isOpen) {
                        scope.launch { drawerState.close() }
                    } else if (currentWebView?.canGoBack() == true) {
                        currentWebView.goBack()
                    } else if (currentDestination != AppDestination.HUB) {
                        currentDestination = AppDestination.HUB
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
                                        injectSsoCookie(token)
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
                    // Main App Shell with Navigation Drawer, Top Bar, and Dynamic Bottom Dock
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            PTSNavigationDrawerContent(
                                user = currentUser,
                                stats = systemStats,
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
                            bottomBar = {
                                // Dynamic bottom dock tailored for each active app
                                when (currentDestination) {
                                    AppDestination.STREAM -> {
                                        DynamicBottomDock(
                                            tabs = listOf(
                                                DockTabItem("all", "All", Icons.Default.Layers),
                                                DockTabItem("movies", "Movies", Icons.Default.Movie),
                                                DockTabItem("series", "Series", Icons.Default.Tv),
                                                DockTabItem("watchlist", "Watchlist", Icons.Default.Bookmark)
                                            ),
                                            selectedTabId = "all",
                                            onTabSelected = { tabId ->
                                                webViews[AppDestination.STREAM]?.evaluateJavascript(
                                                    "window.location.hash = '#$tabId'; if (typeof setActiveTab === 'function') setActiveTab('$tabId');",
                                                    null
                                                )
                                            }
                                        )
                                    }
                                    AppDestination.HUB -> {
                                        DynamicBottomDock(
                                            tabs = listOf(
                                                DockTabItem("dashboard", "Dashboard", Icons.Default.Dashboard),
                                                DockTabItem("downloads", "Downloads", Icons.Default.Bolt),
                                                DockTabItem("settings", "Settings", Icons.Default.Settings)
                                            ),
                                            selectedTabId = "dashboard",
                                            onTabSelected = { tabId ->
                                                webViews[AppDestination.HUB]?.evaluateJavascript(
                                                    "window.location.hash = '#$tabId'; if (typeof setActiveTab === 'function') setActiveTab('$tabId');",
                                                    null
                                                )
                                            }
                                        )
                                    }
                                    AppDestination.FILES -> {
                                        DynamicBottomDock(
                                            tabs = listOf(
                                                DockTabItem("browse", "Browse", Icons.Default.Folder),
                                                DockTabItem("recent", "Recent", Icons.Default.Schedule),
                                                DockTabItem("uploads", "Uploads", Icons.Default.UploadFile)
                                            ),
                                            selectedTabId = "browse",
                                            onTabSelected = { }
                                        )
                                    }
                                    AppDestination.VAULT -> {
                                        DynamicBottomDock(
                                            tabs = listOf(
                                                DockTabItem("wallet", "Wallet Deck", Icons.Default.CreditCard),
                                                DockTabItem("notes", "Notes", Icons.Default.Lock),
                                                DockTabItem("categories", "Categories", Icons.Default.Folder)
                                            ),
                                            selectedTabId = "wallet",
                                            onTabSelected = { tabId ->
                                                webViews[AppDestination.VAULT]?.evaluateJavascript(
                                                    "if (typeof switchTab === 'function') switchTab('$tabId');",
                                                    null
                                                )
                                            }
                                        )
                                    }
                                    else -> {}
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
                                    AppDestination.HUB, AppDestination.DASHBOARD -> {
                                        WebEngineView(
                                            url = "https://hub.ptsmovies.online",
                                            authToken = authToken,
                                            onWebViewCreated = { webViews[AppDestination.HUB] = it }
                                        )
                                    }
                                    AppDestination.STREAM -> {
                                        WebEngineView(
                                            url = "https://stream.ptsmovies.online",
                                            authToken = authToken,
                                            onWebViewCreated = { webViews[AppDestination.STREAM] = it }
                                        )
                                    }
                                    AppDestination.FILES -> {
                                        WebEngineView(
                                            url = "https://files.ptsmovies.online",
                                            authToken = authToken,
                                            onWebViewCreated = { webViews[AppDestination.FILES] = it }
                                        )
                                    }
                                    AppDestination.VAULT -> {
                                        WebEngineView(
                                            url = "https://vault.ptsmovies.online",
                                            authToken = authToken,
                                            onWebViewCreated = { webViews[AppDestination.VAULT] = it }
                                        )
                                    }
                                    AppDestination.TERMINAL -> {
                                        WebEngineView(
                                            url = "https://ssh.ptsmovies.online",
                                            authToken = authToken,
                                            onWebViewCreated = { webViews[AppDestination.TERMINAL] = it }
                                        )
                                    }
                                    AppDestination.DOWNLOADS -> {
                                        OfflineDownloadsScreen(
                                            onPlayOfflineFile = { _, _ -> },
                                            onBack = { currentDestination = AppDestination.HUB }
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
                                            onBack = { currentDestination = AppDestination.HUB }
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

package com.pts.suite.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.pts.suite.ui.theme.PitchBlack

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebEngineView(
    url: String,
    authToken: String?,
    onWebViewCreated: (WebView) -> Unit,
    modifier: Modifier = Modifier
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var customView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
    var fileUploadCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val clipData = result.data?.clipData
            val dataUri = result.data?.data
            val uris = when {
                clipData != null -> Array(clipData.itemCount) { clipData.getItemAt(it).uri }
                dataUri != null -> arrayOf(dataUri)
                else -> null
            }
            fileUploadCallback?.onReceiveValue(uris)
        } else {
            fileUploadCallback?.onReceiveValue(null)
        }
        fileUploadCallback = null
    }

    // Back gesture inside WebView
    BackHandler(enabled = webViewInstance?.canGoBack() == true || customView != null) {
        if (customView != null) {
            customViewCallback?.onCustomViewHidden()
            customView = null
        } else if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(PitchBlack)) {
        if (customView != null) {
            // Fullscreen video playback view
            AndroidView(
                factory = { customView!! },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(0xFF000000.toInt())

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            mediaPlaybackRequiresUserGesture = false
                            allowFileAccess = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            userAgentString = "$userAgentString PTSSuiteNative/1.0"
                        }

                        // Shared Auth Cookie Injection across PTS Subdomains
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)
                        if (!authToken.isNullOrEmpty()) {
                            val domain = if (url.contains("ptsmovies.online")) ".ptsmovies.online" else Uri.parse(url).host ?: ""
                            cookieManager.setCookie(url, "pts_token=$authToken; Domain=$domain; Path=/; Secure; SameSite=Lax")
                            cookieManager.flush()
                        }

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val reqUrl = request?.url?.toString() ?: ""
                                return if (reqUrl.startsWith("http://") || reqUrl.startsWith("https://")) {
                                    false
                                } else {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(reqUrl)))
                                        true
                                    } catch (e: Exception) {
                                        false
                                    }
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                                customView = view
                                customViewCallback = callback
                                (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            }

                            override fun onHideCustomView() {
                                customView = null
                                customViewCallback = null
                                (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                            }

                            override fun onShowFileChooser(
                                webView: WebView?,
                                filePathCallback: ValueCallback<Array<Uri>>?,
                                fileChooserParams: FileChooserParams?
                            ): Boolean {
                                fileUploadCallback?.onReceiveValue(null)
                                fileUploadCallback = filePathCallback

                                val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                                    type = "*/*"
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                }
                                try {
                                    fileChooserLauncher.launch(intent)
                                } catch (e: Exception) {
                                    fileUploadCallback = null
                                    return false
                                }
                                return true
                            }
                        }

                        // Native Download Interceptor -> Downloads directly to Android phone storage
                        setDownloadListener { downloadUrl, userAgent, contentDisposition, mimetype, contentLength ->
                            try {
                                val filename = URLUtil.guessFileName(downloadUrl, contentDisposition, mimetype)
                                val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                                    setMimeType(mimetype)
                                    val cookie = CookieManager.getInstance().getCookie(downloadUrl)
                                    addRequestHeader("Cookie", cookie)
                                    if (!authToken.isNullOrEmpty()) {
                                        addRequestHeader("Authorization", "Bearer $authToken")
                                    }
                                    setDescription("Downloading media file...")
                                    setTitle(filename)
                                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "PTS/$filename")
                                }
                                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                dm.enqueue(request)
                                Toast.makeText(context, "Download started: $filename", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Download error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }

                        loadUrl(url)
                        webViewInstance = this
                        onWebViewCreated(this)
                    }
                },
                update = { webView ->
                    if (webView.url != url) {
                        webView.loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

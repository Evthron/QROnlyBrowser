package com.example.qronlybrowser

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.qronlybrowser.ui.theme.QROnlyBrowserTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.isGranted
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.outlined.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QROnlyBrowserTheme {
                BrowserApp()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BrowserApp() {
    var url by remember { mutableStateOf("https://now.order.place/?token=TEST_TOKEN#/stor/mode/prekiosk") }
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.trim()?.let { scanned ->
            val secureScanned = if (scanned.startsWith("http://")) {
                scanned.replace("http://", "https://") // Corrected: Assign to a new variable
            } else {
                scanned
            }

            url = if (secureScanned.startsWith("http://") || secureScanned.startsWith("https://")) {
                secureScanned
            } else {
                "https://$secureScanned"
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var webView by remember { mutableStateOf<WebView?>(null) } // 新增：儲存 WebView 實例
    var showSourceDialog by remember { mutableStateOf(false) } // New: Control dialog visibility
    var sourceCode by remember { mutableStateOf("") } // New: Store source code
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
//                OutlinedTextField(
//                    value = url,
//                    onValueChange = { url = it.trim() },
//                    label = { Text("URL") },
//                    singleLine = true,
//                    modifier = Modifier
//                        .weight(1f)
//                        .padding(end = 8.dp)
//                )
                IconButton(
                    onClick = {
                        if (permissionState.status.isGranted) scanLauncher.launch(ScanOptions())
                        else permissionState.launchPermissionRequest()
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(Icons.Outlined.QrCodeScanner, contentDescription = "Scan QR Code")
                }
//                FilledTonalButton(
//                    onClick = {
//                        if (url.isNotBlank()) {
//                            url = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
//                        }
//                    }
//                ) { Text("Go") }
                // 新增：返回、前進、刷新按鈕
                IconButton(
                    onClick = { webView?.takeIf { it.canGoBack() }?.goBack() },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Go Back")
                }
                IconButton(
                    onClick = { webView?.takeIf { it.canGoForward() }?.goForward() },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = "Go Forward")
                }
                IconButton(
                    onClick = { webView?.reload() },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                }
                IconButton( // 新增：Zoom In
                    onClick = {
                        webView?.let {
                            val newZoom = (it.settings.textZoom + 10).coerceAtMost(200)
                            it.settings.textZoom = newZoom
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(Icons.Outlined.ZoomIn, contentDescription = "Zoom In")
                }
                IconButton( // 新增：Zoom Out
                    onClick = {
                        webView?.let {
                            val newZoom = (it.settings.textZoom - 10).coerceAtLeast(50)
                            it.settings.textZoom = newZoom
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(Icons.Outlined.ZoomOut, contentDescription = "Zoom Out")
                }
                IconButton( // New: Source code button
                    onClick = {
                        webView?.evaluateJavascript("(function() { return document.documentElement.outerHTML; })();") { result ->
                            sourceCode = result?.trim('"')?.replace("\\n", "\n")?.replace("\\t", "\t") ?: "Failed to load source code"
                            showSourceDialog = true
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(Icons.Outlined.Code, contentDescription = "View Source Code")
                }
            }
        }
    ) { padding ->
        // New: Source code dialog
        if (showSourceDialog) {
            AlertDialog(
                onDismissRequest = { showSourceDialog = false },
                title = { Text("Source Code") },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(sourceCode, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSourceDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true // 新增：啟用 DOM 儲存
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW // 新增：允許混合內容
                    settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; Pixel 4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36" // 新增
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                            if (request.hasGesture()) {
                                val currentHost = Uri.parse(url).host ?: return false
                                val targetHost = request.url.host ?: return false
                                return if (currentHost == targetHost) {
                                    view.loadUrl(request.url.toString())
                                    false
                                } else {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar("Blocked: ${request.url.host}")
                                    }
                                    true
                                }
                            }
                            return false
                        }
                        override fun onReceivedError(
                            view: WebView,
                            request: WebResourceRequest,
                            error: WebResourceError
                        ) {
                            // 新增：顯示加載錯誤
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarHostState.showSnackbar(
                                    "Failed to load: ${error.description} (Code: ${error.errorCode})"
                                )
                            }
                        }
                    }

                    loadUrl(url)
                    webView = this
                }
            },
            update = { it.loadUrl(url) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun BrowserAppPreview() {
    QROnlyBrowserTheme {
        BrowserApp()
    }
}
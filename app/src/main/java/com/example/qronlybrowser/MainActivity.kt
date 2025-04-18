package com.example.qronlybrowser

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

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
    var url by remember { mutableStateOf("https://example.com") }
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.trim()?.let { scanned ->
            url = if (scanned.startsWith("http://") || scanned.startsWith("https://")) scanned else "https://$scanned"
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    var webView by remember { mutableStateOf<WebView?>(null) } // 新增：儲存 WebView 實例
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
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
            }
        }
    ) { padding ->
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
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
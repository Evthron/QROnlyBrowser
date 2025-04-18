package com.example.qronlybrowser

import android.Manifest
import android.os.Bundle
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
    var url by remember { mutableStateOf("https://www.google.com") }
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.trim()?.let { scanned ->
            url = if (scanned.startsWith("http://") || scanned.startsWith("https://")) scanned else "https://$scanned"
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it.trim() },
                    label = { Text("URL") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                FilledTonalButton(
                    onClick = {
                        if (permissionState.status.isGranted) scanLauncher.launch(ScanOptions())
                        else permissionState.launchPermissionRequest()
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) { Text("Scan") }
                FilledTonalButton(
                    onClick = {
                        if (url.isNotBlank()) {
                            url = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
                        }
                    }
                ) { Text("Go") }
            }
        }
    ) { padding ->
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = WebViewClient()
                    loadUrl(url)
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
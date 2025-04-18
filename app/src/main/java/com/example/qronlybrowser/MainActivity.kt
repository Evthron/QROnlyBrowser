package com.example.qronlybrowser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.qronlybrowser.ui.theme.QROnlyBrowserTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QROnlyBrowserTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                    BrowserApp()
                }
            }

        }
    }
}

@Composable // UI component
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun CounterApp(){
    var counter by remember { mutableStateOf(0) }
    // 使用 Column 來垂直排列元件
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 顯示計數器的值
        Text(
            text = "Count: $counter",
            fontSize = 24.sp
        )

        // 添加一個按鈕
        Button(
            onClick = { counter++ }, // 點擊按鈕時，增加計數器的值
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Click Me")
        }
    }
}

@Preview(showBackground = true)

@Composable
fun GreetingPreview() {
    QROnlyBrowserTheme {
        Greeting("Android")
    }
}

@Composable
fun WebViewExample() {
    // 使用 AndroidView 將 WebView 嵌入到 Compose 中
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                // 設置 WebView 的基本配置
                settings.javaScriptEnabled = true // 啟用 JavaScript
                webViewClient = WebViewClient()  // 確保頁面在 WebView 內加載
                loadUrl("https://evthron.github.io") // 加載初始網址
            }
        },
        //modifier = Modifier.fillMaxSize() // 讓 WebView 填滿整個畫面
    )
}

@Composable
fun BrowserApp() {
    var url by remember { mutableStateOf("https://google.com") } // 存儲當前網址
    var webViewUrl by remember { mutableStateOf(url) } // WebView 加載的網址

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 地址欄和按鈕
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            TextField(
                value = url,
                onValueChange = {
                    url = it.trim() // 去除多餘的空格
                },
                label = { Text("Enter URL") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp)) // 按鈕與輸入框之間的間距
            Button(
                onClick = {
                    if (url.isNotBlank()) { // 確保輸入不為空
                        if (!url.startsWith("http://") && !url.startsWith("https://")) {
                            url = "https://$url" // 自動補全 http 或 https
                        }
                        webViewUrl = url // 更新 WebView 的網址
                    }
                }
            ) {
                Text("Go")
            }
        }

        // WebView 區域
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = WebViewClient()

                    // 初始加載網址
                    loadUrl(webViewUrl)
                }
            },
            update = { webView ->
                // 當 webViewUrl 發生變化時，重新加載網址
                webView.loadUrl(webViewUrl)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
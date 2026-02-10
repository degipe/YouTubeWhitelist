package io.github.degipe.youtubewhitelist.feature.sleep.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.Keep
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val sleepDarkColors = darkColorScheme(
    background = Color(0xFF0A0A1A),
    surface = Color(0xFF121230),
    primary = Color(0xFF7B68EE),
    onBackground = Color(0xFFB0B0D0),
    onSurface = Color(0xFFB0B0D0),
    onPrimary = Color.White
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepModeScreen(
    viewModel: SleepModeViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MaterialTheme(colorScheme = sleepDarkColors) {
        Scaffold(
            containerColor = sleepDarkColors.background,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "  Sleep Mode",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            viewModel.stopTimer()
                            onNavigateBack()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = sleepDarkColors.surface,
                        titleContentColor = sleepDarkColors.onSurface,
                        navigationIconContentColor = sleepDarkColors.onSurface
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(sleepDarkColors.background)
            ) {
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = sleepDarkColors.primary)
                        }
                    }
                    uiState.videos.isEmpty() -> {
                        EmptySleepContent()
                    }
                    uiState.timerState == TimerState.SELECTING -> {
                        TimerSelectionContent(
                            selectedDuration = uiState.selectedDurationMinutes,
                            onSelectDuration = viewModel::selectDuration,
                            onStart = viewModel::startTimer
                        )
                    }
                    uiState.timerState == TimerState.RUNNING -> {
                        SleepPlaybackContent(
                            uiState = uiState,
                            onVideoEnded = viewModel::onVideoEnded,
                            onStop = viewModel::stopTimer
                        )
                    }
                    uiState.timerState == TimerState.EXPIRED -> {
                        TimerExpiredContent(
                            onDismiss = {
                                viewModel.stopTimer()
                                onNavigateBack()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySleepContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Bedtime,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = sleepDarkColors.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No videos available",
                style = MaterialTheme.typography.titleMedium,
                color = sleepDarkColors.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add some videos first, then come back for sleep mode",
                style = MaterialTheme.typography.bodyMedium,
                color = sleepDarkColors.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun TimerSelectionContent(
    selectedDuration: Int,
    onSelectDuration: (Int) -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Bedtime,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = sleepDarkColors.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Set Sleep Timer",
            style = MaterialTheme.typography.headlineMedium,
            color = sleepDarkColors.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf(15, 30, 45, 60).forEach { minutes ->
                FilterChip(
                    selected = selectedDuration == minutes,
                    onClick = { onSelectDuration(minutes) },
                    label = {
                        Text("${minutes}m")
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = sleepDarkColors.primary,
                        selectedLabelColor = sleepDarkColors.onPrimary,
                        containerColor = sleepDarkColors.surface,
                        labelColor = sleepDarkColors.onSurface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(
                containerColor = sleepDarkColors.primary,
                contentColor = sleepDarkColors.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text("  Start Sleep Mode")
        }
    }
}

@Composable
private fun SleepPlaybackContent(
    uiState: SleepModeUiState,
    onVideoEnded: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Timer display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            val minutes = uiState.remainingSeconds / 60
            val seconds = uiState.remainingSeconds % 60
            Text(
                text = "%d:%02d".format(minutes, seconds),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 48.sp,
                    letterSpacing = 4.sp
                ),
                color = if (uiState.remainingSeconds <= 120)
                    sleepDarkColors.primary.copy(alpha = 0.7f)
                else
                    sleepDarkColors.onBackground
            )
        }

        // Video player
        uiState.currentVideo?.let { video ->
            SleepYouTubePlayer(
                youtubeId = video.youtubeId,
                onVideoEnded = onVideoEnded,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = video.title,
                style = MaterialTheme.typography.titleSmall,
                color = sleepDarkColors.onBackground.copy(alpha = 0.8f),
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Stop button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onStop,
                colors = ButtonDefaults.buttonColors(
                    containerColor = sleepDarkColors.surface,
                    contentColor = sleepDarkColors.onSurface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null
                )
                Text("  Stop")
            }
        }
    }
}

@Composable
private fun TimerExpiredContent(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Bedtime,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = sleepDarkColors.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Good night!",
                style = MaterialTheme.typography.headlineMedium,
                color = sleepDarkColors.onBackground
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = sleepDarkColors.primary,
                    contentColor = sleepDarkColors.onPrimary
                )
            ) {
                Text("Done")
            }
        }
    }
}

@Keep
private class SleepVideoEndedBridge(
    private val onVideoEnded: () -> Unit,
    private val onEmbedError: () -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onStateChange(state: Int) {
        // YT.PlayerState.ENDED == 0
        if (state == 0) {
            handler.post { onVideoEnded() }
        }
    }

    @JavascriptInterface
    fun onError(errorCode: Int) {
        // 101, 150 = embedding disabled by video owner
        if (errorCode == 101 || errorCode == 150) {
            handler.post { onEmbedError() }
        }
    }

    @JavascriptInterface
    fun onReady() { }
}

private fun buildSleepPlayerHtml(videoId: String, origin: String): String {
    return """
        <!DOCTYPE html>
        <html>
        <style type="text/css">
            html, body {
                height: 100%;
                width: 100%;
                margin: 0;
                padding: 0;
                background-color: #000000;
                overflow: hidden;
                position: fixed;
            }
        </style>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <script defer src="https://www.youtube.com/iframe_api"></script>
        </head>
        <body>
            <div id="player"></div>
        </body>
        <script type="text/javascript">
            var player;
            function onYouTubeIframeAPIReady() {
                player = new YT.Player('player', {
                    height: '100%',
                    width: '100%',
                    videoId: '$videoId',
                    playerVars: {
                        autoplay: 1,
                        controls: 0,
                        enablejsapi: 1,
                        fs: 0,
                        origin: '$origin',
                        rel: 0,
                        iv_load_policy: 3,
                        modestbranding: 1,
                        playsinline: 1
                    },
                    events: {
                        onReady: function(event) {
                            if (typeof AndroidBridge !== 'undefined') {
                                AndroidBridge.onReady();
                            }
                        },
                        onStateChange: function(event) {
                            if (typeof AndroidBridge !== 'undefined') {
                                AndroidBridge.onStateChange(event.data);
                            }
                        },
                        onError: function(event) {
                            if (typeof AndroidBridge !== 'undefined') {
                                AndroidBridge.onError(event.data);
                            }
                        }
                    }
                });
            }
        </script>
        </html>
    """.trimIndent()
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SleepYouTubePlayer(
    youtubeId: String,
    onVideoEnded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val webViewRef = remember { mutableStateOf<WebView?>(null) }

    DisposableEffect(youtubeId) {
        onDispose {
            webViewRef.value?.let { wv ->
                wv.loadUrl("about:blank")
                wv.stopLoading()
                wv.clearHistory()
                wv.destroy()
            }
            webViewRef.value = null
        }
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewRef.value = this

                val origin = "https://${context.packageName}"

                settings.javaScriptEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT

                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this, true)

                addJavascriptInterface(
                    SleepVideoEndedBridge(onVideoEnded, onVideoEnded),
                    "AndroidBridge"
                )

                webChromeClient = object : WebChromeClient() {
                    override fun getDefaultVideoPoster(): Bitmap? {
                        return super.getDefaultVideoPoster()
                            ?: Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565)
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        // Block all navigation to prevent escaping the app
                        return true
                    }
                }

                val html = buildSleepPlayerHtml(youtubeId, origin)
                loadDataWithBaseURL(origin, html, "text/html", "utf-8", null)
            }
        },
        update = { /* Re-creation handled by DisposableEffect keyed on youtubeId */ },
        modifier = modifier
    )
}

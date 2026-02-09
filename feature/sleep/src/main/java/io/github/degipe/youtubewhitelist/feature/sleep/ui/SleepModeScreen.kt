package io.github.degipe.youtubewhitelist.feature.sleep.ui

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.Keep
import androidx.compose.animation.animateColorAsState
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
private class SleepVideoEndedBridge(private val callback: () -> Unit) {
    @JavascriptInterface
    fun onVideoEnded(durationSeconds: Int) {
        callback()
    }
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

                settings.javaScriptEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                settings.safeBrowsingEnabled = true

                webChromeClient = WebChromeClient()
                webViewClient = WebViewClient()

                addJavascriptInterface(
                    SleepVideoEndedBridge(onVideoEnded),
                    "AndroidBridge"
                )

                val html = SleepPlayerHtml.generate(youtubeId)
                loadDataWithBaseURL(
                    "https://www.youtube.com",
                    html,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        update = { wv ->
            val html = SleepPlayerHtml.generate(youtubeId)
            wv.loadDataWithBaseURL(
                "https://www.youtube.com",
                html,
                "text/html",
                "UTF-8",
                null
            )
        },
        modifier = modifier
    )
}

private object SleepPlayerHtml {
    fun generate(videoId: String): String = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { margin: 0; background: #0A0A1A; overflow: hidden; }
                #player { width: 100%; height: 100%; }
            </style>
        </head>
        <body>
            <div id="player"></div>
            <script src="https://www.youtube.com/iframe_api"></script>
            <script>
                var player;
                function onYouTubeIframeAPIReady() {
                    player = new YT.Player('player', {
                        videoId: '$videoId',
                        playerVars: {
                            autoplay: 1,
                            controls: 0,
                            rel: 0,
                            modestbranding: 1,
                            iv_load_policy: 3,
                            fs: 0
                        },
                        events: {
                            onStateChange: function(event) {
                                if (event.data === YT.PlayerState.ENDED) {
                                    var duration = Math.round(player.getDuration());
                                    AndroidBridge.onVideoEnded(duration);
                                }
                            }
                        }
                    });
                }
            </script>
        </body>
        </html>
    """.trimIndent()
}

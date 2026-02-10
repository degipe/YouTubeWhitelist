package io.github.degipe.youtubewhitelist.feature.kid.ui.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.Keep
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import io.github.degipe.youtubewhitelist.core.data.model.WhitelistItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    viewModel: VideoPlayerViewModel,
    onNavigateBack: () -> Unit,
    onParentAccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? Activity
    var fullscreenView by remember { mutableStateOf<View?>(null) }
    var fullscreenCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    val exitFullscreen: () -> Unit = {
        fullscreenView?.let { view ->
            (activity?.window?.decorView as? FrameLayout)?.removeView(view)
        }
        fullscreenCallback?.onCustomViewHidden()
        fullscreenView = null
        fullscreenCallback = null
        activity?.let { act ->
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            WindowCompat.setDecorFitsSystemWindows(act.window, true)
            WindowInsetsControllerCompat(act.window, act.window.decorView).show(
                WindowInsetsCompat.Type.systemBars()
            )
        }
    }

    BackHandler(enabled = fullscreenView != null) {
        exitFullscreen()
    }

    DisposableEffect(Unit) {
        onDispose {
            if (fullscreenView != null) exitFullscreen()
        }
    }

    // When overlay appears (sleep timer or time limit), exit fullscreen
    val shouldBlock = uiState.isSleepTimerExpired || uiState.isTimeLimitReached
    LaunchedEffect(shouldBlock) {
        if (shouldBlock && fullscreenView != null) {
            exitFullscreen()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.videoTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Remaining time badge
                    uiState.remainingTimeFormatted?.let { remaining ->
                        Text(
                            text = "Time remaining: $remaining",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    // YouTube Player WebView
                    YouTubePlayer(
                        youtubeId = uiState.youtubeId,
                        shouldPause = uiState.isSleepTimerExpired || uiState.isTimeLimitReached,
                        onVideoEnded = { viewModel.playNext() },
                        onEmbedError = { viewModel.playNext() },
                        onEnterFullscreen = { view, callback ->
                            fullscreenView = view
                            fullscreenCallback = callback
                            activity?.let { act ->
                                view.setBackgroundColor(android.graphics.Color.BLACK)
                                (act.window.decorView as? FrameLayout)?.addView(
                                    view,
                                    FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT
                                    )
                                )
                                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                WindowCompat.setDecorFitsSystemWindows(act.window, false)
                                WindowInsetsControllerCompat(act.window, act.window.decorView).apply {
                                    hide(WindowInsetsCompat.Type.systemBars())
                                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                                }
                            }
                        },
                        onExitFullscreen = exitFullscreen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                    )

                    // Video info + controls
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = uiState.videoTitle,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Next/Previous controls
                        if (uiState.siblingVideos.size > 1) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.playPrevious() },
                                    enabled = uiState.hasPrevious
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipPrevious,
                                        contentDescription = "Previous",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Text(
                                    text = "${uiState.currentIndex + 1} / ${uiState.siblingVideos.size}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                IconButton(
                                    onClick = { viewModel.playNext() },
                                    enabled = uiState.hasNext
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipNext,
                                        contentDescription = "Next",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Up next list
                    if (uiState.siblingVideos.size > 1) {
                        Text(
                            text = "Up Next",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
                        ) {
                            val filteredSiblings = uiState.siblingVideos
                                .mapIndexed { index, item -> index to item }
                                .filterNot { it.second.youtubeId == uiState.youtubeId }
                            items(filteredSiblings, key = { it.second.id }) { (index, video) ->
                                UpNextCard(
                                    video = video,
                                    onClick = { viewModel.playVideoAt(index) }
                                )
                            }
                        }
                    }
                }
            }

            // Time's Up overlay
            if (uiState.isTimeLimitReached) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Time's Up!",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Your daily screen time is over.\nAsk a parent to continue.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        FloatingActionButton(
                            onClick = onParentAccess,
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Parent Mode")
                        }
                    }
                }
            }

            // Good Night overlay (sleep timer expired)
            if (uiState.isSleepTimerExpired) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0A0A1A).copy(alpha = 0.98f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color(0xFF7B68EE)
                        )
                        Text(
                            text = "Good Night!",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color(0xFFB0B0D0)
                        )
                        Text(
                            text = "Time to sleep.\nSweet dreams!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = Color(0xFFB0B0D0).copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        FloatingActionButton(
                            onClick = onParentAccess,
                            containerColor = Color(0xFF7B68EE)
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Parent Mode",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Keep
private class VideoEndedBridge(
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

private fun buildYouTubePlayerHtml(videoId: String, origin: String, showControls: Boolean): String {
    val controls = if (showControls) 1 else 0
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
                        controls: $controls,
                        enablejsapi: 1,
                        fs: 1,
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
private fun YouTubePlayer(
    youtubeId: String,
    shouldPause: Boolean = false,
    onVideoEnded: () -> Unit,
    onEmbedError: () -> Unit = {},
    onEnterFullscreen: (View, WebChromeClient.CustomViewCallback) -> Unit,
    onExitFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val webViewRef = remember { mutableStateOf<WebView?>(null) }

    // Pause video when overlay blocks playback (sleep timer / time limit)
    LaunchedEffect(shouldPause) {
        if (shouldPause) {
            webViewRef.value?.post {
                webViewRef.value?.evaluateJavascript(
                    "if(player && player.pauseVideo) player.pauseVideo();",
                    null
                )
            }
        }
    }

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
        factory = { ctx ->
            WebView(ctx).apply {
                webViewRef.value = this

                val origin = "https://${ctx.packageName}"

                settings.javaScriptEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT

                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this, true)

                addJavascriptInterface(
                    VideoEndedBridge(onVideoEnded, onEmbedError),
                    "AndroidBridge"
                )

                webChromeClient = object : WebChromeClient() {
                    override fun getDefaultVideoPoster(): Bitmap? {
                        return super.getDefaultVideoPoster()
                            ?: Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565)
                    }

                    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                        if (view != null && callback != null) {
                            onEnterFullscreen(view, callback)
                        }
                    }

                    override fun onHideCustomView() {
                        onExitFullscreen()
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        // Block all navigation to prevent kids from escaping the app
                        // (e.g., "Watch on YouTube" links on embed-disabled videos)
                        return true
                    }
                }

                val html = buildYouTubePlayerHtml(youtubeId, origin, showControls = true)
                loadDataWithBaseURL(origin, html, "text/html", "utf-8", null)
            }
        },
        update = { /* Re-creation handled by DisposableEffect keyed on youtubeId */ },
        modifier = modifier
    )
}

@Composable
private fun UpNextCard(video: WhitelistItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = video.title,
                modifier = Modifier
                    .width(120.dp)
                    .aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = video.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

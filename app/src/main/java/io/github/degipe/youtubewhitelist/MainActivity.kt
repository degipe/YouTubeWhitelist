package io.github.degipe.youtubewhitelist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import io.github.degipe.youtubewhitelist.navigation.AppNavigation
import io.github.degipe.youtubewhitelist.ui.theme.YouTubeWhitelistTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YouTubeWhitelistTheme {
                AppNavigation()
            }
        }
    }
}

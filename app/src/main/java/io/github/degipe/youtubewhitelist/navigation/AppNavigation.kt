package io.github.degipe.youtubewhitelist.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

/**
 * Top-level navigation for the app.
 * Routes between parent mode, kid mode, and authentication screens.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Navigation graph will be built incrementally across sessions
}

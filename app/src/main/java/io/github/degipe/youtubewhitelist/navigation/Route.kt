package io.github.degipe.youtubewhitelist.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable data object Splash : Route
    @Serializable data object SignIn : Route
    @Serializable data object PinSetup : Route
    @Serializable data object PinEntry : Route
    @Serializable data object PinChange : Route
    @Serializable data object ProfileCreation : Route
    @Serializable data object KidHome : Route
    @Serializable data object ParentDashboard : Route
    @Serializable data class WhitelistManager(val profileId: String) : Route
    @Serializable data class WebViewBrowser(val profileId: String) : Route
}

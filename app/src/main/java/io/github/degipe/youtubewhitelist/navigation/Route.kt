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
    @Serializable data class KidHome(val profileId: String) : Route
    @Serializable data object ParentDashboard : Route
    @Serializable data class WhitelistManager(val profileId: String) : Route
    @Serializable data class WebViewBrowser(val profileId: String) : Route
    @Serializable data class ChannelDetail(
        val profileId: String,
        val channelTitle: String,
        val channelThumbnailUrl: String
    ) : Route
    @Serializable data class VideoPlayer(
        val profileId: String,
        val videoId: String,
        val channelTitle: String? = null
    ) : Route
    @Serializable data class KidSearch(val profileId: String) : Route
    @Serializable data class SleepMode(val profileId: String) : Route
}

package io.github.degipe.youtubewhitelist.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.degipe.youtubewhitelist.feature.kid.ui.channel.ChannelDetailScreen
import io.github.degipe.youtubewhitelist.feature.kid.ui.channel.ChannelDetailViewModel
import io.github.degipe.youtubewhitelist.feature.kid.ui.home.KidHomeScreen
import io.github.degipe.youtubewhitelist.feature.kid.ui.home.KidHomeViewModel
import io.github.degipe.youtubewhitelist.feature.kid.ui.player.VideoPlayerScreen
import io.github.degipe.youtubewhitelist.feature.kid.ui.player.VideoPlayerViewModel
import io.github.degipe.youtubewhitelist.feature.kid.ui.playlist.PlaylistDetailScreen
import io.github.degipe.youtubewhitelist.feature.kid.ui.playlist.PlaylistDetailViewModel
import io.github.degipe.youtubewhitelist.feature.kid.ui.search.KidSearchScreen
import io.github.degipe.youtubewhitelist.feature.kid.ui.search.KidSearchViewModel
import io.github.degipe.youtubewhitelist.feature.sleep.ui.SleepModeScreen
import io.github.degipe.youtubewhitelist.feature.sleep.ui.SleepModeViewModel
import io.github.degipe.youtubewhitelist.feature.parent.ui.browser.WebViewBrowserScreen
import io.github.degipe.youtubewhitelist.feature.parent.ui.browser.WebViewBrowserViewModel
import io.github.degipe.youtubewhitelist.feature.parent.ui.dashboard.ParentDashboardScreen
import io.github.degipe.youtubewhitelist.feature.parent.ui.dashboard.ParentDashboardViewModel
import io.github.degipe.youtubewhitelist.feature.parent.ui.exportimport.ExportImportScreen
import io.github.degipe.youtubewhitelist.feature.parent.ui.exportimport.ExportImportViewModel
import io.github.degipe.youtubewhitelist.feature.parent.ui.profile.ProfileEditScreen
import io.github.degipe.youtubewhitelist.feature.parent.ui.profile.ProfileEditViewModel
import io.github.degipe.youtubewhitelist.feature.parent.ui.stats.WatchStatsScreen
import io.github.degipe.youtubewhitelist.feature.parent.ui.stats.WatchStatsViewModel
import io.github.degipe.youtubewhitelist.feature.parent.ui.whitelist.WhitelistManagerScreen
import io.github.degipe.youtubewhitelist.feature.parent.ui.about.AboutScreen
import io.github.degipe.youtubewhitelist.feature.parent.ui.whitelist.WhitelistManagerViewModel
import io.github.degipe.youtubewhitelist.ui.screen.profile.ProfileSelectorScreen
import io.github.degipe.youtubewhitelist.ui.screen.profile.ProfileSelectorViewModel
import io.github.degipe.youtubewhitelist.ui.screen.auth.SignInScreen
import io.github.degipe.youtubewhitelist.ui.screen.pin.PinChangeScreen
import io.github.degipe.youtubewhitelist.ui.screen.pin.PinEntryScreen
import io.github.degipe.youtubewhitelist.ui.screen.pin.PinSetupScreen
import io.github.degipe.youtubewhitelist.ui.screen.profile.ProfileCreationScreen
import io.github.degipe.youtubewhitelist.ui.screen.splash.SplashScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Route.Splash
    ) {
        composable<Route.Splash> {
            SplashScreen(
                onFirstRun = {
                    navController.navigate(Route.SignIn) {
                        popUpTo<Route.Splash> { inclusive = true }
                    }
                },
                onReturningUser = { profileId ->
                    navController.navigate(Route.KidHome(profileId)) {
                        popUpTo<Route.Splash> { inclusive = true }
                    }
                },
                onMultipleProfiles = {
                    navController.navigate(Route.ProfileSelector) {
                        popUpTo<Route.Splash> { inclusive = true }
                    }
                }
            )
        }

        composable<Route.SignIn> {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(Route.PinSetup) {
                        popUpTo<Route.SignIn> { inclusive = true }
                    }
                }
            )
        }

        composable<Route.PinSetup> {
            PinSetupScreen(
                onPinSet = {
                    navController.navigate(Route.ProfileCreation) {
                        popUpTo<Route.PinSetup> { inclusive = true }
                    }
                }
            )
        }

        composable<Route.PinEntry> {
            val pinContext = LocalContext.current
            PinEntryScreen(
                onPinVerified = {
                    // Stop screen pinning when parent accesses parent mode
                    try {
                        (pinContext as? Activity)?.stopLockTask()
                    } catch (_: Exception) {
                        // Screen pinning was not active
                    }
                    navController.navigate(Route.ParentDashboard) {
                        popUpTo<Route.PinEntry> { inclusive = true }
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        composable<Route.PinChange> {
            PinChangeScreen(
                onPinChanged = {
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        composable<Route.ProfileCreation> {
            ProfileCreationScreen(
                onProfileCreated = { profileId ->
                    navController.navigate(Route.KidHome(profileId)) {
                        popUpTo<Route.ProfileCreation> { inclusive = true }
                    }
                }
            )
        }

        composable<Route.KidHome> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.KidHome>()
            val context = LocalContext.current
            val viewModel: KidHomeViewModel =
                hiltViewModel<KidHomeViewModel, KidHomeViewModel.Factory> { factory ->
                    factory.create(route.profileId)
                }

            // Start screen pinning when entering kid mode
            LaunchedEffect(Unit) {
                try {
                    (context as? Activity)?.startLockTask()
                } catch (_: Exception) {
                    // Screen pinning not available or already active
                }
            }

            KidHomeScreen(
                viewModel = viewModel,
                onParentAccess = {
                    navController.navigate(Route.PinEntry)
                },
                onSearchClick = {
                    navController.navigate(Route.KidSearch(route.profileId))
                },
                onChannelClick = { channelId, channelTitle, thumbnailUrl ->
                    navController.navigate(
                        Route.ChannelDetail(route.profileId, channelId, channelTitle, thumbnailUrl)
                    )
                },
                onVideoClick = { videoId, videoTitle, channelTitle ->
                    navController.navigate(
                        Route.VideoPlayer(
                            profileId = route.profileId,
                            videoId = videoId,
                            videoTitle = videoTitle,
                            channelTitle = channelTitle
                        )
                    )
                },
                onPlaylistClick = { youtubeId, title, thumbnailUrl ->
                    navController.navigate(
                        Route.PlaylistDetail(route.profileId, youtubeId, title, thumbnailUrl)
                    )
                }
            )
        }

        composable<Route.ChannelDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.ChannelDetail>()
            val viewModel: ChannelDetailViewModel =
                hiltViewModel<ChannelDetailViewModel, ChannelDetailViewModel.Factory> { factory ->
                    factory.create(route.channelId, route.channelTitle)
                }
            ChannelDetailScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onVideoClick = { videoId, videoTitle, channelTitle ->
                    navController.navigate(
                        Route.VideoPlayer(
                            profileId = route.profileId,
                            videoId = videoId,
                            videoTitle = videoTitle,
                            channelTitle = channelTitle
                        )
                    )
                }
            )
        }

        composable<Route.VideoPlayer> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.VideoPlayer>()
            val viewModel: VideoPlayerViewModel =
                hiltViewModel<VideoPlayerViewModel, VideoPlayerViewModel.Factory> { factory ->
                    factory.create(route.profileId, route.videoId, route.videoTitle, route.channelTitle)
                }
            VideoPlayerScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onParentAccess = {
                    navController.navigate(Route.PinEntry)
                }
            )
        }

        composable<Route.KidSearch> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.KidSearch>()
            val viewModel: KidSearchViewModel =
                hiltViewModel<KidSearchViewModel, KidSearchViewModel.Factory> { factory ->
                    factory.create(route.profileId)
                }
            KidSearchScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onVideoClick = { videoId, videoTitle, channelTitle ->
                    navController.navigate(
                        Route.VideoPlayer(
                            profileId = route.profileId,
                            videoId = videoId,
                            videoTitle = videoTitle,
                            channelTitle = channelTitle
                        )
                    )
                },
                onChannelClick = { channelId, channelTitle, thumbnailUrl ->
                    navController.navigate(
                        Route.ChannelDetail(route.profileId, channelId, channelTitle, thumbnailUrl)
                    )
                },
                onPlaylistClick = { youtubeId, title, thumbnailUrl ->
                    navController.navigate(
                        Route.PlaylistDetail(route.profileId, youtubeId, title, thumbnailUrl)
                    )
                }
            )
        }

        composable<Route.PlaylistDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.PlaylistDetail>()
            val viewModel: PlaylistDetailViewModel =
                hiltViewModel<PlaylistDetailViewModel, PlaylistDetailViewModel.Factory> { factory ->
                    factory.create(route.profileId, route.playlistId)
                }
            PlaylistDetailScreen(
                viewModel = viewModel,
                playlistTitle = route.playlistTitle,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onVideoClick = { videoId, videoTitle, channelTitle ->
                    navController.navigate(
                        Route.VideoPlayer(
                            profileId = route.profileId,
                            videoId = videoId,
                            videoTitle = videoTitle,
                            channelTitle = channelTitle
                        )
                    )
                }
            )
        }

        composable<Route.ParentDashboard> {
            val viewModel: ParentDashboardViewModel = hiltViewModel()
            ParentDashboardScreen(
                viewModel = viewModel,
                onBackToKidMode = { profileId ->
                    navController.navigate(Route.KidHome(profileId)) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onChangePin = {
                    navController.navigate(Route.PinChange)
                },
                onOpenWhitelistManager = { profileId ->
                    navController.navigate(Route.WhitelistManager(profileId))
                },
                onOpenBrowser = { profileId ->
                    navController.navigate(Route.WebViewBrowser(profileId))
                },
                onOpenSleepMode = { profileId ->
                    navController.navigate(Route.SleepMode(profileId))
                },
                onEditProfile = { profileId ->
                    navController.navigate(Route.ProfileEdit(profileId))
                },
                onWatchStats = { profileId ->
                    navController.navigate(Route.WatchStats(profileId))
                },
                onExportImport = { parentAccountId ->
                    navController.navigate(Route.ExportImport(parentAccountId))
                },
                onCreateProfile = {
                    navController.navigate(Route.ProfileCreation)
                },
                onAbout = {
                    navController.navigate(Route.About)
                }
            )
        }

        composable<Route.About> {
            AboutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Route.WhitelistManager> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.WhitelistManager>()
            val viewModel: WhitelistManagerViewModel =
                hiltViewModel<WhitelistManagerViewModel, WhitelistManagerViewModel.Factory> { factory ->
                    factory.create(route.profileId)
                }
            WhitelistManagerScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Route.WebViewBrowser> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.WebViewBrowser>()
            val viewModel: WebViewBrowserViewModel = hiltViewModel()
            WebViewBrowserScreen(
                viewModel = viewModel,
                profileId = route.profileId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Route.SleepMode> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.SleepMode>()
            val viewModel: SleepModeViewModel =
                hiltViewModel<SleepModeViewModel, SleepModeViewModel.Factory> { factory ->
                    factory.create(route.profileId)
                }
            SleepModeScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Route.ProfileSelector> {
            val viewModel: ProfileSelectorViewModel = hiltViewModel()
            ProfileSelectorScreen(
                viewModel = viewModel,
                onProfileSelected = { profileId ->
                    navController.navigate(Route.KidHome(profileId)) {
                        popUpTo<Route.ProfileSelector> { inclusive = true }
                    }
                },
                onParentAccess = {
                    navController.navigate(Route.PinEntry)
                }
            )
        }

        composable<Route.ProfileEdit> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.ProfileEdit>()
            val viewModel: ProfileEditViewModel =
                hiltViewModel<ProfileEditViewModel, ProfileEditViewModel.Factory> { factory ->
                    factory.create(route.profileId)
                }
            ProfileEditScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onProfileDeleted = {
                    navController.popBackStack()
                }
            )
        }

        composable<Route.WatchStats> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.WatchStats>()
            val viewModel: WatchStatsViewModel =
                hiltViewModel<WatchStatsViewModel, WatchStatsViewModel.Factory> { factory ->
                    factory.create(route.profileId)
                }
            WatchStatsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Route.ExportImport> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.ExportImport>()
            val viewModel: ExportImportViewModel =
                hiltViewModel<ExportImportViewModel, ExportImportViewModel.Factory> { factory ->
                    factory.create(route.parentAccountId)
                }
            ExportImportScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

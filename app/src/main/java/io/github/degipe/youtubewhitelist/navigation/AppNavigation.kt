package io.github.degipe.youtubewhitelist.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.degipe.youtubewhitelist.ui.screen.auth.SignInScreen
import io.github.degipe.youtubewhitelist.ui.screen.kid.KidHomeScreen
import io.github.degipe.youtubewhitelist.ui.screen.parent.ParentDashboardScreen
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
                onReturningUser = {
                    navController.navigate(Route.KidHome) {
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
            PinEntryScreen(
                onPinVerified = {
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
                onProfileCreated = {
                    navController.navigate(Route.KidHome) {
                        popUpTo<Route.ProfileCreation> { inclusive = true }
                    }
                }
            )
        }

        composable<Route.KidHome> {
            KidHomeScreen(
                onParentAccess = {
                    navController.navigate(Route.PinEntry)
                }
            )
        }

        composable<Route.ParentDashboard> {
            ParentDashboardScreen(
                onBackToKidMode = {
                    navController.navigate(Route.KidHome) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onChangePin = {
                    navController.navigate(Route.PinChange)
                }
            )
        }
    }
}

package io.agora.myapplication.ui.app

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.agora.myapplication.services.Navigator
import io.agora.myapplication.ui.scenes.LoginScene
import io.agora.myapplication.ui.scenes.RTCScene
import io.agora.myapplication.ui.scenes.RTMScene
import io.agora.myapplication.ui.scenes.SettingsScene
import io.agora.myapplication.viewmodels.LoginViewModel
import io.agora.myapplication.viewmodels.RTCViewModel
import io.agora.myapplication.viewmodels.RTMViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun DemoApp(navigator: Navigator) {
    val navigationController = rememberNavController()

    LaunchedEffect("navigation") {
        navigator.navigationItemFlow.onEach {
            if (navigationController.currentDestination?.route == LoginNavItem.route) {
                navigationController.navigate(it.route) {
                    popUpTo(LoginNavItem.route) { inclusive = true }
                }
            } else if (it == LoginNavItem) {
                Log.i("navigation", "trying to pop")
                navigationController.navigate(LoginNavItem.route) {
                    popUpTo(0)
                }
            } else {
                navigationController.navigate(it.route) {
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }.launchIn(this)
    }

    NavHost(navController = navigationController, startDestination = LoginNavItem.route) {
        composable(LoginNavItem.route) {
            val viewModel = hiltViewModel<LoginViewModel>()
            LoginScene(viewModel = viewModel)
        }
        composable(RTCNavItem.route) {
            val viewModel = hiltViewModel<RTCViewModel>()
            RTCScene(viewModel)
        }
        composable(RTMNavItem.route) {
            val viewModel = hiltViewModel<RTMViewModel>()
            RTMScene(viewModel)
        }
        composable(SettingsNavItem.route) {
            SettingsScene()
        }
    }
}
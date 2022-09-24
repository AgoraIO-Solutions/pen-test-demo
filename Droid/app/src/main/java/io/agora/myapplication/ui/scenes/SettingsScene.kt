package io.agora.myapplication.ui.scenes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import io.agora.myapplication.services.RTEManager
import io.agora.myapplication.ui.app.RTCNavItem
import io.agora.myapplication.ui.app.SettingsNavItem
import io.agora.myapplication.ui.common.AppBottomBar
import io.agora.myapplication.ui.theme.MyApplicationTheme
import io.agora.myapplication.viewmodels.SettingsViewModel


@Composable
fun SettingsScene() {
    val rteManager: SettingsViewModel = hiltViewModel()
    Scaffold(
        topBar = {
            TopAppBar() {
                Text(text = "Settings")
            }
        },
        bottomBar = {
            AppBottomBar(barNavigationItem = SettingsNavItem)
        }
    ) {
        Column(verticalArrangement = Arrangement.Center) {
            Row(horizontalArrangement = Arrangement.Center) {
                Button(onClick = {
                    rteManager.logout()
                }) {
                    Text("Logout")
                }
            }
        }
    }
}


@Preview
@Composable
private fun SettingsScenePreview() {
    MyApplicationTheme {
        SettingsScene()
    }
}
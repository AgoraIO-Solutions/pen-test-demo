package io.agora.myapplication.ui.scenes

import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.agora.myapplication.ui.app.RTCNavItem
import io.agora.myapplication.ui.app.RTMNavItem
import io.agora.myapplication.ui.common.AppBottomBar
import io.agora.myapplication.ui.theme.MyApplicationTheme


@Composable
fun RTMScene() {
    Scaffold(
        topBar = {
            TopAppBar() {
                Text(text = "RTM")
            }
        },
        bottomBar = {
            AppBottomBar(barNavigationItem = RTMNavItem)
        }
    ) {
        Text(text = "Do RTM Things")
    }
}


@Preview
@Composable
private fun RTMScenePreview() {
    MyApplicationTheme {
        RTMScene()
    }
}
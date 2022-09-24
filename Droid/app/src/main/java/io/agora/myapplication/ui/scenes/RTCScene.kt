package io.agora.myapplication.ui.scenes

import androidx.compose.material.BottomAppBar
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.agora.myapplication.ui.app.RTCNavItem
import io.agora.myapplication.ui.common.AppBottomBar
import io.agora.myapplication.ui.theme.MyApplicationTheme

@Composable
fun RTCScene() {
    Scaffold(
        topBar = {
            TopAppBar() {
                Text(text = "RTC")
            }
        },
        bottomBar = {
            AppBottomBar(barNavigationItem = RTCNavItem)
        }
    ) {
        Text(text = "Do RTC Things")
    }
}


@Preview
@Composable
private fun RTCScenePreview() {
    MyApplicationTheme {
        RTCScene()
    }
}
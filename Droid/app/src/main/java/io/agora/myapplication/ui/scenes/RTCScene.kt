package io.agora.myapplication.ui.scenes

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.agora.myapplication.services.*
import io.agora.myapplication.ui.app.RTCNavItem
import io.agora.myapplication.ui.common.AppBottomBar
import io.agora.myapplication.ui.theme.MyApplicationTheme
import io.agora.myapplication.viewmodels.RTCVM

@Composable
fun RTCScene(rtcvm: RTCVM) {
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
        RTCSceneContent(rtcvm)
    }
}

@Composable
private fun RTCSceneContent(rtcvm: RTCVM) {
    BoxWithConstraints {
        val controlHeight = (this.maxHeight.value * 0.2).dp
        val smallVideos = (this.maxHeight.value * 0.25).dp
        val bigVideo = (this.maxHeight.value * 0.5).dp


        Column {
            Row(Modifier.height(bigVideo)) {
               Text("Big Videos")
            }
            Row(Modifier.height(smallVideos)) {
                Text("Small Videos")
            }
            Row(Modifier.height(controlHeight)) {
                RTCControls(rtcvm = rtcvm)
            }
        }

    }    
}


@Composable
private fun RTCControls(rtcvm: RTCVM) {
    Column {
        Row {
            Column(Modifier.padding(3.dp)) {
                Button(onClick = { rtcvm.publishAudio = !rtcvm.publishAudio } ) {
                    if (rtcvm.publishAudio) {
                        Text("Toggle Publish Audio Off")
                    } else {
                        Text("Toggle Publish Audio On")
                    }
                }
            }
            Column(Modifier.padding(3.dp)) {
                Button(onClick = { rtcvm.publishVideo = !rtcvm.publishVideo } ) {
                    if (rtcvm.publishVideo) {
                        Text("Toggle Publish Video Off")
                    } else {
                        Text("Toggle Publish Video On")
                    }
                }
            }
        }
        Row(Modifier.padding(5.dp)) {
            RTCVideoQualityDialogAndButton(rtcvm = rtcvm)

        }
    }
}

@Composable
private fun RTCVideoQualityDialogAndButton(rtcvm: RTCVM) {
    var dialogShown by remember { mutableStateOf(false) }
    if (dialogShown) {
        Dialog(
            onDismissRequest = {
                dialogShown = false
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card {
                Column(Modifier.padding(20.dp)) {
                    Row {
                        Text("Pick your video Quality")
                    }
                    listOf<VideoQuality>(
                        HighVideoQuality,
                        MediumVideoQuality,
                        LowVideoQuality
                    ).forEach { videoQuality ->
                        Row {
                            OutlinedButton(onClick = {
                                rtcvm.videoQuality = videoQuality
                                dialogShown = false
                            }) {
                                Text(videoQuality.identifier)
                            }
                        }
                    }
                }
            }
        }
    }

    Button(onClick = { dialogShown = true }) {
        Text(text = "Video Quality ${rtcvm.videoQuality.identifier}")
    }
}

private object FakeRTCVM: RTCVM {
    override val rtcUsers: List<RTCUser>
        get() = listOf()

    override var videoQuality: VideoQuality = HighVideoQuality
    override var publishAudio: Boolean = false
    override var publishVideo: Boolean = false
}

@Preview
@Composable
private fun RTCScenePreview() {
    MyApplicationTheme {
        RTCScene(FakeRTCVM)
    }
}
package io.agora.myapplication.ui.common

import android.view.SurfaceView
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import io.agora.myapplication.services.RTCUser
import io.agora.myapplication.viewmodels.RTCVM
import io.agora.myapplication.viewmodels.RTCViewModel


@Composable
fun VideoView(rtcUser: RTCUser, big: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Box(
            Modifier
                .fillMaxSize(1.0f)
                .zIndex(1f)
        ) {
            AgoraView(rtcUser = rtcUser,  big = big)
        }

        Box(
            Modifier
                .align(Alignment.TopEnd)
                .zIndex(2f)
        ) {
            Text(text = "FPS: ${rtcUser.fps}")
        }
    }
}

@Composable
private fun AgoraView(rtcUser: RTCUser, big: Boolean) {
    val rtcvm = hiltViewModel<RTCViewModel>()
    AndroidView(
        modifier = Modifier.fillMaxSize(), // Occupy the max size in the Compose UI tree
        factory = { context ->
            FrameLayout(context).apply {
                addView(
                    SurfaceView(context).apply {
                        rtcvm.setupVideo(surfaceView = this, uid = rtcUser.uid, big = big)
                    }
                )
            }
        },
        update = { view ->
            if (view is FrameLayout) {
                view.removeAllViews()
                view.addView(
                    SurfaceView(view.context).apply {
                        rtcvm.setupVideo(surfaceView = this, uid = rtcUser.uid, big = big)
                    }
                )
            }
        }
    )
}
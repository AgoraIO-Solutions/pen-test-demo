package io.agora.myapplication.viewmodels

import android.view.SurfaceView
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.agora.myapplication.services.RTCManager
import io.agora.myapplication.services.RTCUser
import io.agora.myapplication.services.VideoQuality
import javax.inject.Inject

interface RTCVM {
    val rtcUsers: List<RTCUser>
    var videoQuality: VideoQuality
    var publishAudio: Boolean
    var publishVideo: Boolean
    var focusedUid: Int
    fun setupVideo(surfaceView: SurfaceView, uid: Int, big: Boolean)
}

@HiltViewModel
class RTCViewModel @Inject constructor(
    private val rtcManager: RTCManager
): ViewModel(), RTCVM{
    override var videoQuality: VideoQuality
        get() =  rtcManager.videoQuality
        set(value) {
            rtcManager.videoQuality = value
        }

    override var focusedUid: Int
        get() = rtcManager.focusedUid
        set(value) {
            rtcManager.focusedUid = value
        }

    override val rtcUsers: List<RTCUser>
        get() = rtcManager.rtcUsers

    override var publishAudio: Boolean
        get() = rtcManager.publishAudio
        set(value) {  rtcManager.publishAudio = value }

    override var publishVideo: Boolean
        get() = rtcManager.publishVideo
        set(value) {  rtcManager.publishVideo = value }

    override fun setupVideo(surfaceView: SurfaceView, uid: Int, big: Boolean) {
        rtcManager.setupVideo(surfaceView, uid, big)
    }
}
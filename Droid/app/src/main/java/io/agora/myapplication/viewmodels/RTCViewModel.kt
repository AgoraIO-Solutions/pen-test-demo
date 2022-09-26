package io.agora.myapplication.viewmodels

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

    override val rtcUsers: List<RTCUser>
        get() = rtcManager.rtcUsers

    override var publishAudio: Boolean
        get() = rtcManager.publishAudio
        set(value) {  rtcManager.publishAudio = value }

    override var publishVideo: Boolean
        get() = rtcManager.publishVideo
        set(value) {  rtcManager.publishVideo = value }
}
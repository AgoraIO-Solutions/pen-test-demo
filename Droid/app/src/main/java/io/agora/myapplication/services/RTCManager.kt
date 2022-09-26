package io.agora.myapplication.services

import android.content.Context
import androidx.compose.runtime.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.agora.myapplication.R
import io.agora.myapplication.utils.EZExceptionHandler
import io.agora.myapplication.utils.EZLogger
import io.agora.rtc.*
import io.agora.rtc.Constants.CHANNEL_PROFILE_COMMUNICATION
import io.agora.rtc.internal.EncryptionConfig
import io.agora.rtc.internal.EncryptionConfig.EncryptionMode.AES_256_GCM
import io.agora.rtc.internal.LastmileProbeConfig
import io.agora.rtc.video.VideoEncoderConfiguration
import io.agora.rtc.video.VideoEncoderConfiguration.FRAME_RATE.*
import io.agora.rtc.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
import io.agora.rtc.video.VideoEncoderConfiguration.STANDARD_BITRATE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RTCModule {
    @Provides
    @Singleton
    fun provideRTCManager(@ApplicationContext context: Context) = RTCManager(context)
}

sealed class VideoQuality(
    val identifier: String,
    val agoraVideoEncoderConfiguration: VideoEncoderConfiguration
    )
object LowVideoQuality: VideoQuality(
    identifier = "Low",
    agoraVideoEncoderConfiguration = VideoEncoderConfiguration(
         VideoEncoderConfiguration.VideoDimensions(160, 120),
        FRAME_RATE_FPS_15,
        STANDARD_BITRATE,
        ORIENTATION_MODE_ADAPTIVE
    )
)

object MediumVideoQuality: VideoQuality(
    identifier = "Medium",
    agoraVideoEncoderConfiguration = VideoEncoderConfiguration(
        VideoEncoderConfiguration.VideoDimensions(480, 360),
        FRAME_RATE_FPS_24,
        STANDARD_BITRATE,
        ORIENTATION_MODE_ADAPTIVE
    )
)

object HighVideoQuality: VideoQuality(
    identifier = "High",
    agoraVideoEncoderConfiguration = VideoEncoderConfiguration(
        VideoEncoderConfiguration.VideoDimensions(960, 720),
        FRAME_RATE_FPS_30,
        STANDARD_BITRATE,
        ORIENTATION_MODE_ADAPTIVE
    )
)


data class RTCUser(val uid: Int)  {
    var fps by mutableStateOf(15)
}

sealed class QOS(val quality: String)
object Excellent: QOS(quality = "Excellent")
object Good: QOS(quality = "Good")
object Poor: QOS(quality = "Poor")
object Bad: QOS(quality = "Bad")
object VeryBad: QOS(quality = "Very Bad")
object Unknown: QOS(quality = "Unknown")

fun Int.toQOS(): QOS = when(this) {
    1 -> Excellent
    2 -> Good
    3 -> Poor
    4 -> Bad
    5 -> VeryBad
    else -> Unknown
}


class RTCManager(
    context: Context
): EZExceptionHandler {
    private val mainScope = CoroutineScope(Dispatchers.Main)

    private var myUID: Int = 0
    private val channelHandler = ChannelEventHandler()
    private val engineEventHandler = EngineEventHandler()
    private val _connectionState = MutableSharedFlow<ConnectionState>()

    var publishAudio by mutableStateOf(true)
    var publishVideo by mutableStateOf(true)
    var networkQuality by mutableStateOf<QOS>(Unknown)
    var videoQuality by mutableStateOf<VideoQuality>(MediumVideoQuality)
    val rtcUsers = mutableStateListOf<RTCUser>()
    var focusedUid by mutableStateOf(0)
    val connectionState: SharedFlow<ConnectionState> get() = _connectionState

    private lateinit var agoraAppEngine: RtcEngine
    init {
        mainScope.launch {
            try {
                val rtcEngineConfig = RtcEngineConfig()
                rtcEngineConfig.mContext = context
                rtcEngineConfig.mAppId = context.getString(R.string.agora_app_id)
                rtcEngineConfig.mEventHandler = engineEventHandler
                agoraAppEngine = RtcEngine.create(rtcEngineConfig)
            } catch (throwable: Throwable) {
                throw RuntimeException("RTC Engine: $throwable");
            }

            agoraAppEngine.setChannelProfile(CHANNEL_PROFILE_COMMUNICATION)
            agoraAppEngine.enableDualStreamMode(true)
            agoraAppEngine.enableAudio()
            agoraAppEngine.enableVideo()
            agoraAppEngine.startPreview()

            startLastMileProbe()

            snapshotFlow { videoQuality }
                .onEach {
                    adjustVideoQuality()
                }
                .launchIn(mainScope)

            snapshotFlow { publishVideo }
                .onEach {
                    info("Video publishing updated $it")
                    agoraAppEngine.muteLocalVideoStream(!publishVideo)
                }
                .launchIn(mainScope)

            snapshotFlow { publishAudio }
                .onEach {
                    info("Audio publishing updated $it")
                    agoraAppEngine.muteLocalVideoStream(!publishAudio)
                }
                .launchIn(mainScope)

        }
    }

    private fun startLastMileProbe() {
        val config = LastmileProbeConfig().apply {
            expectedDownlinkBitrate = 100_000
            expectedUplinkBitrate = 100_000
            probeDownlink = true
            probeUplink = true
        }
        agoraAppEngine.startLastmileProbeTest(config)
    }

    private fun adjustVideoQuality(){
        info("adjusted video quality too $videoQuality")
        agoraAppEngine.setVideoEncoderConfiguration(videoQuality.agoraVideoEncoderConfiguration)
    }

    private fun setEncryption(aesKey: AesKey) {
        val encryptionConfig = EncryptionConfig().apply {
            encryptionKey = aesKey.key
            encryptionMode = AES_256_GCM

        }
        agoraAppEngine.enableEncryption(true, encryptionConfig)
    }


    fun join(channelName: String, tokens: Tokens, aesKey: AesKey) = mainScope.launch {
        _connectionState.emit(Connecting)
        setEncryption(aesKey)

        myUID = tokens.uid.toInt()

        val joinStatus = agoraAppEngine.joinChannel(tokens.rtc, channelName, null, tokens.uid.toInt())

        if (joinStatus == 0) {
            _connectionState.emit(Connected)
            info("Successfully joined channel $channelName")
        } else {
            _connectionState.emit(Disconnected)
            error("Failed to join channel status $joinStatus")
        }
    }

    fun leave() = mainScope.launch {
        _connectionState.emit(Disconnected)
        agoraAppEngine.leaveChannel()
        focusedUid = 0
        rtcUsers.clear()
    }



    private inner class EngineEventHandler: IRtcEngineEventHandler() {
        override fun onLastmileQuality(quality: Int) {
            networkQuality = quality.toQOS()
        }
        override fun onLeaveChannel(stats: RtcStats?) {
            startLastMileProbe()
        }

        override fun onWarning(warn: Int) {
            error("Warn in RTC engine $warn")
        }

        override fun onError(err: Int) {
            error("Error in RTC Engine $err")
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            info("Successfully joined $channel")
            rtcUsers.add(RTCUser(uid))
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            rtcUsers.add(RTCUser(uid))
            if (focusedUid == 0) {
                focusedUid = uid
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            rtcUsers.removeIf { it.uid == uid }
        }

        override fun onRemoteVideoStats(stats: RemoteVideoStats?) {
            val stats = stats ?: return
            try {
                rtcUsers.first { it.uid == stats.uid }.fps = stats.decoderOutputFrameRate
            } catch (throwable: Throwable) {
                error("No user found for stats $stats, throwable $throwable")
            }
        }

        override fun onLocalVideoStats(stats: LocalVideoStats?) {
            val stats = stats ?: return
            try {
                rtcUsers.first { it.uid == myUID }.fps = stats.captureFrameRate
            } catch (throwable: Throwable) {
                error("No user found for stats $stats, throwable $throwable")
            }
        }
    }

    private inner class ChannelEventHandler: IRtcChannelEventHandler() {

    }
}

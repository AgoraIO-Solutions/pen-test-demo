package io.agora.myapplication.services

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.agora.myapplication.R
import io.agora.myapplication.utils.EZExceptionHandler
import io.agora.rtm.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RTMModule {
    @Provides
    @Singleton
    fun provides(@ApplicationContext context: Context): RTMManager = RTMManager(context)
}

class RTMManager @Inject constructor(
    context: Context
): RtmClientListener, RtmChannelListener, EZExceptionHandler {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val agoraRtmClient = RtmClient.createInstance(context, context.getString(R.string.agora_app_id), this)
    private val _connectionState = MutableSharedFlow<ConnectionState>()
    private var channel: RtmChannel? = null
    private val _messages = mutableStateListOf<String>()

    val connectionState: SharedFlow<ConnectionState> get() = _connectionState
    val messages: List<String> get() = _messages

    fun join(channelName: String, tokens: Tokens) {
        scope.launch(exceptionHandler) {
            _connectionState.emit(Connecting)
        }
        agoraRtmClient.login(tokens.rtm, tokens.rtmuid, object : ResultCallback<Void> {
            override fun onSuccess(p0: Void?) {
                channel = agoraRtmClient.createChannel(channelName, this@RTMManager)
                channel?.join(object : ResultCallback<Void>{
                    override fun onSuccess(p0: Void?) {
                        scope.launch(exceptionHandler) {
                            info("Succeeded in logging in to rtm")
                            _connectionState.emit(Connected)
                        }
                    }
                    override fun onFailure(p0: ErrorInfo?) = error("Error joining channel $p0")
                })
            }
            override fun onFailure(p0: ErrorInfo?) {
                error("Failed to login to RTM $p0")
                scope.launch(exceptionHandler) {
                    _connectionState.emit(Disconnected)
                }
            }
        })
    }

    fun logout() {
        channel?.leave( object : ResultCallback<Void> {
            override fun onSuccess(p0: Void?) {
                info("Successfully left channels")
                scope.launch(exceptionHandler) {
                    withContext(Dispatchers.Main) {
                        agoraRtmClient.logout(object: ResultCallback<Void> {
                            override fun onSuccess(p0: Void?) {
                                info("logged out of client successfully")
                                scope.launch(exceptionHandler) {
                                _connectionState.emit(Disconnected) }
                            }
                            override fun onFailure(p0: ErrorInfo?) = error("Error logging out of rtm $p0")
                        })
                    }
                }
            }
            override fun onFailure(p0: ErrorInfo?) = error("Failed to leave channel $p0")
        })

    }

    fun send(message: String) {
        val rtmMsg = agoraRtmClient.createMessage(message)
        channel?.sendMessage(rtmMsg, object : ResultCallback<Void> {
            override fun onSuccess(p0: Void?) {
                info("succesfully sent $message")
                _messages.add(message)
            }
            override fun onFailure(err: ErrorInfo?) = error("Failed to send $message, err: $err")
        })
    }

    // RTMChannelListener one useful method
    override fun onMessageReceived(msg: RtmMessage?, p1: RtmChannelMember?) {
        info("Channel message received")
        val newMsg = msg?.text ?: return
        _messages.add(newMsg)
    }

    // Note: everything below is conformance
    // RTMClientListener
    override fun onConnectionStateChanged(p0: Int, p1: Int) {
        // Connection changed
    }

    override fun onMessageReceived(p0: RtmMessage?, p1: String?) {
        //
    }

    override fun onTokenExpired() {
        //
    }

    override fun onTokenPrivilegeWillExpire() {
        //
    }

    override fun onPeersOnlineStatusChanged(p0: MutableMap<String, Int>?) {
        //
    }

     // RTM Channele Listner
    override fun onMemberCountUpdated(p0: Int) {

    }

    override fun onAttributesUpdated(p0: MutableList<RtmChannelAttribute>?) {

    }

    override fun onMemberJoined(p0: RtmChannelMember?) {
        //
    }

    override fun onMemberLeft(p0: RtmChannelMember?) {
        //
    }

}
package io.agora.myapplication.services

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.agora.myapplication.ui.app.LoginNavItem
import io.agora.myapplication.ui.app.RTCNavItem
import io.agora.myapplication.utils.EZExceptionHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

sealed class LoginState
object LoggedIn: LoginState()
object LoggingIn: LoginState()
object LoggedOut: LoginState()

sealed class ConnectionState
object Connected: ConnectionState()
object Connecting: ConnectionState()
object Disconnected: ConnectionState()

@Module
@InstallIn(SingletonComponent::class)
object RTEManagerModule {
    @Provides
    @Singleton
    fun provideRTEManager(
        navigator: Navigator,
        networkService: NetworkService,
        rtmManager: RTMManager,
        rtcManager: RTCManager
    ): RTEManager = RTEManager(navigator, networkService, rtmManager, rtcManager)
}

class RTEManager @Inject constructor(
    private val navigator: Navigator,
    private val networkService: NetworkService,
    private val rtmManager: RTMManager,
    private val rtcManager: RTCManager
): EZExceptionHandler {
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch(exceptionHandler) {
            rtmManager.connectionState.combine(rtcManager.connectionState) { rtmConnection, rtcConnection ->
                info("Connection state of rtm changed to $rtmConnection, rtcConnection $rtcConnection")
                val state = when (Pair(rtmConnection, rtcConnection)) {
                    Pair(Connecting, Connecting) -> LoggingIn
                    Pair(Connected, Connecting) -> LoggingIn
                    Pair(Connecting, Connected) -> LoggingIn
                    Pair(Disconnected, Connecting) -> LoggingIn
                    Pair(Connecting, Disconnected) -> LoggingIn
                    Pair(Connected, Connected) -> {
                        navigator.navigateTo(RTCNavItem)
                        LoggedIn
                    }
                    else -> {
                        navigator.navigateTo(LoginNavItem)
                        LoggedOut
                    }
                }
                withContext(Dispatchers.Main) {
                    _loginState.emit(state)
                }
            }.collect()
        }
    }

    private val _loginState: MutableStateFlow<LoginState> = MutableStateFlow(LoggedOut)
    val loginState: StateFlow<LoginState> get() = _loginState

    fun login(channelName: String) {
        scope.launch(exceptionHandler) {
            _loginState.emit(LoggingIn)
            try {
                val aesKey = getAesKey(channelName)
                debug("Got the aes key $aesKey")

                val agoraToken = getAgoraToken(channelName)
                debug("Got the agora token $agoraToken")

                async {
                    rtcManager.join(channelName, agoraToken, aesKey)
                }
                rtmManager.join(channelName, agoraToken)

            } catch (th: Throwable) {
                error("Throwable $th")
                withContext(Dispatchers.Main) {
                    _loginState.emit(LoggedOut)
                }
                return@launch
            }
        }
    }

    private suspend fun getAesKey(channelName: String): AesKey {
        val response = networkService.getAesToken(channelName)
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw Throwable("Error getting aes key")
        }
    }

    private suspend fun getAgoraToken(channelName: String): Tokens {
        val response = networkService.getToken(channelName)
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw Throwable("Error getting aes key")
        }
    }

    fun logout() {
        rtmManager.logout()
        rtcManager.leave()
    }
}
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
        rtmManager: RTMManager
    ): RTEManager = RTEManager(navigator, networkService, rtmManager)
}

class RTEManager @Inject constructor(
    private val navigator: Navigator,
    private val networkService: NetworkService,
    private val rtmManager: RTMManager
): EZExceptionHandler {
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch(exceptionHandler) {
            rtmManager.connectionState.collect {
                info("Connection state of rtm changed to $it")
                val state = when (it) {
                    Connecting -> LoggingIn
                    Connected -> {
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
            }
        }
    }

    private val _loginState: MutableStateFlow<LoginState> = MutableStateFlow(LoggedOut)
    val loginState: StateFlow<LoginState> get() = _loginState

    fun login(channelName: String) {
        scope.launch(exceptionHandler) {
            _loginState.emit(LoggingIn)
            try {
                val aesKey = getAesKey(channelName)
                info("Got the aes key $aesKey")

                val agoraToken = getAgoraToken(channelName)
                info("Got the agora token $agoraToken")

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
    }
}
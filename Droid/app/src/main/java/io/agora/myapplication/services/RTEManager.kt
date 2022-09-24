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

@Module
@InstallIn(SingletonComponent::class)
object RTEManagerModule {
    @Provides
    @Singleton
    fun provideRTEManager(navigator: Navigator, networkService: NetworkService): RTEManager = RTEManager(navigator, networkService)
}

class RTEManager @Inject constructor(
    private val navigator: Navigator,
    private val networkService: NetworkService
) : EZExceptionHandler {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _loginState: MutableStateFlow<LoginState> = MutableStateFlow(LoggedOut)
    val loginState: StateFlow<LoginState> get() = _loginState

    fun login(channelName: String) {
        scope.launch(exceptionHandler) {
            withContext(Dispatchers.Main) {
                _loginState.emit(LoggingIn)
            }

            try {
                val aesKey = getAesKey(channelName)
                info("Got the aes key $aesKey")

                val agoraToken = getAgoraToken(channelName)
                info("Got the agora token $agoraToken")
            } catch (th: Throwable) {
                error("Throwable $th")
                withContext(Dispatchers.Main) {
                    _loginState.emit(LoggedOut)
                }
                return@launch
            }
            delay(100)
            navigator.navigateTo(RTCNavItem)

            withContext(Dispatchers.Main) {
                _loginState.emit(LoggedIn)
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
        scope.launch(exceptionHandler) {
            withContext(Dispatchers.Main) {
                _loginState.emit(LoggedOut)
                navigator.navigateTo(LoginNavItem)
            }
        }
    }
}
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
    fun provideRTEManager(navigator: Navigator): RTEManager = RTEManager(navigator)
}

class RTEManager @Inject constructor(
    private val navigator: Navigator
) : EZExceptionHandler {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _loginState: MutableStateFlow<LoginState> = MutableStateFlow(LoggedOut)
    val loginState: StateFlow<LoginState> get() = _loginState

    fun login(channelName: String) {
        scope.launch(exceptionHandler) {
            withContext(Dispatchers.Main) {
                _loginState.emit(LoggingIn)
            }

            delay(100)
            navigator.navigateTo(RTCNavItem)

            withContext(Dispatchers.Main) {
                _loginState.emit(LoggedIn)
            }
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
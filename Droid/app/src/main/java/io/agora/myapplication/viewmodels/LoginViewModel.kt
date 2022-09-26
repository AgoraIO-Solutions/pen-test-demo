package io.agora.myapplication.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.agora.myapplication.services.*
import io.agora.myapplication.ui.scenes.LoginSceneViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject




@HiltViewModel
class LoginViewModel @Inject constructor(
    private val rteManager: RTEManager,
    private val rtcManager: RTCManager
) : ViewModel(), LoginSceneViewModel {
    override val loginState: StateFlow<LoginState> get() = rteManager.loginState
    override val qosState: QOS get() = rtcManager.networkQuality
    override var channel by mutableStateOf("TEST")

    override fun login() {
        rteManager.login(channel.uppercase())
    }

    fun logout() {
        rteManager.logout()
    }
}
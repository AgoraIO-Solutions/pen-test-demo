package io.agora.myapplication.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.agora.myapplication.services.LoginState
import io.agora.myapplication.services.Navigator
import io.agora.myapplication.services.RTEManager
import io.agora.myapplication.ui.scenes.LoginSceneViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

sealed class QOS(val quality: String)
object Excellent: QOS(quality = "Excellent")
object Good: QOS(quality = "Good")
object Ok: QOS(quality = "Ok")
object Bad: QOS(quality = "Bad")
object VeryBad: QOS(quality = "Very Bad")
object Unknown: QOS(quality = "Unknown")


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val rteManager: RTEManager
) : ViewModel(), LoginSceneViewModel {
    override val loginState: StateFlow<LoginState> get() = rteManager.loginState
    override val qosState: StateFlow<QOS> = MutableStateFlow(Excellent)
    override var channel by mutableStateOf("test channel")

    override fun login() {
        rteManager.login(channel)
    }

    fun logout() {
        rteManager.logout()
    }
}
package io.agora.myapplication.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.agora.myapplication.services.RTMManager
import javax.inject.Inject

interface RTMVM {
    val messages: List<String>
    fun send()
    var message: String
}

@HiltViewModel
class RTMViewModel @Inject constructor(
    private val rtmManager: RTMManager
): ViewModel(), RTMVM {
    override var message by mutableStateOf("")

    override val messages: List<String>
        get() = rtmManager.messages

    override fun send() {
        rtmManager.send(message)
        message = ""
    }
}
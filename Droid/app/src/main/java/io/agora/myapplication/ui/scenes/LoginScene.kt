package io.agora.myapplication.ui.scenes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.agora.myapplication.services.LoggedOut
import io.agora.myapplication.services.LoginState
import io.agora.myapplication.ui.theme.MyApplicationTheme
import io.agora.myapplication.viewmodels.Excellent
import io.agora.myapplication.viewmodels.LoginViewModel
import io.agora.myapplication.viewmodels.QOS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.log

interface LoginSceneViewModel {
    val loginState: StateFlow<LoginState>
    val qosState: StateFlow<QOS>
    var channel: String
    fun login()
}

@Composable
fun LoginScene(viewModel: LoginSceneViewModel) {
    val loginState by viewModel.loginState.collectAsState()
    val qos by viewModel.qosState.collectAsState()
    _LoginScene(viewModel = viewModel, loginState = loginState, qos = qos)
}

@Composable
 private fun _LoginScene(viewModel: LoginSceneViewModel, loginState: LoginState, qos: QOS) {
    Scaffold(
        topBar = { TopAppBar() {
            Text(text = "Log In")
        } }
    ) {
        Column {
            val rowModifier = Modifier.padding(20.dp)
            Row(modifier = rowModifier) {
                OutlinedTextField(
                    value = viewModel.channel,
                    placeholder = { Text("enter a channel name") },
                    onValueChange = { viewModel.channel = it }
                )
            }
            Row(modifier = rowModifier) {
                Button(
                    onClick = { viewModel.login() },
                    enabled = loginState == LoggedOut
                ) {
                    Text("Login")
                }
            }
            Row(modifier = rowModifier) {
               Text(
                   text ="Network Condition: ${qos.quality}"
               )
            }
        }
    }
    
}

private class FakeLoginViewModel: LoginSceneViewModel {
    override var channel = "channel"
    override val qosState: StateFlow<QOS>
        get() = MutableStateFlow(Excellent)
    override val loginState: StateFlow<LoginState>
        get() = MutableStateFlow(LoggedOut)
    override fun login() {}

}

@Preview
@Composable
fun LoginScenePreview() {
    MyApplicationTheme {
        LoginScene(viewModel = FakeLoginViewModel())
    }
}

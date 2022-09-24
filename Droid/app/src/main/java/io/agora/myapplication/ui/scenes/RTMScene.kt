package io.agora.myapplication.ui.scenes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.agora.myapplication.ui.app.RTCNavItem
import io.agora.myapplication.ui.app.RTMNavItem
import io.agora.myapplication.ui.common.AppBottomBar
import io.agora.myapplication.ui.theme.MyApplicationTheme
import io.agora.myapplication.viewmodels.RTMVM


@Composable
fun RTMScene(rtmvm: RTMVM) {
    Scaffold(
        topBar = {
            TopAppBar() {
                Text(text = "RTM")
            }
        },
        bottomBar = {
            AppBottomBar(barNavigationItem = RTMNavItem)
        }
    ) {
       RTMContent(rtmvm)
    }
}

@Composable
private fun RTMContent(rtmvm: RTMVM) {
    Column {
        Row {
           Column {
               OutlinedTextField(value = rtmvm.message, onValueChange = {
                   rtmvm.message = it
               })
           }
            Column {
                Button(onClick = { rtmvm.send() }) {
                    Text("Send")
                }
            }
        }
        rtmvm.messages.forEach { 
            Row {
               Text(text = it) 
            }
        }
    }
}


private object FakeRTMVM: RTMVM {
    override val messages: List<String>
        get() = listOf("dogs", "and", "cats")

    override var message: String = ""
    override fun send() {

    }
}

@Preview
@Composable
private fun RTMScenePreview() {
    MyApplicationTheme {
        RTMScene(FakeRTMVM)
    }
}
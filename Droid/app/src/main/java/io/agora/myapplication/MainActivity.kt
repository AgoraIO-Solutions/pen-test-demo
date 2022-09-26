package io.agora.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import io.agora.myapplication.services.Navigator
import io.agora.myapplication.ui.app.DemoApp
import io.agora.myapplication.ui.theme.MyApplicationTheme
import io.agora.myapplication.utils.EZLogger
import javax.inject.Inject


private const val PERMISSION_REQ_ID_RECORD_AUDIO = 22
private const val PERMISSION_REQ_ID_RECORD_VIDEO = 23
@AndroidEntryPoint
class MainActivity : ComponentActivity(), EZLogger {
    @Inject lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        info("booted!")
        checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)
        checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_RECORD_VIDEO)

        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                DemoApp(navigator)
            }
        }
    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        info( "checkSelfPermission $permission $requestCode")
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(permission),
                requestCode
            )
            return false
        }
        return true
    }
}

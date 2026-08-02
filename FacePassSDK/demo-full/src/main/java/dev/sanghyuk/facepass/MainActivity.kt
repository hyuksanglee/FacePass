package dev.sanghyuk.facepass

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sanghyuk.face_sdk.FaceAuthActivity
import dev.sanghyuk.facepass.ui.theme.FacePassSDKTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FacePassSDKTheme {
                MainScreen(
                    onStartAuth = {
                        startActivity(Intent(this, FaceAuthActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreen(onStartAuth: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = onStartAuth) {
            Text("얼굴 인증 시작")
        }
    }
}
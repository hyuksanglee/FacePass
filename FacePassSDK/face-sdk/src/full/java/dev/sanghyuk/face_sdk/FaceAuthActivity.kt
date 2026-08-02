package dev.sanghyuk.face_sdk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.sanghyuk.face_sdk.api.FaceAuthConfig
import dev.sanghyuk.face_sdk.ui.FaceAuthContent
import dev.sanghyuk.face_sdk.ui.theme.FacePassSDKTheme

class FaceAuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceAuthContent(
                config = FaceAuthConfig(),
                onSuccess = { face -> /* 결과 반환하고 finish */ },
                onFailure = { error -> /* 실패 반환 */ }
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FacePassSDKTheme {
        Greeting("Android")
    }
}
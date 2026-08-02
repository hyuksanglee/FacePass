package dev.sanghyuk.face_sdk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.sanghyuk.face_sdk.api.FaceAuthConfig
import dev.sanghyuk.face_sdk.internal.FaceResultHolder
import dev.sanghyuk.face_sdk.ui.FaceAuthContent
import dev.sanghyuk.face_sdk.ui.theme.FacePassSDKTheme

class FaceAuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FacePassSDKTheme {
                FaceAuthContent(
                    config = FaceAuthConfig(),
                    onSuccess = { face ->
                        FaceResultHolder.set(face)
                        setResult(Activity.RESULT_OK)
                        finish()
                    },
                    onFailure = { error ->
                        val data = Intent().apply {
                            putExtra(FaceAuthActivity.EXTRA_ERROR_CODE, error.code)
                            putExtra(FaceAuthActivity.EXTRA_ERROR_RETRYABLE, error.retryable)
                        }
                        setResult(Activity.RESULT_CANCELED, data)
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_ERROR_CODE = "face_auth_error_code"
        const val EXTRA_ERROR_RETRYABLE = "face_auth_error_retryable"
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
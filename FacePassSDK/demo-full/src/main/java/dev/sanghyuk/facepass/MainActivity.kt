package dev.sanghyuk.facepass

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import dev.sanghyuk.face_sdk.FaceAuthActivity
import dev.sanghyuk.face_sdk.internal.FaceResultHolder
import dev.sanghyuk.facepass.ui.theme.FacePassSDKTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FacePassSDKTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var resultImage by remember { mutableStateOf<Bitmap?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            resultImage = FaceResultHolder.consume()
            errorText = null
        } else {
            val code = result.data?.getStringExtra(FaceAuthActivity.EXTRA_ERROR_CODE)
            val retryable = result.data?.getBooleanExtra(
                FaceAuthActivity.EXTRA_ERROR_RETRYABLE, false
            ) ?: false
            errorText = "인증 실패 (코드: $code, 재시도 가능: $retryable)"
            resultImage = null
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        resultImage?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "인증된 얼굴",
                modifier = Modifier.size(200.dp)
            )
            Spacer(Modifier.height(20.dp))
        }

        errorText?.let { text ->
            Text(text = text, color = androidx.compose.ui.graphics.Color.Red)
            Spacer(Modifier.height(20.dp))
        }

        Button(onClick = {
            launcher.launch(Intent(context, FaceAuthActivity::class.java))
        }) {
            Text("얼굴 인증 시작")
        }
    }
}
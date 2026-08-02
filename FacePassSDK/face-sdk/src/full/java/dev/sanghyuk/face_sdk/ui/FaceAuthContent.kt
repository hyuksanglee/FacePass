package dev.sanghyuk.face_sdk.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import dev.sanghyuk.face_sdk.api.AuthProgress
import dev.sanghyuk.face_sdk.api.FaceAuthAnalyzer
import dev.sanghyuk.face_sdk.api.FaceAuthCallback
import dev.sanghyuk.face_sdk.api.FaceAuthConfig
import dev.sanghyuk.face_sdk.api.PassError

@Composable
internal fun FaceAuthContent(
    config: FaceAuthConfig,
    onSuccess: (Bitmap) -> Unit,
    onFailure: (PassError) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) onFailure(PassError.CameraDenied)
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var uiState by remember { mutableStateOf(AuthUiState.Searching) }

    val analyzer = remember {
        FaceAuthAnalyzer(
            config,
            callback = object : FaceAuthCallback {
                override fun onSuccess(face: Bitmap) {
                    uiState = AuthUiState.Success
                    onSuccess(face)
                }

                override fun onError(error: PassError) {
                    onFailure(error)
                }

                override fun onProgress(state: AuthProgress) {
                    uiState = state.toUiState()
                }
            }
        )
    }

    FaceAuthScreen(
        state = uiState,
        modifier = modifier,
        cameraContent = {
            if (hasCameraPermission) {
                CameraPreview(analyzer = analyzer)
            }
        }
    )
}

private fun AuthProgress.toUiState(): AuthUiState = when (this) {
    AuthProgress.SEARCHING -> AuthUiState.Searching
    AuthProgress.ALIGNING -> AuthUiState.Aligning
    AuthProgress.AWAITING_ACTION -> AuthUiState.Aligning       // "고개 돌려주세요"
    AuthProgress.ACTION_IN_PROGRESS -> AuthUiState.Action      // "원래대로"
}

private fun PassError.toMessage(): String = when (this) {
    PassError.MaskDetected -> "마스크를 벗고 다시 시도해주세요"
    PassError.MultipleFaces -> "한 명만 화면에 나와주세요"
    PassError.CameraDenied -> "카메라 권한이 필요합니다"
    PassError.ModelNotReady -> "잠시 후 다시 시도해주세요"
    else -> "인증에 실패했습니다"
}
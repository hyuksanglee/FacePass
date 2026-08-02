package dev.sanghyuk.face_sdk.ui

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
){
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
                    uiState = AuthUiState.fail(error.toMessage())
                    if (!error.retryable) onFailure(error)
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
        cameraContent = { CameraPreview(analyzer = analyzer) }
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
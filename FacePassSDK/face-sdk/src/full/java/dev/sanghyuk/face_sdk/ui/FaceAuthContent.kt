package dev.sanghyuk.face_sdk.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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

    // analyzer를 콜백 안에서 참조하기 위한 홀더 (생성 시점 순환참조 회피)
    val analyzerRef = remember { arrayOfNulls<FaceAuthAnalyzer>(1) }

    val analyzer = remember {
        FaceAuthAnalyzer(
            context = context,
            config,
            callback = object : FaceAuthCallback {
                override fun onSuccess(face: Bitmap) {
                    uiState = AuthUiState.Success
                    onSuccess(face)
                }

                override fun onError(error: PassError) {
                    if (error.retryable) {
                        // 재시도 가능한 에러 → 화면 유지, FAIL 상태로 전환 (다시 시도 버튼 표시)
                        uiState = AuthUiState.fail(error.toMessage())
                    } else {
                        // 재시도 불가 (권한 등) → 앱에 최종 실패 반환
                        onFailure(error)
                    }
                }

                override fun onProgress(state: AuthProgress) {
                    uiState = state.toUiState()
                }
            }
        ).also { analyzerRef[0] = it }
    }

    DisposableEffect(Unit) {
        onDispose { analyzer.close() }
    }

    FaceAuthScreen(
        state = uiState,
        modifier = modifier,
        cameraContent = {
            if (hasCameraPermission) {
                CameraPreview(analyzer = analyzer)
            }
        },
        onRetry = {
            // 다시 시도: analyzer 리셋 + UI를 처음 상태로
            analyzerRef[0]?.reset()
            uiState = AuthUiState.Searching
        }
    )
}

private fun AuthProgress.toUiState(): AuthUiState = when (this) {
    AuthProgress.SEARCHING -> AuthUiState.Searching
    AuthProgress.ALIGNING -> AuthUiState.Aligning
    AuthProgress.AWAITING_ACTION -> AuthUiState.Aligning
    AuthProgress.ACTION_IN_PROGRESS -> AuthUiState.Action
}


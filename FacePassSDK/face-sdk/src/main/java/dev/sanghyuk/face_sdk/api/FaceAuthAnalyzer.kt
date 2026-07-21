package dev.sanghyuk.face_sdk.api

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.face.Face
import dev.sanghyuk.face_sdk.detection.FaceDetectorSource


/**
 * LOGIC SDK의 진입점.
 *
 * CameraX [ImageAnalysis.Analyzer]를 직접 구현하므로, 자체 카메라 프리뷰를
 * 가진 앱은 기존 ImageAnalysis 유스케이스에 이 Analyzer를 연결하는 것만으로
 * 얼굴 인증을 붙일 수 있습니다:
 *
 * ```
 * val analyzer = FaceAuthAnalyzer(config, callback)
 * imageAnalysis.setAnalyzer(executor, analyzer)
 * ```
 *
 * 인증 파이프라인: 얼굴 검출 → 마스크 확인 → 액션 라이브니스 → 얼굴 반환
 *
 * - 결과는 [FaceAuthCallback]으로 정확히 한 번 전달됩니다 (성공 또는 실패).
 * - 결과 전달 후에는 프레임이 들어와도 무시합니다. 새 인증을 시작하려면
 *   [reset]을 호출하세요.
 * - 콜백은 메인 스레드에서 호출됩니다.
 */
class FaceAuthAnalyzer(
    private val config: FaceAuthConfig = FaceAuthConfig(),
    private val callback: FaceAuthCallback
) : ImageAnalysis.Analyzer {

    private val detectorSource = FaceDetectorSource()
    private var finished = false

    override fun analyze(image: ImageProxy) {

        if (finished) { image.close(); return}

        detectorSource.detect(
            imageProxy = image,
            onResult = { faces -> handleFaces(faces)},
            onFailure = { error -> fail(PassError.Unknown(error))},
            onComplete = { image.close() }
        )

    }

    private fun handleFaces(faces: List<Face>) {
        when {
            faces.isEmpty() -> callback.onProgress(AuthProgress.SEARCHING)
            faces.size >= 2 -> fail(PassError.MultipleFaces)
            else -> {
                val face = faces.first()
                android.util.Log.d(
                    "FacePass",
                    "angleY=${face.headEulerAngleY}, box=${face.boundingBox}"
                )
                callback.onProgress(AuthProgress.ALIGNING)
            }
        }
    }

    private fun fail(error: PassError) {
        if (finished) return
        finished = true
        callback.onError(error)
    }


    fun reset() {
        finished = false
    }
}
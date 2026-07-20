package dev.sanghyuk.face_sdk.api

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy


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

    override fun analyze(image: ImageProxy) {
        // TODO(2단계): MLKit 검출 파이프라인 연결
        // TODO(3단계): 라이브니스 상태 머신 연결
        // TODO(4단계): 마스크 분류 연결
        image.close()  // 이거 안 하면 다음 프레임이 안 들어옴 — 뼈대에서도 필수
    }

    /**
     * 인증 상태를 초기화하고 새 인증 세션을 시작할 수 있게 합니다.
     * 진행 중이던 판별은 폐기됩니다.
     */
    fun reset() {
        // TODO(3단계): 상태 머신 초기화
    }
}
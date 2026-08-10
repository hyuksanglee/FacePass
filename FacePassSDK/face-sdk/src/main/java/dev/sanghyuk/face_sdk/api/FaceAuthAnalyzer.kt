package dev.sanghyuk.face_sdk.api

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.face.Face
import dev.sanghyuk.face_sdk.detection.FaceDetectorSource
import dev.sanghyuk.face_sdk.detection.MaskDetector
import dev.sanghyuk.face_sdk.internal.FaceCropper
import dev.sanghyuk.face_sdk.internal.rotate
import dev.sanghyuk.face_sdk.liveness.FrameQuality
import dev.sanghyuk.face_sdk.liveness.LivenessStateMachine
import kotlin.math.abs

/**
 * LOGIC SDK의 진입점.
 *
 * CameraX [ImageAnalysis.Analyzer]를 직접 구현하므로, 자체 카메라 프리뷰를
 * 가진 앱은 기존 ImageAnalysis 유스케이스에 이 Analyzer를 연결하는 것만으로
 * 얼굴 인증을 붙일 수 있습니다.
 *
 * 인증 파이프라인: 얼굴 검출 → 마스크 확인 → 액션 라이브니스 → 최적 얼굴 반환
 *
 * 인증이 진행되는 동안 정면 프레임의 품질을 지속적으로 평가하여,
 * 가장 점수가 높은 한 장만 유지했다가 성공 시 반환합니다.
 */
class FaceAuthAnalyzer(
    context: Context,
    private val config: FaceAuthConfig = FaceAuthConfig(),
    private val callback: FaceAuthCallback
) : ImageAnalysis.Analyzer {

    private val detectorSource = FaceDetectorSource()
    private val maskDetector = MaskDetector(context)
    private var finished = false

    private val stateMachine = LivenessStateMachine(config)
    private var lastProgress: AuthProgress? = null

    // 마스크 검사 통과 여부 (인증당 한 번만 검사)
    private var maskChecked = false

    // 지금까지 가장 품질이 좋았던 프레임 (챔피언)
    private var bestBitmap: Bitmap? = null
    private var bestScore: Float = -1f

    override fun analyze(image: ImageProxy) {
        if (finished) { image.close(); return }

        detectorSource.detect(
            imageProxy = image,
            onResult = { faces -> handleFaces(faces, image) },
            onFailure = { error -> fail(PassError.Unknown(error)) },
            onComplete = { image.close() }
        )
    }

    private fun handleFaces(faces: List<Face>, image: ImageProxy) {
        when {
            faces.isEmpty() -> {
                stateMachine.reset()
                maskChecked = false
                clearBest()
                emitProgress(AuthProgress.SEARCHING)
            }
            faces.size >= 2 -> fail(PassError.MultipleFaces)
            else -> {
                val face = faces.first()

                // 마스크 검사: 라이브니스 시작 전, 한 번만
                if (!maskChecked) {
                    val cropped = cropFace(face, image)
                    if (cropped != null) {
                        val masked = maskDetector.isMasked(cropped)
                        cropped.recycle()
                        if (masked) {
                            fail(PassError.MaskDetected)
                            return
                        }
                        maskChecked = true
                    }
                    if (!maskChecked) return
                }

                // 정면 프레임이면 챔피언 갱신 시도
                considerFrame(face, image)

                when (stateMachine.update(face.headEulerAngleY)) {
                    LivenessStateMachine.State.NEUTRAL -> {
                        emitProgress(AuthProgress.AWAITING_ACTION)
                    }
                    LivenessStateMachine.State.ROTATING -> {
                        emitProgress(AuthProgress.ACTION_IN_PROGRESS)
                    }
                    LivenessStateMachine.State.RETURNED -> {
                        succeed()
                    }
                }
            }
        }
    }

    /**
     * 현재 프레임을 평가해, 기존 챔피언보다 품질이 높으면 교체한다.
     * 정면에서 벗어난 프레임은 crop 없이 조기 반환하여 불필요한 연산을 피한다.
     */
    private fun considerFrame(face: Face, image: ImageProxy) {
        // 정면 기준을 벗어난 각도는 후보에서 제외
        if (abs(face.headEulerAngleY) > config.neutralAngleDegrees) return

        val quality = FrameQuality.score(face)
        if (quality <= bestScore) return

        val cropped = cropFace(face, image) ?: return

        bestBitmap?.recycle()
        bestBitmap = cropped
        bestScore = quality
    }

    /** 얼굴 영역을 crop한 비트맵 반환 (실패 시 null) */
    private fun cropFace(face: Face, image: ImageProxy): Bitmap? {
        val bitmap = image.toBitmap().rotate(image.imageInfo.rotationDegrees)
        return FaceCropper.crop(bitmap, face.boundingBox)
    }

    private fun emitProgress(state: AuthProgress) {
        if (lastProgress == state) return
        lastProgress = state
        callback.onProgress(state)
    }

    /** 인증 성공 — 유지하고 있던 최고 품질 프레임을 반환 */
    private fun succeed() {
        if (finished) return

        val best = bestBitmap
        if (best == null) {
            // 유효한 프레임을 한 장도 확보하지 못한 경우
            fail(PassError.Unknown(IllegalStateException("no valid frame")))
            return
        }

        finished = true
        bestBitmap = null   // 소유권이 호출 측으로 넘어가므로 recycle하지 않음
        bestScore = -1f
        callback.onSuccess(best)
    }

    private fun fail(error: PassError) {
        if (finished) return
        finished = true
        clearBest()
        callback.onError(error)
    }

    /** 보관 중인 챔피언 프레임 해제 */
    private fun clearBest() {
        bestBitmap?.recycle()
        bestBitmap = null
        bestScore = -1f
    }

    fun reset() {
        finished = false
        maskChecked = false
        lastProgress = null
        clearBest()
        stateMachine.reset()
    }

    fun close() {
        maskDetector.close()
    }
}
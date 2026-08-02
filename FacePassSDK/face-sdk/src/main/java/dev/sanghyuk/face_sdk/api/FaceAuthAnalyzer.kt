package dev.sanghyuk.face_sdk.api

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.face.Face
import dev.sanghyuk.face_sdk.detection.FaceDetectorSource
import dev.sanghyuk.face_sdk.internal.FaceCropper
import dev.sanghyuk.face_sdk.internal.rotate
import dev.sanghyuk.face_sdk.liveness.FrameQuality
import dev.sanghyuk.face_sdk.liveness.LivenessStateMachine

class FaceAuthAnalyzer(
    private val config: FaceAuthConfig = FaceAuthConfig(),
    private val callback: FaceAuthCallback
) : ImageAnalysis.Analyzer {

    private val detectorSource = FaceDetectorSource()
    private var finished = false

    private val stateMachine = LivenessStateMachine(config)
    private var lastProgress: AuthProgress? = null

    // 후보 프레임: crop된 얼굴 + 품질 점수
    private class Candidate(val bitmap: Bitmap, val score: Float)
    private val candidates = mutableListOf<Candidate>()

    // ① 얼굴 첫 검출 시점을 한 번만 담기 위한 플래그
    private var firstFaceCaptured = false

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
                emitProgress(AuthProgress.SEARCHING)
            }
            faces.size >= 2 -> fail(PassError.MultipleFaces)
            else -> {
                val face = faces.first()

                // ① 얼굴 첫 검출: 이번 인증에서 처음 얼굴이 잡힌 순간 1장
                if (!firstFaceCaptured) {
                    captureCandidate(face, image)
                    firstFaceCaptured = true
                }

                val newState = stateMachine.update(face.headEulerAngleY)

                when (newState) {
                    LivenessStateMachine.State.NEUTRAL -> {
                        // ② 행동 직전(정면 정렬 완료) 1장
                        captureCandidate(face, image)
                        emitProgress(AuthProgress.AWAITING_ACTION)
                    }
                    LivenessStateMachine.State.ROTATING -> {
                        // 고개 돌리는 중 — 옆모습이라 수집 안 함
                        emitProgress(AuthProgress.ACTION_IN_PROGRESS)
                    }
                    LivenessStateMachine.State.RETURNED -> {
                        // ③ 행동 끝(복귀 완료) 1장 후, 베스트 선택
                        captureCandidate(face, image)
                        succeed()
                    }
                }
            }
        }
    }

    /** 현재 프레임의 얼굴을 crop + 품질점수 매겨 후보에 추가 */
    private fun captureCandidate(face: Face, image: ImageProxy) {
        val bitmap = image.toBitmap().rotate(image.imageInfo.rotationDegrees)
        val cropped = FaceCropper.crop(bitmap, face.boundingBox) ?: return
        val quality = FrameQuality.score(face)
        candidates.add(Candidate(cropped, quality))
    }

    private fun emitProgress(state: AuthProgress) {
        if (lastProgress == state) return
        lastProgress = state
        callback.onProgress(state)
    }

    /** 후보 중 품질 점수가 가장 높은 프레임을 최종 결과로 반환 */
    private fun succeed() {
        if (finished) return
        finished = true

        val best = candidates.maxByOrNull { it.score }
        if (best != null) {
            // 베스트 외 나머지 비트맵은 메모리 해제
            candidates.filter { it !== best }.forEach { it.bitmap.recycle() }
            callback.onSuccess(best.bitmap)
        } else {
            finished = false
            fail(PassError.Unknown(IllegalStateException("no valid frame")))
        }
        candidates.clear()
    }

    private fun fail(error: PassError) {
        if (finished) return
        finished = true
        candidates.forEach { it.bitmap.recycle() }
        candidates.clear()
        callback.onError(error)
    }

    fun reset() {
        finished = false
        firstFaceCaptured = false
        lastProgress = null
        candidates.forEach { it.bitmap.recycle() }
        candidates.clear()
        stateMachine.reset()
    }
}
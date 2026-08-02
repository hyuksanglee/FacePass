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

    // 마스크 검사를 이미 통과했는지 (매 프레임 반복 검사 방지)
    private var maskChecked = false

    private class Candidate(val bitmap: Bitmap, val score: Float)
    private val candidates = mutableListOf<Candidate>()

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
                maskChecked = false        // ← 추가: 얼굴 사라지면 마스크 재검사하도록
                firstFaceCaptured = false  // ← 추가: 첫 검출 후보도 다시 담도록
                candidates.forEach { it.bitmap.recycle() }  // ← 담아둔 후보 정리
                candidates.clear()
                emitProgress(AuthProgress.SEARCHING)
            }
            faces.size >= 2 -> fail(PassError.MultipleFaces)
            else -> {
                val face = faces.first()

                // 마스크 검사: 인증 시작 전, 한 번만
                if (!maskChecked) {
                    val cropped = cropFace(face, image)
                    if (cropped != null) {
                        if (maskDetector.isMasked(cropped)) {
                            cropped.recycle()
                            fail(PassError.MaskDetected)
                            return
                        }
                        cropped.recycle()  // 마스크 검사용 crop은 후보와 별개라 해제
                        maskChecked = true
                    }
                    // crop 실패 시 이번 프레임은 넘기고 다음 프레임에 재시도
                    if (!maskChecked) return
                }

                // ① 얼굴 첫 검출
                if (!firstFaceCaptured) {
                    captureCandidate(face, image)
                    firstFaceCaptured = true
                }

                val newState = stateMachine.update(face.headEulerAngleY)

                when (newState) {
                    LivenessStateMachine.State.NEUTRAL -> {
                        captureCandidate(face, image)
                        emitProgress(AuthProgress.AWAITING_ACTION)
                    }
                    LivenessStateMachine.State.ROTATING -> {
                        emitProgress(AuthProgress.ACTION_IN_PROGRESS)
                    }
                    LivenessStateMachine.State.RETURNED -> {
                        captureCandidate(face, image)
                        succeed()
                    }
                }
            }
        }
    }

    /** 얼굴 영역을 crop한 비트맵 반환 (실패 시 null) */
    private fun cropFace(face: Face, image: ImageProxy): Bitmap? {
        val bitmap = image.toBitmap().rotate(image.imageInfo.rotationDegrees)
        return FaceCropper.crop(bitmap, face.boundingBox)
    }

    /** 현재 프레임의 얼굴을 crop + 품질점수 매겨 후보에 추가 */
    private fun captureCandidate(face: Face, image: ImageProxy) {
        val cropped = cropFace(face, image) ?: return
        val quality = FrameQuality.score(face)
        candidates.add(Candidate(cropped, quality))
    }

    private fun emitProgress(state: AuthProgress) {
        if (lastProgress == state) return
        lastProgress = state
        callback.onProgress(state)
    }

    private fun succeed() {
        if (finished) return
        finished = true

        val best = candidates.maxByOrNull { it.score }
        if (best != null) {
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
        maskChecked = false
        lastProgress = null
        candidates.forEach { it.bitmap.recycle() }
        candidates.clear()
        stateMachine.reset()
    }

    fun close() {
        maskDetector.close()
    }
}
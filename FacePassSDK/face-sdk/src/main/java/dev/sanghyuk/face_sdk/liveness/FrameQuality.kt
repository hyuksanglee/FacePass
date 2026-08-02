package dev.sanghyuk.face_sdk.liveness

import com.google.mlkit.vision.face.Face
import kotlin.math.abs

internal object FrameQuality {

    // Face에서 값만 뽑아 넘기는 진입점 (프로덕션용)
    fun score(face: Face): Float = score(
        yaw = face.headEulerAngleY,
        pitch = face.headEulerAngleX,
        roll = face.headEulerAngleZ,
        leftEyeOpen = face.leftEyeOpenProbability,
        rightEyeOpen = face.rightEyeOpenProbability
    )

    // 순수 값만 받는 실제 계산 로직 (테스트용)
    fun score(
        yaw: Float,
        pitch: Float,
        roll: Float,
        leftEyeOpen: Float?,
        rightEyeOpen: Float?
    ): Float {
        val yawScore = angleScore(yaw)
        val pitchScore = angleScore(pitch)
        val rollScore = angleScore(roll)
        val eyeScore = eyeOpenScore(leftEyeOpen, rightEyeOpen)

        return yawScore * 0.35f +
                pitchScore * 0.25f +
                rollScore * 0.15f +
                eyeScore * 0.25f
    }

    private fun angleScore(angleDegrees: Float): Float {
        val normalized = 1f - (abs(angleDegrees) / 30f)
        return normalized.coerceIn(0f, 1f)
    }

    private fun eyeOpenScore(left: Float?, right: Float?): Float {
        if (left == null || right == null) return 0.5f
        return (left + right) / 2f
    }
}
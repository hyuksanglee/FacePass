package dev.sanghyuk.face_sdk.api

/**
 * 인증 동작의 기준값 설정.
 *
 * 모든 값에 검증된 기본값이 있으므로 [FaceAuthConfig()]만으로 시작할 수 있고,
 * 필요한 값만 골라서 조정할 수 있습니다.
 *
 * @property triggerAngleDegrees 회전으로 인정하는 진입 각도. headEulerAngleY의
 *   절댓값이 이 값을 넘으면 "회전함"으로 판정합니다.
 * @property neutralAngleDegrees 중립(정면)으로 인정하는 복귀 각도. 절댓값이
 *   이 값 이내로 돌아오면 "복귀함"으로 판정합니다.
 *   [triggerAngleDegrees]보다 작아야 합니다 (히스테리시스).
 * @property actionTimeoutMillis 액션 지시 후 이 시간 안에 완주하지 못하면
 *   [PassError.ActionTimeout]으로 실패 처리합니다.
 * @property maskConfidenceThreshold 마스크 분류 모델의 출력이 이 값 이상이면
 *   마스크 착용으로 판정하고 [PassError.MaskDetected]를 반환합니다. (0.0~1.0)
 */
data class FaceAuthConfig(
    val triggerAngleDegrees: Float = 25f,
    val neutralAngleDegrees: Float = 10f,
    val actionTimeoutMillis: Long = 15_000L,
    val maskConfidenceThreshold: Float = 0.7f
) {
    init {
        require(neutralAngleDegrees < triggerAngleDegrees) {
            "neutralAngleDegrees($neutralAngleDegrees)는 " +
                    "triggerAngleDegrees($triggerAngleDegrees)보다 작아야 합니다"
        }
        require(maskConfidenceThreshold in 0f..1f) {
            "maskConfidenceThreshold는 0.0~1.0 범위여야 합니다"
        }
        require(actionTimeoutMillis > 0) {
            "actionTimeoutMillis는 양수여야 합니다"
        }
    }
}
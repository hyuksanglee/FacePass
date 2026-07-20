package dev.sanghyuk.face_sdk.api


/**
 * SDK에서 발생하는 모든 실패를 하나의 계층으로 통일한 에러 타입.
 *
 * 코드 체계:
 * - E1xx: 환경 (권한, 모델 준비 상태 등 기기/설정 문제)
 * - E2xx: 검출 (얼굴 검출 단계에서의 실패)
 * - E3xx: 판별 (라이브니스 판별 단계에서의 실패)
 *
 * @property code 호출 측이 로깅/분기에 쓸 수 있는 고유 코드
 * @property retryable 같은 조건에서 재시도하면 성공할 가능성이 있는지
 */
sealed class PassError(
    val code: String,
    val retryable: Boolean
) {
    // E1xx: 환경
    data object CameraDenied : PassError("E101", false)
    data object ModelLoadFailed : PassError("E102", false)
    data object ModelNotReady : PassError("E103", true)

    // E2xx: 검출
    data object MaskDetected : PassError("E201", true)
    data object MultipleFaces : PassError("E202", true)

    // E3xx: 판별
    data object ActionTimeout : PassError("E301", true)

    // 예상 밖 실패의 안전망
    data class Unknown(val cause: Throwable? = null) : PassError("E999", false)
}
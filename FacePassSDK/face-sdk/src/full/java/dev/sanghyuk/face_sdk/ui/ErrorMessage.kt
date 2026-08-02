package dev.sanghyuk.face_sdk.ui


import dev.sanghyuk.face_sdk.api.PassError

/**
 * PassError(에러 코드)를 사용자에게 보여줄 한국어 메시지로 변환한다.
 *
 * SDK는 에러를 code(E201 등)로만 표현하고, 이를 어떤 문구로 보여줄지는
 * UI 계층의 관심사이므로 여기서 매핑한다. (Full SDK 전용)
 */
internal fun PassError.toMessage(): String = when (this) {
    PassError.MaskDetected -> "마스크를 벗고 다시 시도해주세요"
    PassError.MultipleFaces -> "한 명만 화면에 나와주세요"
    PassError.CameraDenied -> "카메라 권한이 필요합니다"
    PassError.ModelNotReady -> "잠시 후 다시 시도해주세요"
    PassError.ModelLoadFailed -> "모델을 불러오지 못했습니다"
    PassError.ActionTimeout -> "시간이 초과되었습니다. 다시 시도해주세요"
    is PassError.Unknown -> "인증에 실패했습니다"
}
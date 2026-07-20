package dev.sanghyuk.face_sdk.api


import android.graphics.Bitmap

/**
 * 인증 결과를 호출 측에 전달하는 콜백.
 *
 * 하나의 인증 세션에서 [onSuccess] 또는 [onError] 중 하나가 정확히 한 번 호출됩니다.
 * [onProgress]는 그 전까지 여러 번 호출될 수 있습니다.
 */
interface FaceAuthCallback {

    /**
     * 인증 성공. STEP 04의 "PASS 얼굴 반환"에 해당.
     *
     * @param face 검출 영역으로 crop된 얼굴 Bitmap
     */
    fun onSuccess(face: Bitmap)

    /**
     * 인증 실패. 모든 실패는 [PassError]로 봉인되어 전달됩니다.
     */
    fun onError(error: PassError)

    /**
     * 진행 상태 변화. UI 안내 문구 갱신용.
     * LOGIC SDK 사용처가 자체 UI에 "천천히 고개를 돌려주세요" 같은
     * 안내를 띄울 수 있도록 제공합니다. 필요 없으면 무시해도 됩니다.
     */
    fun onProgress(state: AuthProgress) {}
}

/**
 * 인증 진행 단계. 파이프라인의 STEP 01~03에 대응.
 */
enum class AuthProgress {
    /** 얼굴을 찾는 중 (STEP 01) */
    SEARCHING,

    /** 얼굴 검출됨, 정면 정렬 확인 중 (STEP 01) */
    ALIGNING,

    /** 액션 지시 중 — 고개를 돌려야 함 (STEP 03) */
    AWAITING_ACTION,

    /** 액션 감지됨, 복귀 대기 중 (STEP 03) */
    ACTION_IN_PROGRESS
}
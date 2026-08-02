package dev.sanghyuk.face_sdk.ui

import androidx.compose.ui.graphics.Color

internal data class AuthUiState(
    val progress: Float,
    val ringColor: Color,
    val instruction: String,
    val statusText: String,
    val phase: Phase
    ){
    enum class Phase { SEARCHING, ALIGNING, ACTION, SUCCESS, FAIL }

    companion object {
        // 시안의 5개 상태를 그대로 값으로

        val Searching = AuthUiState(
            progress = 0f,
            ringColor = FaceAuthColors.Progress,
            instruction = "얼굴을 화면 중앙에 맞춰주세요",
            statusText = "얼굴을 찾는 중…",
            phase = Phase.SEARCHING
        )

        val Aligning = AuthUiState(
            progress = 0.33f,
            ringColor = FaceAuthColors.Progress,
            instruction = "천천히 고개를 돌려주세요",
            statusText = "사람인지 확인 중…",
            phase = Phase.ALIGNING
        )

        val Action = AuthUiState(
            progress = 0.66f,
            ringColor = FaceAuthColors.Action,
            instruction = "다시 원래대로 돌려주세요",
            statusText = "사람인지 확인 중…",
            phase = Phase.ACTION
        )

        val Success = AuthUiState(
            progress = 1f,
            ringColor = FaceAuthColors.Success,
            instruction = "본인 확인이 완료되었습니다",
            statusText = "인증 성공",
            phase = Phase.SUCCESS
        )

        fun fail(message: String) = AuthUiState(
            progress = 1f,
            ringColor = FaceAuthColors.Fail,
            instruction = message,
            statusText = "인증 실패",
            phase = Phase.FAIL
        )
    }
}

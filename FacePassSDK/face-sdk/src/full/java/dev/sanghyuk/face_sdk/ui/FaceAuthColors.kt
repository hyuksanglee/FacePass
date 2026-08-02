package dev.sanghyuk.face_sdk.ui

import androidx.compose.ui.graphics.Color

internal object FaceAuthColors{
    // 상태별 강조색 (링 · 상태 점)
    val Progress = Color(0xFF4A9EEF)   // 파랑 — 진행 중 (찾기/정렬)
    val Action = Color(0xFFF0A835)     // 앰버 — 동작 요구 (고개 돌리기)
    val Success = Color(0xFF2CB88A)    // 초록 — 성공
    val Fail = Color(0xFFEC5A59)       // 빨강 — 실패

    // 배경 · 레이어
    val Dim = Color(0xFF0F1219).copy(alpha = 0.85f)      // 반투명 다크 레이어 베이스
    val ChipBg = Color(0xFF1B2029).copy(alpha = 0.75f)    // 상태 칩 배경

    // 텍스트
    val Title = Color(0xFFE8ECF2).copy(0.55f)      // 타이틀
    val Instruction = Color(0xFFF0F3F7)                     // 상단 안내
    val StatusText = Color(0xFFC4CCD8)                      // 하단 칩 문구
    val Watermark = Color(0xFFFFFFFF).copy(alpha = 0.3f)   //

    // 트랙 (링 배경)
    val RingTrack = Color(0xFFFFFFFF).copy(alpha = 0.15f)
}
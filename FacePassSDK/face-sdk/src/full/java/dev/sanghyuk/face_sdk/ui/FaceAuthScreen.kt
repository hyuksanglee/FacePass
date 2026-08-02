package dev.sanghyuk.face_sdk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun FaceAuthScreen(
    state: AuthUiState,
    modifier: Modifier = Modifier,
    cameraContent: @Composable () -> Unit = {},
    onRetry: () -> Unit = {}
) {
    // 원 밖 전체를 불투명하게 채우는 배경 (사각형 카메라 흔적 제거)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FaceAuthColors.Dim)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "얼굴인증",
            color = FaceAuthColors.Title,
            fontSize = 16.sp
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = state.instruction,
            color = FaceAuthColors.Instruction,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(40.dp))

        Box(contentAlignment = Alignment.Center) {

            val cameraSize = 300.dp
            val ringSize = cameraSize + 28.dp
            Box(
                modifier = Modifier
                    .size(cameraSize)
                    .clip(CircleShape)
            ) {
                cameraContent()
            }
            ProgressRing(
                progress = state.progress,
                color = state.ringColor,
                size = ringSize
            )
        }

        Spacer(Modifier.height(40.dp))

        // 상태 표시 pill (항상 표시)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF1B2029).copy(alpha = 0.75f))
                .padding(horizontal = 18.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(state.ringColor)
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = state.statusText,
                color = Color(0xFFC4CCD8),
                fontSize = 12.sp
            )
        }

        // FAIL일 때만 pill 아래에 "다시 시도" 버튼 추가
        if (state.phase == AuthUiState.Phase.FAIL) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("다시 시도")
            }
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = "FacePass SDK",
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 11.sp
        )
    }
}

// ── 미리보기: 상태별 확인 ──
@Preview(name = "1. 얼굴 찾는 중", showBackground = true, backgroundColor = 0xFF2A3040)
@Composable
private fun PreviewSearching() {
    FaceAuthScreen(state = AuthUiState.Searching)
}

@Preview(name = "2. 고개 돌리기", showBackground = true, backgroundColor = 0xFF2A3040)
@Composable
private fun PreviewAligning() {
    FaceAuthScreen(state = AuthUiState.Aligning)
}

@Preview(name = "3. 원래대로", showBackground = true, backgroundColor = 0xFF2A3040)
@Composable
private fun PreviewAction() {
    FaceAuthScreen(state = AuthUiState.Action)
}

@Preview(name = "4. 성공", showBackground = true, backgroundColor = 0xFF2A3040)
@Composable
private fun PreviewSuccess() {
    FaceAuthScreen(state = AuthUiState.Success)
}

@Preview(name = "5. 실패", showBackground = true, backgroundColor = 0xFF2A3040)
@Composable
private fun PreviewFail() {
    FaceAuthScreen(state = AuthUiState.fail("마스크를 벗고 다시 시도해주세요"))
}
package dev.sanghyuk.face_sdk.liveness

import org.junit.Assert.assertTrue
import org.junit.Test

class FrameQualityTest {

    @Test
    fun `정면일수록 점수가 높다`() {
        val frontal = FrameQuality.score(
            yaw = 0f, pitch = 0f, roll = 0f,
            leftEyeOpen = 1f, rightEyeOpen = 1f
        )
        val turned = FrameQuality.score(
            yaw = 25f, pitch = 0f, roll = 0f,
            leftEyeOpen = 1f, rightEyeOpen = 1f
        )
        assertTrue("정면이 고개 돌린 것보다 높아야 함", frontal > turned)
    }

    @Test
    fun `완전 정면은 최고점에 가깝다`() {
        val score = FrameQuality.score(
            yaw = 0f, pitch = 0f, roll = 0f,
            leftEyeOpen = 1f, rightEyeOpen = 1f
        )
        assertTrue(score > 0.95f)
    }

    @Test
    fun `눈 확률이 null이면 중립값으로 처리된다`() {
        // classification 꺼진 상황 — 크래시 없이 각도만으로 계산돼야 함
        val score = FrameQuality.score(
            yaw = 0f, pitch = 0f, roll = 0f,
            leftEyeOpen = null, rightEyeOpen = null
        )
        assertTrue(score > 0f)
    }

    @Test
    fun `각도가 30도를 넘으면 각도 점수는 0으로 수렴한다`() {
        val extreme = FrameQuality.score(
            yaw = 45f, pitch = 45f, roll = 45f,
            leftEyeOpen = 0f, rightEyeOpen = 0f
        )
        assertTrue("모든 요소 최악이면 0에 가까움", extreme < 0.05f)
    }
}
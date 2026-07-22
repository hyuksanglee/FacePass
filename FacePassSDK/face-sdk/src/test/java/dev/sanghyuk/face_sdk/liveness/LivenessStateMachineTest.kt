package dev.sanghyuk.face_sdk.liveness

import dev.sanghyuk.face_sdk.api.FaceAuthConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class LivenessStateMachineTest {

    private val config = FaceAuthConfig()

    @Test
    fun `정면에서 시작하면 NEUTRAL`() {
        val sm = LivenessStateMachine(config)
        assertEquals(LivenessStateMachine.State.NEUTRAL, sm.update(0f))
    }

    @Test
    fun `충분히 돌리고 복귀하면 RETURNED`(){
        val sm = LivenessStateMachine(config)

        sm.update(0f)
        sm.update(15f)
        sm.update(30f)
        sm.update(20f)
        val result = sm.update(5f)
        assertEquals(LivenessStateMachine.State.RETURNED, result)
    }

    @Test
    fun `살짝만 돌리면 통과 못함`() {
        val sm = LivenessStateMachine(config)
        sm.update(0f)
        sm.update(20f)    // 25° 못 넘음 → NEUTRAL 유지
        val result = sm.update(0f)
        assertEquals(LivenessStateMachine.State.NEUTRAL, result)  // 완주 아님
    }

    @Test
    fun `경계값 노이즈에도 상태가 안정적 (히스테리시스)`() {
        val sm = LivenessStateMachine(config)
        sm.update(0f)
        sm.update(30f)    // ROTATING 진입
        // 복귀 중 11~13도를 오가도 (10° 초과) 아직 완주 아님
        sm.update(13f)
        sm.update(11f)
        assertEquals(LivenessStateMachine.State.ROTATING, sm.state)
        val result = sm.update(8f)   // 10° 이내로 확실히 복귀해야 완주
        assertEquals(LivenessStateMachine.State.RETURNED, result)
    }
}

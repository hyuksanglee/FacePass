package dev.sanghyuk.face_sdk.liveness

import dev.sanghyuk.face_sdk.api.FaceAuthConfig

class LivenessStateMachine(
    private val config: FaceAuthConfig
) {
    enum class State { NEUTRAL, ROTATING, RETURNED}

    var state: State = State.NEUTRAL
        private set

    fun update(angleY: Float): State {
        val abs = kotlin.math.abs(angleY)

        when (state) {
            State.NEUTRAL -> {
                if(abs >= config.triggerAngleDegrees) {
                    state = State.ROTATING
                }
            }
            State.ROTATING -> {
                if(abs <= config.neutralAngleDegrees) {
                    state = State.RETURNED
                }

            }
            State.RETURNED -> {

            }
        }
        return state
    }
    fun reset(){
        state = State.NEUTRAL
    }
}
package dev.sanghyuk.face_sdk.api

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
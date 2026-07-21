package dev.sanghyuk.face_sdk.detection

import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

/**
 * MLKit 얼굴 검출을 감싸는 내부 컴포넌트.
 * detector는 한 번만 생성해 재사용한다 (매 프레임 생성 금지).
 *
 */

// internal class를 사용해서 다른 모듈에서 접근이 불가능하고 다운을 하더라도 수정이 불가
internal class FaceDetectorSource {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
    )


    // imageProxy.image 사용 하기 위해 안내 문구를 읽었는지 확인
    // * 반환된 Image 객체의 .close()를 개발자가 직접 호출하면 안 됩니다.
    // * imageProxy를 통해 .close() 사용하세요
    @OptIn(ExperimentalGetImage::class)
    fun detect(
        imageProxy: ImageProxy,
        onResult: (List<Face>) -> Unit,
        onFailure: (Throwable) -> Unit,
        onComplete: () -> Unit,
    ){
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            onComplete()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        detector.process(inputImage)
            .addOnSuccessListener { faces -> onResult(faces)  }
            .addOnFailureListener { error -> onFailure(error) }
            .addOnCompleteListener { onComplete() }
    }

    fun close() = detector.close()

}
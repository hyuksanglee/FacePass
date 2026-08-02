package dev.sanghyuk.demo_logic

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import dev.sanghyuk.face_sdk.api.AuthProgress
import dev.sanghyuk.face_sdk.api.FaceAuthAnalyzer
import dev.sanghyuk.face_sdk.api.FaceAuthCallback
import dev.sanghyuk.face_sdk.api.PassError
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var resultImage: ImageView
    private val analysisExecutor = Executors.newSingleThreadExecutor()

    // context가 필요하므로 onCreate에서 생성
    private lateinit var analyzer: FaceAuthAnalyzer

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera()
            else statusText.text = "카메라 권한이 필요합니다"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        statusText = findViewById(R.id.statusText)
        resultImage = findViewById(R.id.resultImage)

        // ── SDK 연동 ──────────────────────────────
        analyzer = FaceAuthAnalyzer(
            context = this,
            callback = object : FaceAuthCallback {
                override fun onSuccess(face: Bitmap) {
                    runOnUiThread {
                        statusText.text = "인증 성공 (${face.width}x${face.height})"
                        resultImage.setImageBitmap(face)
                    }
                }

                override fun onError(error: PassError) {
                    runOnUiThread {
                        statusText.text = if (error.retryable)
                            "실패: ${error.code} — 다시 시도" else "실패: ${error.code}"
                    }
                }

                override fun onProgress(state: AuthProgress) {
                    runOnUiThread {
                        statusText.text = when (state) {
                            AuthProgress.SEARCHING -> "얼굴을 화면에 맞춰주세요"
                            AuthProgress.ALIGNING -> "정면을 바라봐주세요"
                            AuthProgress.AWAITING_ACTION -> "천천히 고개를 돌려주세요"
                            AuthProgress.ACTION_IN_PROGRESS -> "좋아요, 다시 정면으로"
                        }
                    }
                }
            }
        )
        // ────────────────────────────────────────────

        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            val provider = providerFuture.get()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider =
                    findViewById<PreviewView>(R.id.previewView).surfaceProvider
            }

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(analysisExecutor, analyzer) }

            provider.unbindAll()
            provider.bindToLifecycle(
                this, CameraSelector.DEFAULT_FRONT_CAMERA, preview, analysis
            )
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        analysisExecutor.shutdown()
        analyzer.close()   // TFLite 인터프리터 해제
    }
}
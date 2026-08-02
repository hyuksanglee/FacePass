package dev.sanghyuk.face_sdk.detection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * crop된 얼굴이 마스크를 착용했는지 판정하는 TFLite 분류기.
 *
 * AIZOOTech face_mask_detection 모델(SSD 구조)을 사용하되,
 * ML Kit이 이미 얼굴을 검출·crop하므로 박스 좌표(loc_branch)는 무시하고
 * 분류 결과(cls_branch)의 마스크 확률만 확인한다.
 *
 * 입력:  260×260 RGB, 0~1 정규화, float32
 * 출력:  cls_branch [1, 5972, 2] — [마스크 확률, 노마스크 확률]
 *        (loc_branch [1, 5972, 4]는 사용 안 함)
 */
internal class MaskDetector(context: Context) {

    private val interpreter: Interpreter

    init {
        val model = loadModelFile(context, MODEL_FILE)
        interpreter = Interpreter(model, Interpreter.Options())
    }

    /**
     * 마스크 착용 여부 판정.
     * @return true = 마스크 감지됨, false = 마스크 없음
     */
    fun isMasked(faceBitmap: Bitmap): Boolean {
        val input = preprocess(faceBitmap)

        val locOutput = Array(1) { Array(NUM_ANCHORS) { FloatArray(4) } }
        val clsOutput = Array(1) { Array(NUM_ANCHORS) { FloatArray(2) } }

        val outputs = mapOf(
            0 to locOutput,
            1 to clsOutput
        )
        interpreter.runForMultipleInputsOutputs(arrayOf(input), outputs)

        var maxMaskProb = 0f
        var maxNoMaskProb = 0f
        for (i in 0 until NUM_ANCHORS) {
            if (clsOutput[0][i][0] > maxMaskProb) maxMaskProb = clsOutput[0][i][0]
            if (clsOutput[0][i][1] > maxNoMaskProb) maxNoMaskProb = clsOutput[0][i][1]
        }

        android.util.Log.d(
            "MaskDetector",
            "maxMask=$maxMaskProb, maxNoMask=$maxNoMaskProb, threshold=$CONF_THRESHOLD"
        )

        return maxMaskProb >= CONF_THRESHOLD
    }

    /** 비트맵 → 260×260 리사이즈 + 0~1 정규화 → float32 ByteBuffer */
    private fun preprocess(bitmap: Bitmap): ByteBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)

        val buffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3)
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        resized.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        for (pixel in pixels) {
            // 각 채널을 0~1로 정규화 (원본 전처리: image / 255.0)
            buffer.putFloat(Color.red(pixel) / 255f)
            buffer.putFloat(Color.green(pixel) / 255f)
            buffer.putFloat(Color.blue(pixel) / 255f)
        }
        buffer.rewind()
        return buffer
    }

    private fun loadModelFile(context: Context, fileName: String): MappedByteBuffer {
        val fd = context.assets.openFd(fileName)
        FileInputStream(fd.fileDescriptor).use { input ->
            val channel = input.channel
            return channel.map(
                FileChannel.MapMode.READ_ONLY,
                fd.startOffset,
                fd.declaredLength
            )
        }
    }

    fun close() {
        interpreter.close()
    }

    companion object {
        private const val MODEL_FILE = "face_mask_detection.tflite"
        private const val INPUT_SIZE = 260
        private const val NUM_ANCHORS = 5972
        private const val MASK_INDEX = 0        // cls_branch[0] = 마스크
        private const val CONF_THRESHOLD = 0.5f // 원본 conf_thresh와 동일, 튜닝 가능
    }
}
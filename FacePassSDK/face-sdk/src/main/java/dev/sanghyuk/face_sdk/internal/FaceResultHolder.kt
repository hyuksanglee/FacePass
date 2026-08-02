package dev.sanghyuk.face_sdk.internal

import android.graphics.Bitmap

object FaceResultHolder {
    private var result: Bitmap? = null

    fun set(bitmap: Bitmap) { result = bitmap }

    /** 값을 꺼내면서 홀더를 비운다 (한 번만 소비) */
    fun consume(): Bitmap? {
        val b = result
        result = null
        return b
    }
}
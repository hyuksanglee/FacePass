package dev.sanghyuk.face_sdk.internal

import android.graphics.Bitmap
import android.graphics.Matrix

internal fun Bitmap.rotate(degrees: Int): Bitmap {
    if (degrees == 0) return this

    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
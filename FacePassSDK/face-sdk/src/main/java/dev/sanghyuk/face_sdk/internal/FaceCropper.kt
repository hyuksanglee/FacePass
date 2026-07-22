package dev.sanghyuk.face_sdk.internal

import android.graphics.Bitmap
import android.graphics.Rect

internal object FaceCropper{
    fun crop(source: Bitmap, box: Rect, paddingRatio: Float = 0.2f): Bitmap? {
        val padX = (box.width() * paddingRatio).toInt()
        val padY = (box.height() * paddingRatio).toInt()

        val left = (box.left - padX).coerceAtLeast(0)
        val top = (box.top - padY).coerceAtLeast(0)
        val right = (box.right + padX).coerceAtMost(source.width)
        val bottom = (box.bottom + padY).coerceAtMost(source.height)

        val width = right - left
        val height = bottom - top

        if (width <= 0 || height <= 0) return null

        return Bitmap.createBitmap(source, left, top, width, height)
    }

}
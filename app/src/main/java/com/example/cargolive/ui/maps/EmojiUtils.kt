package com.example.cargolive.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object EmojiUtils {

    fun createEmojiBitmap(emoji: String, size: Float = 80f): BitmapDescriptor {
        val paint = Paint()
        paint.textSize = size
        paint.textAlign = Paint.Align.LEFT
        paint.isAntiAlias = true

        val baseline = (paint.descent() - paint.ascent())
        val width = (paint.measureText(emoji) + 0.5f).toInt()
        val height = (baseline + 0.5f).toInt()

        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        canvas.drawText(emoji, 0f, -paint.ascent(), paint)

        return BitmapDescriptorFactory.fromBitmap(image)
    }
}

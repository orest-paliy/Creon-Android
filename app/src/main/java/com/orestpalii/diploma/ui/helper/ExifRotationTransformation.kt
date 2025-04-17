package com.orestpalii.diploma.ui.helper

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import com.squareup.picasso.Transformation
import java.net.URL

class ExifRotationTransformation(
    private val imageUrl: String
) : Transformation {
    override fun transform(source: Bitmap): Bitmap {
        // читаємо EXIF із мережевого потоку в бекграунді
        val input = URL(imageUrl).openStream()
        val exif = ExifInterface(input)
        input.close()

        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        val angle = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 ->   90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else                                   ->   0f
        }
        if (angle == 0f) return source

        // повертаємо повернутий бітмап
        val matrix = Matrix().apply { postRotate(angle) }
        val rotated = Bitmap.createBitmap(
            source, 0, 0, source.width, source.height, matrix, true
        )
        source.recycle()
        return rotated
    }

    override fun key() = "exifRotation($imageUrl)"
}

package ru.trainingapp.core.ui.component

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor

class ZoomCropContentScale(
    private val zoom: Float,
) : ContentScale {
    override fun computeScaleFactor(
        srcSize: Size,
        dstSize: Size,
    ): ScaleFactor {
        val cropScale = ContentScale.Crop.computeScaleFactor(srcSize, dstSize)

        return ScaleFactor(
            scaleX = cropScale.scaleX * zoom,
            scaleY = cropScale.scaleY * zoom,
        )
    }
}

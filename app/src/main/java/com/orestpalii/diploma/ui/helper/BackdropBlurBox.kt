package com.orestpalii.diploma.ui.helper

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.draw.clip

@Composable
fun BackdropBlurBox(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 16.dp,
    cornerRadius: Dp = 30.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val context = LocalContext.current
    val blurPx = with(LocalDensity.current) { blurRadius.toPx() }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        AndroidView(
            factory = {
                FrameLayout(it).apply {
                    setBackgroundDrawable(null)
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    setRenderEffect(
                        RenderEffect.createBlurEffect(
                            blurPx,
                            blurPx,
                            Shader.TileMode.CLAMP
                        )
                    )
                }
            },
            modifier = modifier
                .clip(RoundedCornerShape(cornerRadius))
                .background(Color.White.copy(alpha = 0.2f))
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)),
        content = content
    )
}

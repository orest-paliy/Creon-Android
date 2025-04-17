package com.orestpalii.diploma.ui.helper

import android.app.Activity
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun SetStatusBarColor(
    color: Color = Color.White,
    useDarkIcons: Boolean = true
) {
    val view = LocalView.current
    val activity = (LocalContext.current as? Activity)

    SideEffect {
        activity?.window?.statusBarColor = color.toArgb()

        val windowInsetsController =
            activity?.let { WindowCompat.getInsetsController(it.window, view) }
        if (windowInsetsController != null) {
            windowInsetsController.isAppearanceLightStatusBars = useDarkIcons
        }
    }
}

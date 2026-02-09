package io.agora.board.sample.util

import android.app.Activity
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import io.agora.board.forge.sample.R
import io.agora.board.forge.slide.SlideApplication
import io.agora.board.sample.component.FgsSlideControlView

fun Activity.attachSlideDevView(slideApp: SlideApplication?) {
    try {
        if (slideApp == null) {
            return
        }
        val controlView = FgsSlideControlView(this).apply {
            attachApp(slideApp)
            id = R.id.fgs_slide_control_view
        }
        val layout: FrameLayout = window.decorView.findViewById(android.R.id.content)
        layout.addView(
            controlView,
            LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
            ),
        )
    } catch (ignored: Exception) {
    }
}

fun Activity.detachSlideDevView() {
    try {
        val layout: FrameLayout = window.decorView.findViewById(android.R.id.content)
        val controlView = layout.findViewById<FgsSlideControlView>(R.id.fgs_slide_control_view)
        layout.removeView(controlView)
    } catch (ignored: Exception) {
    }
}

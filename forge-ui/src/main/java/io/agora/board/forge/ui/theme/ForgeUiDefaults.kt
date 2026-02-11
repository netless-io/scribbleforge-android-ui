package io.agora.board.forge.ui.theme

import android.content.Context
import androidx.core.content.ContextCompat
import io.agora.board.forge.ui.R
import io.agora.board.forge.ui.component.state.DrawState
import io.agora.board.forge.ui.model.ToolType

object ForgeUiDefaults {
    val strokeColors = listOf(
        R.color.fcr_whiteboard_color_blue,
        R.color.fcr_whiteboard_color_purple,
        R.color.fcr_whiteboard_color_red,
        R.color.fcr_whiteboard_color_green,
        R.color.fcr_whiteboard_color_yellow,
    )

    val strokeIndex: Int
        get() {
            return 0
        }

    fun defaultDrawState(context: Context): DrawState {
        return DrawState(
            toolType = ToolType.CURVE,
            strokeWidth = 4,
            strokeColor = ContextCompat.getColor(context, strokeColors[strokeIndex]),
            backgroundColor = ContextCompat.getColor(context, R.color.fcr_whiteboard_bg_white),
        )
    }
}

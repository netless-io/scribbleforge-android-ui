package io.agora.board.forge.ui.theme

import io.agora.board.forge.ui.R
import io.agora.board.forge.ui.ForgeUiToolType

class ForgeUiDefaultProvider : ForgeUiProvider {
    override fun toolIcon(toolType: ForgeUiToolType): Int = when (toolType) {
        ForgeUiToolType.SELECTOR -> R.mipmap.fcr_whiteboard_whitechoose
        ForgeUiToolType.LASER_POINTER -> R.mipmap.fcr_whiteboard_laserpen2
        ForgeUiToolType.ERASER -> R.mipmap.fcr_mobile_whiteboard_eraser
        ForgeUiToolType.CLICKER -> R.drawable.fcr_ic_tool_clicker
        ForgeUiToolType.TEXT -> R.mipmap.fcr_mobile_whiteboard_text
        ForgeUiToolType.CURVE -> R.mipmap.fcr_whiteboard_pen1
        ForgeUiToolType.STRAIGHT -> R.mipmap.fcr_whiteboard_shap_line
        ForgeUiToolType.ARROW -> R.mipmap.fcr_whiteboard_shap_arrow
        ForgeUiToolType.RECTANGLE -> R.mipmap.fcr_whiteboard_shap_square
        ForgeUiToolType.TRIANGLE -> R.mipmap.fcr_whiteboard_shap_triangle
        ForgeUiToolType.RHOMBUS -> R.mipmap.fcr_whiteboard_shap_rehumbus
        ForgeUiToolType.PENTAGRAM -> 0
        ForgeUiToolType.ELLIPSE -> R.mipmap.fcr_whiteboard_shap_circle
        ForgeUiToolType.HAND -> R.mipmap.fcr_whiteboard_hand
        else -> 0
    }

    override fun toolToast(toolType: ForgeUiToolType): Int = when (toolType) {
        ForgeUiToolType.SELECTOR -> R.string.fcr_board_toast_selector
        ForgeUiToolType.LASER_POINTER -> R.string.fcr_board_toast_laser
        ForgeUiToolType.ERASER -> R.string.fcr_board_toast_eraser
        ForgeUiToolType.CLICKER -> R.string.fcr_board_toast_clicker
        ForgeUiToolType.TEXT -> R.string.fcr_board_toast_text
        ForgeUiToolType.CURVE -> R.string.fcr_board_toast_curve
        ForgeUiToolType.STRAIGHT -> R.string.fcr_board_toast_straight
        ForgeUiToolType.ARROW -> R.string.fcr_board_toast_arrow
        ForgeUiToolType.RECTANGLE -> R.string.fcr_board_toast_rectangle
        ForgeUiToolType.TRIANGLE -> R.string.fcr_board_toast_triangle
        ForgeUiToolType.RHOMBUS -> 0
        ForgeUiToolType.PENTAGRAM -> 0
        ForgeUiToolType.ELLIPSE -> R.string.fcr_board_toast_circle
        ForgeUiToolType.HAND -> R.string.fcr_board_toast_hand
    }

    override fun defaultStrokeColor(): Int {
        return 0
    }
}

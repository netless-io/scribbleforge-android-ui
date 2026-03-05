package io.agora.board.forge.ui.theme

import io.agora.board.forge.ui.R
import io.agora.board.forge.whiteboard.WhiteboardToolType

class ForgeUiDefaultProvider : ForgeUiProvider {
    override fun toolIcon(toolType: WhiteboardToolType): Int = when (toolType) {
        WhiteboardToolType.SELECTOR -> R.mipmap.fcr_whiteboard_whitechoose
        WhiteboardToolType.LASER -> R.mipmap.fcr_whiteboard_laserpen2
        WhiteboardToolType.ERASER -> R.mipmap.fcr_mobile_whiteboard_eraser
        WhiteboardToolType.POINTER -> R.drawable.fcr_ic_tool_clicker
        WhiteboardToolType.TEXT -> R.mipmap.fcr_mobile_whiteboard_text
        WhiteboardToolType.CURVE -> R.mipmap.fcr_whiteboard_pen1
        WhiteboardToolType.LINE -> R.mipmap.fcr_whiteboard_shap_line
        WhiteboardToolType.ARROW -> R.mipmap.fcr_whiteboard_shap_arrow
        WhiteboardToolType.RECTANGLE -> R.mipmap.fcr_whiteboard_shap_square
        WhiteboardToolType.TRIANGLE -> R.mipmap.fcr_whiteboard_shap_triangle
        WhiteboardToolType.ELLIPSE -> R.mipmap.fcr_whiteboard_shap_circle
        WhiteboardToolType.GRAB -> R.mipmap.fcr_whiteboard_hand
        else -> 0
    }

    override fun toolToast(toolType: WhiteboardToolType): Int = when (toolType) {
        WhiteboardToolType.SELECTOR -> R.string.fcr_board_toast_selector
        WhiteboardToolType.LASER -> R.string.fcr_board_toast_laser
        WhiteboardToolType.ERASER -> R.string.fcr_board_toast_eraser
        WhiteboardToolType.POINTER -> R.string.fcr_board_toast_clicker
        WhiteboardToolType.TEXT -> R.string.fcr_board_toast_text
        WhiteboardToolType.CURVE -> R.string.fcr_board_toast_curve
        WhiteboardToolType.LINE -> R.string.fcr_board_toast_straight
        WhiteboardToolType.ARROW -> R.string.fcr_board_toast_arrow
        WhiteboardToolType.RECTANGLE -> R.string.fcr_board_toast_rectangle
        WhiteboardToolType.TRIANGLE -> R.string.fcr_board_toast_triangle
        WhiteboardToolType.ELLIPSE -> R.string.fcr_board_toast_circle
        WhiteboardToolType.GRAB -> R.string.fcr_board_toast_hand
        else -> 0
    }

    override fun defaultStrokeColor(): Int {
        return 0
    }
}

package io.agora.board.forge.ui.model

import io.agora.board.forge.ui.R

/**
 * author : fenglibin
 * date : 2024/7/3
 * description :
 */
fun ToolType.imgResId(): Int = when (this) {
    ToolType.SELECTOR -> R.mipmap.fcr_whiteboard_whitechoose
    ToolType.LASER_POINTER -> R.mipmap.fcr_whiteboard_laser
    ToolType.ERASER -> R.mipmap.fcr_mobile_whiteboard_eraser
    ToolType.CLICKER -> R.drawable.fcr_ic_tool_clicker
    ToolType.TEXT -> R.mipmap.fcr_mobile_whiteboard_text
    ToolType.CURVE -> R.mipmap.fcr_whiteboard_pen1
    ToolType.STRAIGHT -> R.mipmap.fcr_whiteboard_shap_line
    ToolType.ARROW -> R.mipmap.fcr_whiteboard_shap_arrow
    ToolType.RECTANGLE -> R.mipmap.fcr_whiteboard_shap_square
    ToolType.TRIANGLE -> R.mipmap.fcr_whiteboard_shap_triangle
    ToolType.RHOMBUS -> R.mipmap.fcr_whiteboard_shap_rehumbus
    ToolType.PENTAGRAM -> 0
    ToolType.ELLIPSE -> R.mipmap.fcr_whiteboard_shap_circle
    ToolType.HAND -> R.mipmap.fcr_whiteboard_hand
}

fun ToolType.toastResId() = when (this) {
    ToolType.SELECTOR -> R.string.fcr_board_toast_selector
    ToolType.LASER_POINTER -> R.string.fcr_board_toast_laser
    ToolType.ERASER -> R.string.fcr_board_toast_eraser
    ToolType.CLICKER -> R.string.fcr_board_toast_clicker
    ToolType.TEXT -> R.string.fcr_board_toast_text
    ToolType.CURVE -> R.string.fcr_board_toast_curve
    ToolType.STRAIGHT -> R.string.fcr_board_toast_straight
    ToolType.ARROW -> R.string.fcr_board_toast_arrow
    ToolType.RECTANGLE -> R.string.fcr_board_toast_rectangle
    ToolType.TRIANGLE -> R.string.fcr_board_toast_triangle
    ToolType.RHOMBUS -> 0
    ToolType.PENTAGRAM -> 0
    ToolType.ELLIPSE -> R.string.fcr_board_toast_circle
    ToolType.HAND -> R.string.fcr_board_toast_hand
}

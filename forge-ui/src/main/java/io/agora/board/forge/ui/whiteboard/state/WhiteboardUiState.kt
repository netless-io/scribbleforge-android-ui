package io.agora.board.forge.ui.whiteboard.state

import io.agora.board.forge.whiteboard.WhiteboardToolType

/**
 * 白板 UI 状态，仅包含由 WhiteboardStateStore 管理的绘制相关状态。
 */
data class WhiteboardUiState(
    val toolType: WhiteboardToolType,
    val strokeWidth: Int,
    val strokeColor: Int,
    val fontSize: Float = 32f,
    val backgroundColor: Int,
    val undo: Boolean = false,
    val redo: Boolean = false,
    val canDraw: Boolean = true
) {
    fun toDrawState(): DrawState = DrawState(
        toolType = toolType,
        strokeWidth = strokeWidth,
        strokeColor = strokeColor,
        fontSize = fontSize,
        backgroundColor = backgroundColor,
        undo = undo,
        redo = redo,
        canDraw = canDraw
    )
}

/**
 * 供子组件（ToolBox、ColorPick 等）使用的绘制配置视图。
 */
data class DrawState(
    val toolType: WhiteboardToolType,
    val strokeWidth: Int,
    val strokeColor: Int,
    val fontSize: Float = 32f,
    val backgroundColor: Int,
    val undo: Boolean = false,
    val redo: Boolean = false,
    val canDraw: Boolean = true
) {
    fun toWhiteboardUiState(): WhiteboardUiState = WhiteboardUiState(
        toolType = toolType,
        strokeWidth = strokeWidth,
        strokeColor = strokeColor,
        fontSize = fontSize,
        backgroundColor = backgroundColor,
        undo = undo,
        redo = redo,
        canDraw = canDraw
    )
}

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
)

package io.agora.board.forge.ui.whiteboard.state

import io.agora.board.forge.whiteboard.WhiteboardToolType

sealed interface WhiteboardUiAction {
    // 工具与绘制
    data class ChangeTool(val tool: WhiteboardToolType) : WhiteboardUiAction
    data class ChangeStrokeColor(val color: Int) : WhiteboardUiAction
    data class ChangeStrokeWidth(val width: Int) : WhiteboardUiAction
    data class ChangeBackground(val color: Int) : WhiteboardUiAction
    data class UpdateUndoRedo(val undo: Boolean, val redo: Boolean) : WhiteboardUiAction
    data class WritableChanged(val writable: Boolean) : WhiteboardUiAction
}

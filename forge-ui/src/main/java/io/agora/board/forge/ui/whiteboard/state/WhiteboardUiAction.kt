package io.agora.board.forge.ui.whiteboard.state

import io.agora.board.forge.ui.ForgeUiToolType

sealed interface WhiteboardUiAction {
    // 工具
    data class ChangeTool(val tool: ForgeUiToolType) : WhiteboardUiAction
    data class ChangeStrokeColor(val color: Int) : WhiteboardUiAction
    data class ChangeStrokeWidth(val width: Int) : WhiteboardUiAction
    data class ChangeBackground(val color: Int) : WhiteboardUiAction
    data class UpdateUndoRedo(val undo: Boolean, val redo: Boolean) : WhiteboardUiAction

    // 浮层控制
    data object ShowToolPanel: WhiteboardUiAction
    data object ToggleToolPanel : WhiteboardUiAction
    data object ToggleStrokePanel : WhiteboardUiAction
    data object ToggleDownloadPanel : WhiteboardUiAction
    data object ToggleBgPanel : WhiteboardUiAction
    data object HideDownloadPanel : WhiteboardUiAction
    data object HideBgPanel : WhiteboardUiAction
    data object HideAllPanel : WhiteboardUiAction

    // 下载状态
    data object StartDownloading : WhiteboardUiAction
    data object FinishDownloading : WhiteboardUiAction
    data object DownloadFailed : WhiteboardUiAction

    // SDK 回调同步
    data class SyncToolFromWhiteboard(val tool: ForgeUiToolType) : WhiteboardUiAction

}

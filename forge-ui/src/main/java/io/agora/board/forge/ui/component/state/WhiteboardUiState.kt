package io.agora.board.forge.ui.component.state

import io.agora.board.forge.ui.model.ToolType

data class WhiteboardUiState(
    val drawState: DrawState,
    val layoutState: LayoutState,
    val isDownloading: Boolean = false
)

data class DrawState(
    val toolType: ToolType,
    val strokeWidth: Int,
    val strokeColor: Int,
    val backgroundColor: Int,
    val undo: Boolean = false,
    val redo: Boolean = false,
)

data class LayoutState(
    var strokeShown: Boolean = false,
    var toolShown: Boolean = false,
    var downloadShown: Boolean = false,
    var bgPickShown: Boolean = false
)

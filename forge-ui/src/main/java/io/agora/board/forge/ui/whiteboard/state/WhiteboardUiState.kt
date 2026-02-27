package io.agora.board.forge.ui.whiteboard.state

import io.agora.board.forge.ui.ForgeUiToolType

data class WhiteboardUiState(
    val drawState: DrawState,
    val layoutState: LayoutState,
    val isDownloading: Boolean = false
)

data class DrawState(
    val toolType: ForgeUiToolType,
    val strokeWidth: Int,
    val strokeColor: Int,
    val fontSize: Float = 32f,
    val backgroundColor: Int,
    val undo: Boolean = false,
    val redo: Boolean = false,
    val canDraw: Boolean = true
)

data class LayoutState(
    var strokeShown: Boolean = false,
    var toolShown: Boolean = false,
    var downloadShown: Boolean = false,
    var bgPickShown: Boolean = false
)

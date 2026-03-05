package io.agora.board.forge.ui.theme

import io.agora.board.forge.whiteboard.WhiteboardToolType

interface ForgeUiProvider {

    fun toolIcon(toolType: WhiteboardToolType): Int

    fun toolToast(toolType: WhiteboardToolType): Int

    fun defaultStrokeWidth(): Int = 4

    fun defaultStrokeColor(): Int
}

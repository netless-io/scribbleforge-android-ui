package io.agora.board.forge.ui.theme

import io.agora.board.forge.ui.ToolType

interface ForgeUiProvider {

    fun toolIcon(toolType: ToolType): Int

    fun toolToast(toolType: ToolType): Int

    fun toolVisible(toolType: ToolType): Boolean = true

    fun defaultStrokeWidth(): Int = 4

    fun defaultStrokeColor(): Int
}

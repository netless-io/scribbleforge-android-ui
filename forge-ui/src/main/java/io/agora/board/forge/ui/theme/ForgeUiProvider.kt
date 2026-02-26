package io.agora.board.forge.ui.theme

import io.agora.board.forge.ui.ForgeUiToolType

interface ForgeUiProvider {

    fun toolIcon(toolType: ForgeUiToolType): Int

    fun toolToast(toolType: ForgeUiToolType): Int

    fun defaultStrokeWidth(): Int = 4

    fun defaultStrokeColor(): Int
}

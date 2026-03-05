package io.agora.board.forge.ui.theme

import io.agora.board.forge.ui.model.ToolbarAction
import io.agora.board.forge.whiteboard.WhiteboardToolType

interface ForgeUiProvider {

    fun toolIcon(toolType: WhiteboardToolType): Int

    fun toolActionIcon(action: ToolbarAction): Int

    fun toolToast(toolType: WhiteboardToolType): Int
}

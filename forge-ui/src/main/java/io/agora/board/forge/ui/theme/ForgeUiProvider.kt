package io.agora.board.forge.ui.theme

import io.agora.board.forge.ui.model.ToolBoxAction
import io.agora.board.forge.whiteboard.WhiteboardToolType

interface ForgeUiProvider {

    fun toolIcon(toolType: WhiteboardToolType): Int

    fun toolActionIcon(action: ToolBoxAction): Int

    fun toolToast(toolType: WhiteboardToolType): Int
}

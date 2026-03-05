package io.agora.board.forge.ui.theme

import io.agora.board.forge.ui.model.ToolBoxAction
import io.agora.board.forge.whiteboard.WhiteboardToolType

interface ForgeUiProvider {

    fun toolIcon(toolType: WhiteboardToolType): Int

    fun toolToast(toolType: WhiteboardToolType): Int

    /** Action 类工具箱项的图标资源 */
    fun toolActionIcon(action: ToolBoxAction): Int

    fun defaultStrokeWidth(): Int = 4

    fun defaultStrokeColor(): Int
}

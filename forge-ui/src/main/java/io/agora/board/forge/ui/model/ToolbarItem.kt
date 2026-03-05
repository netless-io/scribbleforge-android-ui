package io.agora.board.forge.ui.model

import io.agora.board.forge.whiteboard.WhiteboardToolType

sealed class ToolbarItem {
    abstract var isSelected: Boolean

    data class Tool(
        val tools: List<WhiteboardToolType>,
        var index: Int = 0,
        override var isSelected: Boolean = false,
    ) : ToolbarItem()

    data class Action(
        val action: ToolbarAction,
        override var isSelected: Boolean = false,
    ) : ToolbarItem()
}

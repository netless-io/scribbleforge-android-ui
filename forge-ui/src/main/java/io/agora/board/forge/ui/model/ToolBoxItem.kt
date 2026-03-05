package io.agora.board.forge.ui.model

import io.agora.board.forge.whiteboard.WhiteboardToolType

sealed class ToolBoxItem {
    abstract var isSelected: Boolean

    data class Tool(
        val tools: List<WhiteboardToolType>,
        var index: Int = 0,
        override var isSelected: Boolean = false,
    ) : ToolBoxItem()

    data class Action(
        val action: ToolBoxAction,
        override var isSelected: Boolean = false,
    ) : ToolBoxItem()
}

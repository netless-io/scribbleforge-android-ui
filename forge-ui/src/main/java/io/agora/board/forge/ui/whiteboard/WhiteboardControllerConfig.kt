package io.agora.board.forge.ui.whiteboard

import io.agora.board.forge.whiteboard.WhiteboardOption

data class WhiteboardControllerConfig(
    val appId: String = "MainWhiteboard",
    val whiteboardOption: WhiteboardOption = defaultWhiteboardOption(),
) {
    companion object {
        fun defaultWhiteboardOption() = WhiteboardOption(
            width = 1920,
            height = 1080,
        )
    }
}

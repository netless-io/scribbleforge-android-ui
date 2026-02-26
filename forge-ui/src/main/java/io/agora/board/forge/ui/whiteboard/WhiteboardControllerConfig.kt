package io.agora.board.forge.ui.whiteboard

import io.agora.board.forge.whiteboard.WhiteboardOption

data class WhiteboardControllerConfig(
    val appId: String = "MainWhiteboard",
    val whiteboardOption: WhiteboardOption = defaultWhiteboardOption(),
    val whiteboardAspectRatio: Float = 16f / 9f,
)

private fun defaultWhiteboardOption() = WhiteboardOption(
    width = 1080,
    height = 1920,
)

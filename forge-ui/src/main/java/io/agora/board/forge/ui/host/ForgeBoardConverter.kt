package io.agora.board.forge.ui.host

import io.agora.board.forge.whiteboard.WhiteboardToolType
import io.agora.board.forge.ui.contract.model.ToolType

/**
 * Converter for transforming between SDK types and UI model types
 */
object ForgeBoardConverter {

    /**
     * Convert UI tool type to SDK whiteboard tool type
     */
    fun toWhiteboardToolType(type: ToolType): WhiteboardToolType =
        when (type) {
            ToolType.SELECTOR -> WhiteboardToolType.SELECTOR
            ToolType.LASER_POINTER -> WhiteboardToolType.LASER
            ToolType.ERASER -> WhiteboardToolType.ERASER
            ToolType.CURVE -> WhiteboardToolType.CURVE
            ToolType.STRAIGHT -> WhiteboardToolType.LINE
            ToolType.ARROW -> WhiteboardToolType.ARROW
            ToolType.RECTANGLE -> WhiteboardToolType.RECTANGLE
            ToolType.ELLIPSE -> WhiteboardToolType.ELLIPSE
            ToolType.TRIANGLE -> WhiteboardToolType.TRIANGLE
            ToolType.TEXT -> WhiteboardToolType.TEXT
            ToolType.HAND -> WhiteboardToolType.GRAB
            ToolType.CLICKER -> WhiteboardToolType.POINTER
            else -> WhiteboardToolType.CURVE
        }

    /**
     * Convert SDK whiteboard tool type to UI tool type
     */
    fun toToolType(type: WhiteboardToolType?): ToolType = when (type) {
        WhiteboardToolType.CURVE -> ToolType.CURVE
        WhiteboardToolType.RECTANGLE -> ToolType.RECTANGLE
        WhiteboardToolType.SELECTOR -> ToolType.SELECTOR
        WhiteboardToolType.LINE -> ToolType.STRAIGHT
        WhiteboardToolType.ARROW -> ToolType.ARROW
        WhiteboardToolType.TEXT -> ToolType.TEXT
        WhiteboardToolType.ELLIPSE -> ToolType.ELLIPSE
        WhiteboardToolType.TRIANGLE -> ToolType.TRIANGLE
        WhiteboardToolType.ERASER -> ToolType.ERASER
        WhiteboardToolType.LASER -> ToolType.LASER_POINTER
        WhiteboardToolType.GRAB -> ToolType.HAND
        WhiteboardToolType.POINTER -> ToolType.CLICKER
        else -> ToolType.CURVE
    }
}

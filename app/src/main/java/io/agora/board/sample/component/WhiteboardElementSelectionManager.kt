package io.agora.board.sample.component

import io.agora.board.forge.whiteboard.SimpleWhiteboardListener
import io.agora.board.forge.whiteboard.WhiteboardApplication
import io.agora.board.forge.whiteboard.WhiteboardElementSelection

/**
 * Manager class for handling whiteboard element selection events
 * and coordinating with UI components like ChangeElementAttributesLayout
 */
class WhiteboardElementSelectionManager(private val currentUserId: String) {
    
    private var whiteboardApp: WhiteboardApplication? = null
    private var attributesLayout: WhiteboardElementAttributesLayout? = null
    
    private val whiteboardListener = object : SimpleWhiteboardListener() {
        override fun onElementSelected(
            whiteboard: WhiteboardApplication,
            selection: WhiteboardElementSelection
        ) {
            // Only handle selections made by the current user
            if (selection.userId == currentUserId) {
                attributesLayout?.showElementAttributes(selection)
            }
        }

        override fun onElementUnselected(whiteboard: WhiteboardApplication) {
            attributesLayout?.hide()
        }
    }

    /**
     * Attach to a whiteboard application to start listening for selection events
     */
    fun attachWhiteboard(app: WhiteboardApplication) {
        detachWhiteboard() // Clean up any existing attachment
        
        this.whiteboardApp = app
        app.addListener(whiteboardListener)
        attributesLayout?.setWhiteboardApp(app)
    }

    /**
     * Detach from the current whiteboard application
     */
    fun detachWhiteboard() {
        whiteboardApp?.removeListener(whiteboardListener)
        whiteboardApp = null
        attributesLayout?.setWhiteboardApp(null)
    }

    /**
     * Set the attributes layout that should be updated when elements are selected
     */
    fun setAttributesLayout(layout: WhiteboardElementAttributesLayout?) {
        this.attributesLayout = layout
        layout?.setWhiteboardApp(whiteboardApp)
    }
}

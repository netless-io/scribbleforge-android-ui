package io.agora.board.forge.ui.contract

import io.agora.board.forge.ui.contract.model.AbstractObserver
import io.agora.board.forge.ui.contract.model.ForgeCallback
import io.agora.board.forge.ui.contract.model.RoomConnectionState

/**
 * Board room control abstract class for managing board join, leave, destroy operations
 */
abstract class BoardRoomControl : AbstractObserver<BoardRoomObserver>() {
    /**
     * Initialize method
     */
    abstract fun init()

    /**
     * Open board
     */
    abstract fun open()

    /**
     * Close board
     */
    abstract fun close()

    /**
     * Destroy board
     */
    abstract fun destroy()

    /**
     * Get board connection state
     * @return Board connection state
     */
    abstract fun getConnectionState(): RoomConnectionState

    /**
     * Set board background color
     */
    abstract fun setBoardBackground(
        color: String,
        callback: ForgeCallback<Void?>? = null
    )

    /**
     * Get board background color
     * @return Board background color
     */
    abstract fun getBoardBackgroundColor(): String

    /**
     * Get main window
     * @return Main window object
     */
    abstract fun getMainWindow(): BoardMainWindow?
}

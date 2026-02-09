package io.agora.board.forge.ui.contract

import io.agora.board.forge.ui.contract.model.Region
import io.agora.board.forge.ui.contract.model.RoomJoinConfig

/**
 * Board room interface for managing board lifecycle operations
 */
interface BoardRoom {

    /**
     * Initialize the board room
     * @param appId Application ID
     * @param region Board room region
     */
    fun init(appId: String, region: Region)

    /**
     * Join the board room
     * @param config Room join configuration
     * @param callback Join callback
     */
    fun join(
        config: RoomJoinConfig,
        callback: io.agora.board.forge.ui.contract.model.ForgeCallback<BoardMainWindow>
    )

    /**
     * Leave the board room
     */
    fun leave()

    /**
     * Destroy the board room
     */
    fun destroy()

    /**
     * Set board room listener
     * Note: Must be called before init
     * @param listener Board room listener
     */
    fun setBoardRoomListener(listener: BoardRoomListener)
}

package io.agora.board.forge.ui.internal.control

import io.agora.board.forge.ui.contract.BoardRoomListener
import io.agora.board.forge.ui.contract.model.RoomConnectionState

/**
 * Default implementation of BoardRoomListener interface
 */
open class BoardRoomListenerAdapter : BoardRoomListener {
    override fun onConnectionStateUpdated(state: RoomConnectionState) {
        // Default implementation does nothing
    }
}

package io.agora.board.forge.ui.contract

import io.agora.board.forge.ui.contract.model.RoomConnectionState

/**
 * Board state callback interface for listening to BoardRoom state changes
 */
interface BoardRoomListener {
    fun onConnectionStateUpdated(state: RoomConnectionState)
}

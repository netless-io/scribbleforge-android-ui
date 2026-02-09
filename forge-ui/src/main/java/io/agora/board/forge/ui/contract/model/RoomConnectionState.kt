package io.agora.board.forge.ui.contract.model

/**
 * Board room connection state
 */
enum class RoomConnectionState {
    /**
     * Connecting state
     */
    Connecting,

    /**
     * Connected state
     */
    Connected,

    /**
     * Reconnecting state
     */
    Reconnecting,

    /**
     * Disconnecting state
     */
    Disconnecting,

    /**
     * Disconnected state
     */
    Disconnected
}

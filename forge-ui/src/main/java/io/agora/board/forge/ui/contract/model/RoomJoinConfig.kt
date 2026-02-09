package io.agora.board.forge.ui.contract.model

/**
 * Configuration for joining a board room
 */
data class RoomJoinConfig(
    /**
     * Room ID
     */
    var roomId: String,

    /**
     * Room token
     */
    var roomToken: String,

    /**
     * Board content ratio, default 9/16
     */
    var boardRatio: Float = 9f / 16,

    /**
     * User ID, may be null
     */
    var userId: String? = null,

    /**
     * User name, may be null
     */
    var userName: String? = null,

    /**
     * Whether user has operation privilege, default false
     */
    var hasOperationPrivilege: Boolean = false
)

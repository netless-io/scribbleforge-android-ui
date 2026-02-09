package io.agora.board.forge.ui.contract.model

/**
 * Board activation information
 */
data class ActiveInfo(
    /**
     * Whether board is activated
     */
    val isActive: Boolean,

    /**
     * User ID who activated the board
     */
    val ownerUserId: String,

    /**
     * Reason for deactivation, only present when deactivating
     * 1. Manual close 2. Timeout close 3. Preempted
     */
    var reason: Int? = null
)

package io.agora.board.forge.ui.contract.model

/**
 * Board parameters information
 */
data class BoardParams(
    /**
     * Board application appId
     */
    val boardAppId: String,

    /**
     * Board room ID
     */
    val boardId: String,

    /**
     * Board room token
     */
    val boardToken: String,

    /**
     * Board region information
     */
    val boardRegion: Region,

    /**
     * Board ratio
     */
    val boardRatio: Float
)

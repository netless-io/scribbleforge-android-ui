package io.agora.board.forge.ui.contract.model

/**
 * Board page count state
 */
data class PageInfo(
    /**
     * Current displayed page index
     */
    val showIndex: Int,

    /**
     * Total page count
     */
    val count: Int
)

package io.agora.board.forge.ui.contract.model

/**
 * Board page information
 */
data class Page(
    /**
     * Page content height
     */
    val contentHeight: Double,

    /**
     * Page content width
     */
    val contentWidth: Double,

    /**
     * Page content URL
     */
    val contentUrl: String,

    /**
     * Page name
     */
    val name: String,

    /**
     * Page preview URL, may be null
     */
    val previewUrl: String?
)

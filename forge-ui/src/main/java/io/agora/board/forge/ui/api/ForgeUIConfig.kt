package io.agora.board.forge.ui.api

import io.agora.board.forge.ui.contract.model.Region

/**
 * Configuration class for Forge UI
 */
data class ForgeUIConfig(
    val appId: String,
    val roomId: String,
    val roomToken: String,
    val userId: String? = null,
    val userName: String? = null,
    val region: Region = Region.CN,
    val boardRatio: Float = 9f / 16,
    val theme: ForgeTheme = ForgeTheme.Default,
)

/**
 * Theme configuration for Forge UI
 */
sealed class ForgeTheme {
    object Default : ForgeTheme()
    data class Custom(
        val primaryColor: Int? = null,
        val backgroundColor: Int? = null
    ) : ForgeTheme()
}

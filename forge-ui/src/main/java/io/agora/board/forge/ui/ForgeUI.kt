package io.agora.board.forge.ui

import android.content.Context
import io.agora.board.forge.ui.api.ForgeUIConfig
import io.agora.board.forge.ui.api.ForgeUIController
import io.agora.rtm.RtmClient

object ForgeUI {
    const val VERSION = "0.1.0"

    /**
     * Attach the Forge UI to the application
     * @param context Application or activity context
     * @param config Configuration for the board UI
     * @param rtmClient RTM client for real-time communication
     * @return ForgeUIController instance for managing the board lifecycle
     */
    fun attach(
        context: Context,
        config: ForgeUIConfig,
        rtmClient: RtmClient? = null
    ): ForgeUIController {
        return ForgeUIController(context, config, rtmClient)
    }

    /**
     * Create a default configuration for Forge UI
     * @param appId The application ID
     * @param roomId The room ID
     * @param roomToken The room token
     * @param userId The user ID (optional)
     * @param userName The user name (optional)
     * @return Default ForgeUIConfig
     */
    fun createDefaultConfig(
        appId: String,
        roomId: String,
        roomToken: String,
        userId: String? = null,
        userName: String? = null
    ): ForgeUIConfig {
        return ForgeUIConfig(
            appId = appId,
            roomId = roomId,
            roomToken = roomToken,
            userId = userId,
            userName = userName,
            region = io.agora.board.forge.ui.contract.model.Region.CN,
            boardRatio = 9f / 16,
            theme = io.agora.board.forge.ui.api.ForgeTheme.Default,
        )
    }
}

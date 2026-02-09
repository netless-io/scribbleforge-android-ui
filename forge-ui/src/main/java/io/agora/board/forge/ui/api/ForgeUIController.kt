package io.agora.board.forge.ui.api

import android.content.Context
import io.agora.board.forge.ui.contract.BoardRoomControl
import io.agora.board.forge.ui.contract.model.RoomJoinConfig
import io.agora.board.forge.ui.host.ForgeBoardRoomHost
import io.agora.board.forge.ui.internal.control.BoardRoomControlImpl
import io.agora.rtm.RtmClient

/**
 * Main controller for Forge UI
 * Provides attach/detach methods for managing the board UI lifecycle
 */
class ForgeUIController(
    private val context: Context,
    private val config: ForgeUIConfig,
    private val rtmClient: RtmClient? = null
) {
    private var boardRoomControl: BoardRoomControl? = null

    /**
     * Initialize the board UI controller
     * Call this when you want to set up the board but not yet show it
     */
    fun init() {
        if (rtmClient == null) {
            return
        }

        val forgeBoardRoomHost = ForgeBoardRoomHost(context, rtmClient)

        val roomConfig = RoomJoinConfig(
            roomId = config.roomId,
            roomToken = config.roomToken,
            boardRatio = config.boardRatio,
            userId = config.userId,
            userName = config.userName
        )

        boardRoomControl = BoardRoomControlImpl(forgeBoardRoomHost, roomConfig)
        boardRoomControl?.init()
    }

    /**
     * Attach the board UI to the provided container
     * This will initialize and join the board room
     */
    fun attach(): BoardRoomControl? {
        if (rtmClient == null) {
            return boardRoomControl
        }

        if (boardRoomControl == null) {
            init()
        }

        boardRoomControl?.open()
        return boardRoomControl
    }

    /**
     * Detach the board UI
     * This will leave the board room but keep the controller for potential re-attachment
     */
    fun detach() {
        boardRoomControl?.close()
    }

    /**
     * Destroy the board UI controller
     * This will clean up all resources
     */
    fun destroy() {
        boardRoomControl?.destroy()
        boardRoomControl = null
    }

    /**
     * Get the board room control
     */
    fun getBoardRoomControl(): BoardRoomControl? {
        return boardRoomControl
    }
}

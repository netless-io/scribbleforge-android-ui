package io.agora.board.forge.ui.internal.control

import io.agora.board.forge.ui.contract.BoardMainWindow
import io.agora.board.forge.ui.contract.BoardRoom
import io.agora.board.forge.ui.contract.BoardRoomListener
import io.agora.board.forge.ui.contract.model.ForgeCallback
import io.agora.board.forge.ui.contract.model.ForgeError
import io.agora.board.forge.ui.contract.model.LogType
import io.agora.board.forge.ui.contract.model.Region
import io.agora.board.forge.ui.contract.model.RoomConnectionState
import io.agora.board.forge.ui.contract.model.RoomJoinConfig
import io.agora.board.forge.ui.internal.util.CoroutinesHelper
import io.agora.board.forge.ui.internal.util.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Board room control implementation for managing board join, leave, destroy operations
 */
class BoardRoomControlImpl(
    private var boardRoom: BoardRoom,
    private val roomConfig: RoomJoinConfig? = null
) : io.agora.board.forge.ui.contract.BoardRoomControl() {
    companion object {
        const val TAG = "BoardRoomControlImpl"
    }

    private lateinit var initJob: Job

    private var boardMainWindow: BoardMainWindow? = null
    private var connectionState: RoomConnectionState = RoomConnectionState.Disconnected

    private val boardRoomListener = object : BoardRoomListener {
        override fun onConnectionStateUpdated(state: RoomConnectionState) {
            updateConnectionState(state)
        }
    }

    override fun init() {
        initJob = CoroutinesHelper.getMainScope().launch {
            boardRoom.init("", Region.CN)
        }
    }

    override fun open() {
        CoroutinesHelper.getMainScope().launch {
            if (initJob.isActive) {
                Logger.i(TAG, "Waiting for initialization to complete")
                initJob.join()
            }

            val config = roomConfig ?: RoomJoinConfig(
                roomId = "roomId",  // This should be provided externally
                roomToken = "roomToken",  // This should be provided externally
                boardRatio = 1.0f,
                userId = "userId",
                userName = "userName"
            )

            boardRoom.setBoardRoomListener(boardRoomListener)

            boardRoom.join(config, object : ForgeCallback<BoardMainWindow> {
                override fun onSuccess(res: BoardMainWindow?) {
                    boardMainWindow = res
                    updateConnectionState(RoomConnectionState.Connected)
                }

                override fun onFailure(error: ForgeError) {
                    notifyError("Failed to join board room", error.message)
                    updateConnectionState(RoomConnectionState.Disconnected)
                }
            })
        }
    }

    override fun close() {
        boardRoom.leave()
    }

    override fun destroy() {
        boardRoom.destroy()
    }

    override fun setBoardBackground(color: String, callback: ForgeCallback<Void?>?) {
        callback?.onSuccess(null)
    }

    override fun getBoardBackgroundColor(): String {
        return ""
    }

    override fun getConnectionState(): RoomConnectionState {
        return connectionState
    }

    override fun getMainWindow(): BoardMainWindow? {
        return boardMainWindow
    }

    private fun updateConnectionState(state: RoomConnectionState) {
        if (connectionState == state) return

        // Only notify when boardMainWindow is updated and state is connected
        if (state == RoomConnectionState.Connected && boardMainWindow == null) {
            return
        }

        connectionState = state

        if (state == RoomConnectionState.Disconnected) {
            boardMainWindow = null
        }

        getObserver().forEach {
            it.onConnectionStateUpdated(state)
        }
    }

    private fun notifyError(message: String, extra: String? = null) {
        getObserver().forEach {
            it.onBoardLog(message, null, LogType.Error)
        }
    }
}

package io.agora.board.forge.ui.contract

import io.agora.board.forge.ui.contract.model.ActiveInfo
import io.agora.board.forge.ui.contract.model.LogType
import io.agora.board.forge.ui.contract.model.RoomConnectionState
import io.agora.board.forge.ui.contract.model.UserInfo

/**
 * Board room observer interface for listening to board room state changes
 */
interface BoardRoomObserver {
    /**
     * Called when board connection state is updated
     * @param state New board room connection state
     */
    fun onConnectionStateUpdated(state: RoomConnectionState)

    /**
     * Called when board activation state is updated
     * @param info Board activation information
     * @param operatorUser Operating user information
     */
    fun onBoardActiveInfoUpdated(
        info: ActiveInfo,
        operatorUser: UserInfo?
    )

    /**
     * Called when board background color is changed
     * @param color Background color
     * @param operatorUser Operating user
     */
    fun onBoardBackgroundColorUpdated(
        color: String,
        operatorUser: UserInfo?
    )

    /**
     * Called when board log is updated
     * @param log Log message
     * @param extra Extra log information, may be null
     * @param type Log type
     */
    fun onBoardLog(log: String, extra: String? = null, type: LogType)
}

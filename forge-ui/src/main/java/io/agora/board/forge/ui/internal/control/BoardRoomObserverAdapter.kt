package io.agora.board.forge.ui.internal.control

import io.agora.board.forge.ui.contract.BoardRoomObserver
import io.agora.board.forge.ui.contract.model.ActiveInfo
import io.agora.board.forge.ui.contract.model.LogType
import io.agora.board.forge.ui.contract.model.RoomConnectionState
import io.agora.board.forge.ui.contract.model.UserInfo

/**
 * Default implementation of BoardRoomObserver interface
 * Can be extended by other classes to override methods
 */
abstract class BoardRoomObserverAdapter : BoardRoomObserver {
    /**
     * Called when board connection state is updated
     * @param state New board room connection state
     */
    override fun onConnectionStateUpdated(state: RoomConnectionState) {}

    /**
     * Called when board activation state is updated
     * @param info Board activation information
     * @param operatorUser Operating user information
     */
    override fun onBoardActiveInfoUpdated(
        info: ActiveInfo,
        operatorUser: UserInfo?
    ) {
    }

    /**
     * Called when board background color is changed
     * @param color Background color
     * @param operatorUser Operating user
     */
    override fun onBoardBackgroundColorUpdated(
        color: String,
        operatorUser: UserInfo?
    ) {
    }

    /**
     * Called when board log is updated
     * @param log Log message
     * @param extra Extra log information, may be null
     * @param type Log type
     */
    override fun onBoardLog(log: String, extra: String?, type: LogType) {}
}

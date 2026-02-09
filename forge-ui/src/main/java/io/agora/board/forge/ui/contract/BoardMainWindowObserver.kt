package io.agora.board.forge.ui.contract

import io.agora.board.forge.ui.contract.model.LogType
import io.agora.board.forge.ui.contract.model.PageInfo

/**
 * Board main window observer interface for listening to main window events
 */
interface BoardMainWindowObserver {
    /**
     * Called when board page count is updated
     * @param pageInfo New page information object
     */
    fun onPageInfoUpdated(pageInfo: PageInfo)

    /**
     * Called when undo state is updated
     * @param enable True if undo is available, false otherwise
     */
    fun onUndoStateUpdated(enable: Boolean)

    /**
     * Called when redo state is updated
     * @param enable True if redo is available, false otherwise
     */
    fun onRedoStateUpdated(enable: Boolean)

    /**
     * Called when board log is updated
     * @param log Log message
     * @param extra Extra log information, may be null
     * @param type Log type
     */
    fun onBoardLog(log: String, extra: String? = null, type: LogType)
}

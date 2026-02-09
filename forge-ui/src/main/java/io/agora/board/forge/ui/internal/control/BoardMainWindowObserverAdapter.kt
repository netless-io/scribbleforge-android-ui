package io.agora.board.forge.ui.internal.control

import io.agora.board.forge.ui.contract.BoardMainWindowObserver
import io.agora.board.forge.ui.contract.model.LogType
import io.agora.board.forge.ui.contract.model.PageInfo

/**
 * Default implementation of BoardMainWindowObserver interface
 * Can be extended by other classes to override methods
 */
abstract class BoardMainWindowObserverAdapter : BoardMainWindowObserver {
    /**
     * Called when board page count is updated
     * @param pageInfo New page information object
     */
    override fun onPageInfoUpdated(pageInfo: PageInfo) {}

    /**
     * Called when undo state is updated
     * @param enable True if undo is available, false otherwise
     */
    override fun onUndoStateUpdated(enable: Boolean) {}

    /**
     * Called when redo state is updated
     * @param enable True if redo is available, false otherwise
     */
    override fun onRedoStateUpdated(enable: Boolean) {}

    /**
     * Called when board log is updated
     * @param log Log message
     * @param extra Extra log information, may be null
     * @param type Log type
     */
    override fun onBoardLog(log: String, extra: String?, type: LogType) {}
}

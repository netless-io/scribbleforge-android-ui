package io.agora.board.forge.ui.contract

import android.graphics.Bitmap
import android.view.View
import androidx.annotation.ColorInt
import io.agora.board.forge.ui.contract.model.ForgeCallback
import io.agora.board.forge.ui.contract.model.ForgeProgressCallback
import io.agora.board.forge.ui.contract.model.PageInfo
import io.agora.board.forge.ui.contract.model.ToolType
import io.agora.board.forge.whiteboard.WhiteboardApplication

/**
 * Board main window interface for controlling board operations
 */
interface BoardMainWindow {
    /**
     * Get the content view for displaying board content
     */
    fun getContentView(): View

    /**
     * Get whether current user has board operation privilege
     */
    fun getOperationPrivilege(): Boolean

    /**
     * Update board operation privilege
     */
    fun updateOperationPrivilege(
        hasPrivilege: Boolean,
        callback: ForgeCallback<Boolean>
    )

    /**
     * Get current page info (active index and total count)
     */
    fun getPageInfo(callback: ForgeCallback<PageInfo>)

    /**
     * Get snapshot images of all windows
     */
    fun getAllWindowsSnapshotImageList(callback: ForgeProgressCallback<Array<Bitmap>>)

    /**
     * Update board container size ratio
     */
    fun setContainerSizeRatio(ratio: Float)

    /**
     * Add a new page after current page
     */
    fun addPage()

    /**
     * Remove current page
     */
    fun removePage()

    /**
     * Switch to previous page
     */
    fun prevPage()

    /**
     * Switch to next page
     */
    fun nextPage()

    /**
     * Switch to page at specified index
     */
    fun setPageIndex(index: Int)

    /**
     * Undo last stroke
     */
    fun undo()

    /**
     * Redo last undone stroke
     */
    fun redo()

    /**
     * Clear board content
     */
    fun clean()

    /**
     * Insert image
     */
    fun insertImage(
        resourceUrl: String,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        callback: ForgeCallback<String>
    )

    /**
     * Get current tool type
     */
    fun getToolType(): ToolType

    /**
     * Set current tool type
     */
    fun setToolType(type: ToolType)

    /**
     * Get stroke width
     */
    fun getStokeWidth(): Int

    /**
     * Set stroke width
     */
    fun setStokeWidth(width: Int)

    /**
     * Get stroke color
     */
    fun getStokeColor(): Int

    /**
     * Set stroke color
     */
    fun setStokeColor(@ColorInt color: Int)

    /**
     * Set text size
     */
    fun setTextSize(size: Int)

    /**
     * Set text color
     */
    fun setTextColor(@ColorInt color: Int)

    /**
     * Set background color
     */
    fun setBackgroundColor(@ColorInt color: Int)

    /**
     * Add main window observer
     */
    fun addObserver(observer: BoardMainWindowObserver)

    /**
     * Remove main window observer
     */
    fun removeObserver(observer: BoardMainWindowObserver)

    fun getWhiteboardApp(): WhiteboardApplication
}

package io.agora.board.forge.ui.host

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.View
import io.agora.board.forge.RoomCallback
import io.agora.board.forge.RoomError
import io.agora.board.forge.whiteboard.SimpleWhiteboardListener
import io.agora.board.forge.whiteboard.WhiteboardApplication
import io.agora.board.forge.ui.contract.BoardMainWindow
import io.agora.board.forge.ui.contract.BoardMainWindowObserver
import io.agora.board.forge.ui.contract.model.ForgeCallback
import io.agora.board.forge.ui.contract.model.ForgeProgressCallback
import io.agora.board.forge.ui.contract.model.PageInfo
import io.agora.board.forge.ui.contract.model.ToolType
import io.agora.board.forge.ui.internal.util.CoroutinesHelper
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Host implementation of BoardMainWindow using Forge SDK
 * This is the ONLY layer that can reference the Forge SDK directly
 */
class ForgeBoardMainWindowHost(private val whiteboardApp: WhiteboardApplication) : BoardMainWindow {

    class EmptyRoomCallback<T> : RoomCallback<T> {
        override fun onSuccess(result: T) {
            Log.e("EmptyRoomCallback", "onSuccess")
        }

        override fun onFailure(error: RoomError) {
            Log.e("EmptyRoomCallback", "onFailure")
        }
    }

    override fun getContentView(): View {
        return whiteboardApp.getWhiteboardView()!!
    }

    override fun getOperationPrivilege(): Boolean {
        return true
    }

    override fun updateOperationPrivilege(
        hasPrivilege: Boolean,
        callback: ForgeCallback<Boolean>
    ) {
        callback.onSuccess(hasPrivilege)
    }

    override fun getPageInfo(callback: ForgeCallback<PageInfo>) {
        whiteboardApp.indexedNavigation.currentPageIndex(object : RoomCallback<Int> {
            override fun onSuccess(result: Int) {
                whiteboardApp.indexedNavigation.pageCount(object : RoomCallback<Int> {
                    override fun onSuccess(count: Int) {
                        callback.onSuccess(PageInfo(result, count))
                    }

                    override fun onFailure(error: RoomError) {
                        callback.onFailure(io.agora.board.forge.ui.contract.model.ForgeError(message = error.message))
                    }
                })
            }

            override fun onFailure(error: RoomError) {
                callback.onFailure(io.agora.board.forge.ui.contract.model.ForgeError(message = error.message))
            }
        })
    }

    override fun getAllWindowsSnapshotImageList(callback: ForgeProgressCallback<Array<Bitmap>>) {
        whiteboardApp.indexedNavigation.pageCount(object : RoomCallback<Int> {
            override fun onSuccess(count: Int) {
                CoroutinesHelper.getMainScope().launch {
                    val bitmaps = mutableListOf<Bitmap>()
                    for (index in 0 until count) {
                        val bitmap = getSceneSnapshotImage(index)
                        bitmaps.add(bitmap)
                        callback.onProgress((index + 1) * 100 / count)
                    }
                    callback.onSuccess(bitmaps.toTypedArray())
                }
            }

            override fun onFailure(error: RoomError) {
                callback.onFailure(io.agora.board.forge.ui.contract.model.ForgeError(message = error.message))
            }
        })
    }

    private suspend fun getSceneSnapshotImage(index: Int): Bitmap = suspendCoroutine { cont ->
        whiteboardApp.rasterize(index, object : RoomCallback<Bitmap> {
            override fun onSuccess(result: Bitmap) {
                cont.resume(result)
            }

            override fun onFailure(error: RoomError) {
                cont.resumeWithException(error)
            }
        })
    }

    override fun setContainerSizeRatio(ratio: Float) {
        TODO("Not yet implemented")
    }

    override fun addPage() {
        whiteboardApp.indexedNavigation.currentPageIndex(object : RoomCallback<Int> {
            override fun onSuccess(result: Int) {
                whiteboardApp.indexedNavigation.insertPage(result, object : RoomCallback<Int> {
                    override fun onSuccess(result: Int) {
                        Log.e("addPage", "onSuccess")
                    }

                    override fun onFailure(error: RoomError) {
                        Log.e("addPage", "onFailure")
                    }
                })
            }

            override fun onFailure(error: RoomError) {
                Log.e("currentPageIndex", "onFailure")
            }
        })
    }

    override fun removePage() {
        whiteboardApp.indexedNavigation.currentPageIndex(object : RoomCallback<Int> {
            override fun onSuccess(result: Int) {
                whiteboardApp.indexedNavigation.removePage(result, object : RoomCallback<Int> {
                    override fun onSuccess(result: Int) {
                        Log.e("removePage", "onSuccess")
                    }

                    override fun onFailure(error: RoomError) {
                        Log.e("removePage", "onFailure")
                    }
                })
            }

            override fun onFailure(error: RoomError) {
                Log.e("currentPageIndex", "onFailure")
            }
        })
    }

    override fun prevPage() {
        whiteboardApp.indexedNavigation.prevPage(EmptyRoomCallback())
    }

    override fun nextPage() {
        whiteboardApp.indexedNavigation.nextPage(object : RoomCallback<Int> {
            override fun onSuccess(result: Int) {
                Log.e("nextPage", "onSuccess")
            }

            override fun onFailure(error: RoomError) {
                Log.e("nextPage", "onFailure")
            }
        })
    }

    override fun setPageIndex(index: Int) {
        whiteboardApp.indexedNavigation.gotoPage(index, EmptyRoomCallback())
    }

    override fun undo() {
        whiteboardApp.undo()
    }

    override fun redo() {
        whiteboardApp.redo()
    }

    override fun clean() {
        whiteboardApp.clean()
    }

    override fun insertImage(
        resourceUrl: String,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        callback: ForgeCallback<String>
    ) {
        TODO("Not yet implemented")
    }

    override fun getToolType(): ToolType {
        return ForgeBoardConverter.toToolType(whiteboardApp.currentTool())
    }

    override fun setToolType(type: ToolType) {
        whiteboardApp.setCurrentTool(ForgeBoardConverter.toWhiteboardToolType(type))
    }

    override fun getStokeWidth(): Int {
        return whiteboardApp.strokeWidth().toInt()
    }

    override fun setStokeWidth(width: Int) {
        whiteboardApp.setStrokeWidth(width.toFloat())
    }

    override fun getStokeColor(): Int {
        return Color.parseColor(whiteboardApp.strokeColor())
    }

    override fun setStokeColor(color: Int) {
        whiteboardApp.setStrokeColor(color)
    }

    override fun setTextSize(size: Int) {
        whiteboardApp.setFontSize(size.toFloat())
    }

    override fun setTextColor(color: Int) {
        // TODO: Implement setTextColor
    }

    override fun setBackgroundColor(color: Int) {
        whiteboardApp.setBackgroundColor(color)
    }

    override fun addObserver(observer: BoardMainWindowObserver) {
        whiteboardApp.addListener(object : SimpleWhiteboardListener() {
            override fun onPageInfoUpdate(whiteboard: WhiteboardApplication, activePageIndex: Int, pageCount: Int) {
                observer.onPageInfoUpdated(PageInfo(activePageIndex, pageCount))
            }
        })
    }

    override fun removeObserver(observer: BoardMainWindowObserver) {
        // TODO: Implement observer removal
    }

    /**
     * Get the underlying WhiteboardApplication from SDK
     * This is exposed for advanced use cases but should be used carefully
     */
    override fun getWhiteboardApp(): WhiteboardApplication {
        return whiteboardApp
    }
}

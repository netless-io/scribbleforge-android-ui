package io.agora.board.forge.ui.whiteboard

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.agora.board.forge.RoomCallback
import io.agora.board.forge.RoomError
import io.agora.board.forge.ui.databinding.FcrBoardControlComponentBinding
import io.agora.board.forge.ui.internal.DeviceOrientation
import io.agora.board.forge.ui.internal.ForgeProgressCallback
import io.agora.board.forge.ui.R
import io.agora.board.forge.ui.common.ext.ForgeToast
import io.agora.board.forge.ui.whiteboard.state.WhiteboardStateStore
import io.agora.board.forge.ui.whiteboard.state.WhiteboardUiAction
import io.agora.board.forge.ui.whiteboard.state.WhiteboardUiState
import io.agora.board.forge.ui.internal.animateHide
import io.agora.board.forge.ui.internal.animateShow
import io.agora.board.forge.ui.internal.findForgeConfig
import io.agora.board.forge.ui.internal.BitmapUtils
import io.agora.board.forge.ui.whiteboard.component.FcrBoardBackgroundPanel
import io.agora.board.forge.ui.whiteboard.component.FcrBoardStrokePanel
import io.agora.board.forge.ui.whiteboard.component.FcrBoardShapePanel
import io.agora.board.forge.ui.whiteboard.component.FcrBoardToolbar
import io.agora.board.forge.ui.whiteboard.component.FcrBoardUiDownloadingState
import io.agora.board.forge.ui.model.ToolbarItem
import io.agora.board.forge.ui.model.ToolbarAction
import io.agora.board.forge.whiteboard.SimpleWhiteboardListener
import io.agora.board.forge.whiteboard.WhiteboardApplication
import io.agora.board.forge.whiteboard.WhiteboardToolInfo
import io.agora.board.forge.whiteboard.WhiteboardToolType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class WhiteboardControlLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {
    private val mainScope = CoroutineScope(Dispatchers.Main)

    private val binding = FcrBoardControlComponentBinding.inflate(LayoutInflater.from(context), this, true)

    private var downloadHideJob: Job? = null

    private var whiteboardApp: WhiteboardApplication? = null

    private val store = WhiteboardStateStore(context)

    private var layoutState = LayoutState()

    private var isDownloading = false

    init {
        mainScope.launch {
            store.state.collect { state ->
                render(state)
            }
        }

        setupToolBoxListener()
        setupStrokeSettingListener()
        setupShapePickListener()
        setupBgPickLayout()
        adjustLayoutForOrientation()
    }

    private fun render(state: WhiteboardUiState) {
        applyUiState(state)
        updateToolboxSelection(layoutState, state.toolType)
    }

    private fun applyLayoutState(block: LayoutState.() -> LayoutState) {
        layoutState = block(layoutState)
        updateFloatLayouts(layoutState)
        updateToolboxSelection(layoutState, store.state.value.toolType)
    }

    private fun setupToolBoxListener() {
        val toolBoxListener = object : FcrBoardToolbar.ToolBoxListener {
            override fun onToolBoxClick(item: ToolbarItem, position: Int) {
                when (item) {
                    is ToolbarItem.Tool -> onToolClick(item)
                    is ToolbarItem.Action -> when (item.action) {
                        ToolbarAction.Clear -> onClearClick()
                        ToolbarAction.Stroke -> onColorPickClick()
                        ToolbarAction.Undo -> onUndoClick()
                        ToolbarAction.Redo -> onRedoClick()
                        ToolbarAction.Download -> onDownloadClick()
                        ToolbarAction.Background -> onBgPickClick()
                    }
                }
            }
        }
        binding.phonePortLayout.boardToolBox.setToolBoxListener(toolBoxListener)
        binding.phoneLoadLayout.boardToolBox.setToolBoxListener(toolBoxListener)
        binding.tabletLayout.boardToolBox.setToolBoxListener(toolBoxListener)
    }

    private fun setupStrokeSettingListener() {
        val onStrokeSettingListener = object : FcrBoardStrokePanel.OnStrokeSettingListener {
            override fun onStrokeWidthClick(width: Int) {
                setStrokeWidth(width)
            }

            override fun onStrokeColorClick(color: Int) {
                setStrokeColor(color)
            }
        }
        binding.phonePortLayout.boardColorPickLayout.setOnStrokeSettingsListener(onStrokeSettingListener)
        binding.phoneLoadLayout.boardColorPickLayout.setOnStrokeSettingsListener(onStrokeSettingListener)
        binding.tabletLayout.boardColorPickLayout.setOnStrokeSettingsListener(onStrokeSettingListener)
    }

    private fun setupShapePickListener() {
        val shapePickListener = object : FcrBoardShapePanel.ShapePickListener {
            override fun onToolClick(toolType: WhiteboardToolType) {
                setToolType(toolType)
            }
        }
        binding.phonePortLayout.boardShapePickLayout.setShapePickListener(shapePickListener)
        binding.phoneLoadLayout.boardShapePickLayout.setShapePickListener(shapePickListener)
        binding.tabletLayout.boardShapePickLayout.setShapePickListener(shapePickListener)
    }

    private fun setupBgPickLayout() {
        val bgPickListener = object : FcrBoardBackgroundPanel.BoardBgPickListener {
            override fun onBoardBgPicked(color: Int, toast: Int) {
                syncBoardBackground(color) { success ->
                    if (success) {
                        ForgeToast.normal(context, toast)
                        setBoardBackgroundColor(color)
                    }
                }
                applyLayoutState { copy(bgPickShown = false) }
            }
        }
        listOf(
            binding.phoneLoadLayout.boardBgPickLayout,
            binding.phonePortLayout.boardBgPickLayout,
            binding.tabletLayout.boardBgPickLayout
        ).forEach { it.setBoardBgPickListener(bgPickListener) }
    }

    private val whiteboardListener = object : SimpleWhiteboardListener() {
        override fun onUndoStackLengthUpdate(whiteboard: WhiteboardApplication, length: Int) {
            store.dispatch(
                WhiteboardUiAction.UpdateUndoRedo(
                    undo = length > 0,
                    redo = store.state.value.redo
                )
            )
        }

        override fun onRedoStackLengthUpdate(whiteboard: WhiteboardApplication, length: Int) {
            store.dispatch(
                WhiteboardUiAction.UpdateUndoRedo(
                    undo = store.state.value.undo,
                    redo = length > 0
                )
            )
        }

        override fun onToolInfoUpdate(whiteboard: WhiteboardApplication, toolInfo: WhiteboardToolInfo) {
            store.dispatch(WhiteboardUiAction.ChangeTool(toolInfo.tool))
        }
    }

    fun bind(app: WhiteboardApplication) {
        whiteboardApp = app
        whiteboardApp?.addListener(whiteboardListener)
        syncBoardViewState()
    }

    fun unbind() {
        this.whiteboardApp?.removeListener(whiteboardListener)
        this.whiteboardApp = null
    }

    fun setWritable(writable: Boolean) {
        store.dispatch(WhiteboardUiAction.WritableChanged(writable))
    }

    private fun onClearClick() {
        whiteboardApp?.clean()
    }

    private fun onUndoClick() {
        whiteboardApp?.undo()
    }

    private fun onRedoClick() {
        whiteboardApp?.redo()
    }

    private fun onToolClick(item: ToolbarItem.Tool) {
        if (item.tools.size > 1) {
            val open = layoutState.toolShown
            if (open) {
                applyLayoutState { copy(toolShown = false, strokeShown = false, downloadShown = false, bgPickShown = false) }
            } else {
                applyLayoutState { copy(toolShown = true, strokeShown = false, downloadShown = false, bgPickShown = false) }
                setToolType(item.tools[item.index])
            }
            binding.phonePortLayout.boardShapePickLayout.setTools(item.tools)
            binding.phoneLoadLayout.boardShapePickLayout.setTools(item.tools)
            binding.tabletLayout.boardShapePickLayout.setTools(item.tools)
        } else {
            applyLayoutState { copy(toolShown = false, strokeShown = false, downloadShown = false, bgPickShown = false) }
            setToolType(item.tools[0])
        }
    }

    private fun onColorPickClick() {
        val current = layoutState.strokeShown
        if (!current) {
            showToolBoxToast(R.string.fcr_board_toast_change_color)
        }
        applyLayoutState { copy(strokeShown = !strokeShown, toolShown = false, downloadShown = false, bgPickShown = false) }
    }

    private fun onDownloadClick() {
        performDownload()
    }

    private fun performDownload() {
        if (!layoutState.downloadShown) {
            applyLayoutState { copy(downloadShown = true, toolShown = false, strokeShown = false, bgPickShown = false) }
        }

        if (isDownloading) return
        isDownloading = true
        setDownloadLayoutVisible(true)
        downloadHideJob?.cancel()

        val downloadLayout = when (DeviceOrientation.Companion.get(context)) {
            DeviceOrientation.PhonePortrait -> binding.phonePortLayout.boardSceneDownloadingLayout
            DeviceOrientation.PhoneLandscape -> binding.phoneLoadLayout.boardSceneDownloadingLayout
            DeviceOrientation.TabletPortrait, DeviceOrientation.TabletLandscape -> binding.tabletLayout.boardSceneDownloadingLayout
        }
        downloadLayout.setDownloadState(FcrBoardUiDownloadingState.DOWNLOADING)
        getAllWindowsSnapshotImageList(object : ForgeProgressCallback<Array<Bitmap>> {
            override fun onProgress(progress: Int) {
                downloadLayout.setProgress(progress)
            }

            override fun onSuccess(res: Array<Bitmap>) {
                downloadHideJob?.cancel()
                mainScope.launch {
                    try {
                        val bitmap = withContext(Dispatchers.IO) {
                            BitmapUtils.combineBitmapsVertically(res.toList())
                        }
                        if (bitmap != null) {
                            val imageName = "board_${System.currentTimeMillis()}"
                            val result = BitmapUtils.saveToGallery(context, bitmap, imageName)
                            if (result == BitmapUtils.SUCCESS) {
                                downloadLayout.setDownloadState(FcrBoardUiDownloadingState.SUCCESS)
                            } else {
                                downloadLayout.setDownloadState(FcrBoardUiDownloadingState.FAILURE)
                            }
                        } else {
                            downloadLayout.setDownloadState(FcrBoardUiDownloadingState.FAILURE)
                        }
                    } catch (e: Exception) {
                        downloadLayout.setDownloadState(FcrBoardUiDownloadingState.FAILURE)
                    } finally {
                        scheduleHideDownloadPanel()
                    }
                }
            }

            override fun onFailure(error: Exception) {
                downloadLayout.setDownloadState(FcrBoardUiDownloadingState.FAILURE)
                scheduleHideDownloadPanel()
            }
        })
    }

    private fun scheduleHideDownloadPanel() {
        downloadHideJob?.cancel()
        downloadHideJob = mainScope.launch {
            delay(1500)
            isDownloading = false
            applyLayoutState { copy(downloadShown = false) }
        }
    }

    private fun onBgPickClick() {
        val current = layoutState.bgPickShown
        if (!current) {
            showToolBoxToast(R.string.fcr_board_toast_change_bg)
        }
        applyLayoutState { copy(bgPickShown = !bgPickShown, toolShown = false, strokeShown = false, downloadShown = false) }
    }

    private fun syncBoardBackground(color: Int, onSync: (Boolean) -> Unit) {
        whiteboardApp?.setBackgroundColor(color)
        onSync(true)
    }

    private fun setBoardBackgroundColor(color: Int) {
        whiteboardApp?.setBackgroundColor(color)
        store.dispatch(WhiteboardUiAction.ChangeBackground(color))
        applyLayoutState { copy(bgPickShown = false) }
    }

    private fun setBgPickLayoutVisible(visible: Boolean) {
        binding.phonePortLayout.boardBgPickLayout.isVisible = visible
        binding.tabletLayout.boardBgPickLayout.isVisible = visible
        binding.phoneLoadLayout.boardBgPickLayout.run {
            if (visible) {
                animateShow(R.anim.fcr_board_slide_in_bottom_fade_in)
            } else {
                animateHide(R.anim.fcr_board_slide_out_bottom_fade_out)
            }
        }
    }

    private fun setDownloadLayoutVisible(visible: Boolean) {
        val layouts = listOf(
            binding.phonePortLayout.boardSceneDownloadingLayout,
            binding.phoneLoadLayout.boardSceneDownloadingLayout,
            binding.tabletLayout.boardSceneDownloadingLayout
        )
        layouts.forEach {
            if (visible) {
                it.animateShow(R.anim.fcr_board_slide_in_bottom_fade_in)
            } else {
                it.animateHide(R.anim.fcr_board_slide_out_bottom_fade_out)
            }
        }
    }

    private fun setUiEditMode(enable: Boolean) {
        if (this.isVisible == enable) return

        this.visibility = if (enable) VISIBLE else INVISIBLE

        if (enable) {
            val delayTime = resources.getInteger(R.integer.fcr_animator_duration_short).toLong()
            when (DeviceOrientation.Companion.get(context)) {
                DeviceOrientation.PhonePortrait -> {
                    binding.phonePortLayout.fcrBoardToolBoxWrapper.run {
                        startAnimation(AnimationUtils.loadAnimation(context, R.anim.fcr_board_slide_in_bottom_fade_in))
                    }
                    postDelayed({ binding.phonePortLayout.boardToolBox.animateGuide() }, delayTime)
                }

                DeviceOrientation.PhoneLandscape -> {
                    binding.phoneLoadLayout.fcrBoardToolBoxWrapper.run {
                        startAnimation(AnimationUtils.loadAnimation(context, R.anim.fcr_board_slide_in_right_fade_in))
                    }
                    postDelayed({ binding.phoneLoadLayout.boardToolBox.animateGuide() }, delayTime)
                }

                DeviceOrientation.TabletPortrait, DeviceOrientation.TabletLandscape -> {
                    binding.tabletLayout.fcrBoardToolBoxWrapper.run {
                        startAnimation(AnimationUtils.loadAnimation(context, R.anim.fcr_board_slide_in_bottom_fade_in))
                    }
                }
            }
        }

        if (enable) {
            ForgeToast.normal(context, R.string.fcr_board_toast_two_finger_move)
        }
    }

    private fun onLocalWritableChanged(writable: Boolean) {
        syncBoardViewState()
    }

    private fun setStrokeColor(color: Int) {
        whiteboardApp?.setStrokeColor(color)
        store.dispatch(WhiteboardUiAction.ChangeStrokeColor(color))
    }

    private fun setStrokeWidth(width: Int) {
        whiteboardApp?.setStrokeWidth(width.toFloat() * context.resources.displayMetrics.density)
        store.dispatch(WhiteboardUiAction.ChangeStrokeWidth(width))
    }

    private fun setToolType(toolType: WhiteboardToolType) {
        val current = store.state.value.toolType
        if (toolType != current) {
            showToolBoxToast(findForgeConfig().provider.toolToast(toolType))
        }

        whiteboardApp?.setCurrentTool(toolType)
        store.dispatch(WhiteboardUiAction.ChangeTool(toolType))
    }

    private fun showToolBoxToast(resId: Int) {
        ForgeToast.normal(context, resId)
    }

    /**
     * 将 WhiteboardUiState 应用到子视图（工具箱、颜色选择、形状选择、背景等）
     */
    private fun applyUiState(state: WhiteboardUiState) {
        binding.phonePortLayout.run {
            boardToolBox.setUiState(state)
            boardColorPickLayout.setUiState(state)
            boardShapePickLayout.selectTool(state.toolType)
            boardBgPickLayout.setBoardBackgroundColor(state.backgroundColor)
        }

        binding.phoneLoadLayout.run {
            boardToolBox.setUiState(state)
            boardColorPickLayout.setUiState(state)
            boardShapePickLayout.selectTool(state.toolType)
            boardBgPickLayout.setBoardBackgroundColor(state.backgroundColor)
        }

        binding.tabletLayout.run {
            boardToolBox.setUiState(state)
            boardColorPickLayout.setUiState(state)
            boardShapePickLayout.selectTool(state.toolType)
            boardBgPickLayout.setBoardBackgroundColor(state.backgroundColor)
        }

        if (state.canDraw) {
            setUiEditMode(true)
        } else {
            setUiEditMode(false)
        }
    }

    /**
     * 布局显示数据变更时更新浮动布局
     */
    private fun updateFloatLayouts(layoutState: LayoutState) {
        binding.phonePortLayout.run {
            boardShapePickLayout.isVisible = layoutState.toolShown
            boardColorPickLayout.isVisible = layoutState.strokeShown
        }

        binding.phoneLoadLayout.run {
            boardShapePickLayout.isVisible = layoutState.toolShown
            boardColorPickLayout.isVisible = layoutState.strokeShown
        }

        binding.tabletLayout.run {
            boardShapePickLayout.isVisible = layoutState.toolShown
            boardColorPickLayout.isVisible = layoutState.strokeShown
        }

        setDownloadLayoutVisible(layoutState.downloadShown)
        setBgPickLayoutVisible(layoutState.bgPickShown)
    }

    /**
     * 更新工具箱选中状态
     */
    private fun updateToolboxSelection(layoutState: LayoutState, currentTool: WhiteboardToolType) {
        listOf(
            binding.phonePortLayout.boardToolBox,
            binding.phoneLoadLayout.boardToolBox,
            binding.tabletLayout.boardToolBox
        ).forEach {
            it.setSelectionType(
                layoutState.strokeShown,
                layoutState.bgPickShown,
                layoutState.downloadShown,
                currentTool
            )
        }
    }

    private fun syncBoardViewState() {
        val s = store.state.value
        setStrokeColor(s.strokeColor)
        setStrokeWidth(s.strokeWidth)
        setToolType(s.toolType)
        whiteboardApp?.setFontSize(s.fontSize)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        adjustLayoutForOrientation()
    }

    private fun adjustLayoutForOrientation() {
        binding.tabletLayout.root.isVisible = false
        binding.phonePortLayout.root.isVisible = false
        binding.phoneLoadLayout.root.isVisible = false
        when (DeviceOrientation.Companion.get(context)) {
            DeviceOrientation.TabletPortrait, DeviceOrientation.TabletLandscape -> {
                binding.tabletLayout.root.isVisible = true
            }

            DeviceOrientation.PhonePortrait -> {
                binding.phonePortLayout.root.isVisible = true
            }

            DeviceOrientation.PhoneLandscape -> {
                binding.phoneLoadLayout.root.isVisible = true
            }
        }
    }

    private suspend fun getSceneSnapshotImage(index: Int): Bitmap = suspendCoroutine { cont ->
        whiteboardApp?.rasterize(index, object : RoomCallback<Bitmap> {
            override fun onSuccess(result: Bitmap) {
                cont.resume(result)
            }

            override fun onFailure(error: RoomError) {
                cont.resumeWithException(error)
            }
        })
    }

    private fun getAllWindowsSnapshotImageList(callback: ForgeProgressCallback<Array<Bitmap>>) {
        whiteboardApp?.indexedNavigation?.pageCount(object : RoomCallback<Int> {
            override fun onSuccess(count: Int) {
                mainScope.launch {
                    try {
                        val bitmaps = mutableListOf<Bitmap>()
                        for (index in 0 until count) {
                            val bitmap = getSceneSnapshotImage(index)
                            bitmaps.add(bitmap)
                            callback.onProgress((index + 1) * 100 / count)
                        }
                        callback.onSuccess(bitmaps.toTypedArray())
                    } catch (e: Exception) {
                        callback.onFailure(e)
                    }
                }
            }

            override fun onFailure(error: RoomError) {
                callback.onFailure(error)
            }
        })
    }

    data class LayoutState(
        var strokeShown: Boolean = false,
        var toolShown: Boolean = false,
        var downloadShown: Boolean = false,
        var bgPickShown: Boolean = false
    )
}

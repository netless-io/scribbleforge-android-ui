package io.agora.board.forge.ui.component

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
import io.agora.board.forge.ui.ForgeError
import io.agora.board.forge.ui.ForgeProgressCallback
import io.agora.board.forge.ui.R
import io.agora.board.forge.ui.component.state.DrawState
import io.agora.board.forge.ui.component.state.LayoutState
import io.agora.board.forge.ui.component.state.WhiteboardStateStore
import io.agora.board.forge.ui.component.state.WhiteboardUiAction
import io.agora.board.forge.ui.component.state.WhiteboardUiState
import io.agora.board.forge.ui.databinding.FcrBoardControlComponentBinding
import io.agora.board.forge.ui.internal.FcrDeviceOrientation
import io.agora.board.forge.ui.internal.animateHide
import io.agora.board.forge.ui.internal.animateShow
import io.agora.board.forge.ui.internal.findForgeConfig
import io.agora.board.forge.ui.ToolType
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
        updateDrawSettings(state.drawState)
        updateFloatLayouts(state.layoutState)
        updateToolboxSelection(state.layoutState)
    }

    private fun setupToolBoxListener() {
        val toolBoxListener = object : FcrBoardToolBoxLayout.ToolBoxListener {
            override fun onToolBoxClick(item: ToolBoxItem, position: Int) {
                when (item.type) {
                    FcrBoardToolBoxType.Tool -> onToolClick(item)
                    FcrBoardToolBoxType.Stroke -> onColorPickClick()
                    FcrBoardToolBoxType.Clear -> onClearClick()
                    FcrBoardToolBoxType.Undo -> onUndoClick()
                    FcrBoardToolBoxType.Redo -> onRedoClick()
                    FcrBoardToolBoxType.Download -> onDownloadClick()
                    FcrBoardToolBoxType.Background -> onBgPickClick()
                }
            }
        }
        binding.phonePortLayout.boardToolBox.setToolBoxListener(toolBoxListener)
        binding.phoneLoadLayout.boardToolBox.setToolBoxListener(toolBoxListener)
        binding.tabletLayout.boardToolBox.setToolBoxListener(toolBoxListener)

        binding.tabletLayout.flExitDraw.setOnClickListener {
            this@WhiteboardControlLayout.visibility = GONE
        }
    }

    private fun setupStrokeSettingListener() {
        val onStrokeSettingListener = object : FcrBoardColorPickLayout.OnStrokeSettingListener {
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
        val shapePickListener = object : FcrBoardShapePickLayout.ShapePickListener {
            override fun onToolClick(toolType: ToolType) {
                setToolType(toolType)
            }
        }
        binding.phonePortLayout.boardShapePickLayout.setShapePickListener(shapePickListener)
        binding.phoneLoadLayout.boardShapePickLayout.setShapePickListener(shapePickListener)
        binding.tabletLayout.boardShapePickLayout.setShapePickListener(shapePickListener)
    }

    private fun setupBgPickLayout() {
        val bgPickListener = object : FcrBoardBgPickLayout.BoardBgPickListener {
            override fun onBoardBgPicked(color: Int, toast: Int) {
                syncBoardBackground(color) { success ->
                    if (success) {
                        FcrToast.normal(context, toast)
                        setBoardBackgroundColor(color)
                    }
                }
                store.dispatch(WhiteboardUiAction.HideBgPanel)
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
                    redo = store.state.value.drawState.redo
                )
            )
        }

        override fun onRedoStackLengthUpdate(whiteboard: WhiteboardApplication, length: Int) {
            store.dispatch(
                WhiteboardUiAction.UpdateUndoRedo(
                    undo = store.state.value.drawState.undo,
                    redo = length > 0
                )
            )
        }

        override fun onToolInfoUpdate(whiteboard: WhiteboardApplication, toolInfo: WhiteboardToolInfo) {
            store.dispatch(WhiteboardUiAction.ChangeTool(toolInfo.tool.toToolType()))
        }
    }

    fun attachWhiteboard(app: WhiteboardApplication) {
        whiteboardApp = app
        whiteboardApp?.addListener(whiteboardListener)
        syncBoardViewState()
    }

    fun detachWhiteboard() {
        this.whiteboardApp?.removeListener(whiteboardListener)
        this.whiteboardApp = null
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

    private fun onToolClick(item: ToolBoxItem) {
        if (item.tools.size > 1) {
            val open = store.state.value.layoutState.toolShown
            if (open) {
                store.dispatch(WhiteboardUiAction.ToggleToolPanel)
            } else {
                store.dispatch(WhiteboardUiAction.ShowToolPanel)
                setToolType(item.tools[item.index])
            }
            binding.phonePortLayout.boardShapePickLayout.setTools(item.tools)
            binding.phoneLoadLayout.boardShapePickLayout.setTools(item.tools)
            binding.tabletLayout.boardShapePickLayout.setTools(item.tools)
        } else {
            store.dispatch(WhiteboardUiAction.HideAllPanel)
            setToolType(item.tools[0])
        }
    }

    private fun onColorPickClick() {
        val current = store.state.value.layoutState.strokeShown
        if (!current) {
            showToolBoxToast(R.string.fcr_board_toast_change_color)
        }

        store.dispatch(WhiteboardUiAction.ToggleStrokePanel)
    }

    private fun onDownloadClick() {
        performDownload()
    }

    private fun performDownload() {
        if (!store.state.value.layoutState.downloadShown) {
            store.dispatch(WhiteboardUiAction.ToggleDownloadPanel)
        }

        if (store.state.value.isDownloading) return
        store.dispatch(WhiteboardUiAction.StartDownloading)
        downloadHideJob?.cancel()

        val downloadLayout = when (FcrDeviceOrientation.Companion.get(context)) {
            FcrDeviceOrientation.PhonePortrait -> binding.phonePortLayout.boardSceneDownloadingLayout
            FcrDeviceOrientation.PhoneLandscape -> binding.phoneLoadLayout.boardSceneDownloadingLayout
            FcrDeviceOrientation.TabletPortrait, FcrDeviceOrientation.TabletLandscape -> binding.tabletLayout.boardSceneDownloadingLayout
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
                        bitmap?.let {
                            val imageName = "board_${System.currentTimeMillis()}"
                            val result = BitmapUtils.saveToGallery(context, it, imageName)
                            if (result == BitmapUtils.SUCCESS) {
                                downloadLayout.setDownloadState(FcrBoardUiDownloadingState.SUCCESS)
                                downloadHideJob = launch {
                                    delay(1500)
                                    store.dispatch(WhiteboardUiAction.HideDownloadPanel)
                                }
                            } else {
                                downloadLayout.setDownloadState(FcrBoardUiDownloadingState.FAILURE)
                            }
                        }
                    } catch (e: Exception) {
                        downloadLayout.setDownloadState(FcrBoardUiDownloadingState.FAILURE)
                    } finally {
                        store.dispatch(WhiteboardUiAction.FinishDownloading)
                    }
                }
            }

            override fun onFailure(error: ForgeError) {
                downloadLayout.setDownloadState(FcrBoardUiDownloadingState.FAILURE)
                store.dispatch(WhiteboardUiAction.FinishDownloading)
            }
        })
    }

    private fun onBgPickClick() {
        store.dispatch(WhiteboardUiAction.ToggleBgPanel)
        val current = store.state.value.layoutState.bgPickShown
        if (!current) {
            showToolBoxToast(R.string.fcr_board_toast_change_bg)
        }
    }

    private fun syncBoardBackground(color: Int, onSync: (Boolean) -> Unit) {
        whiteboardApp?.setBackgroundColor(color)
        onSync(true)
    }

    private fun setBoardBackgroundColor(color: Int) {
        whiteboardApp?.setBackgroundColor(color)
        store.dispatch(
            WhiteboardUiAction.ChangeBackground(color)
        )
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
        this.visibility = if (enable) VISIBLE else INVISIBLE

        if (enable) {
            val delayTime = resources.getInteger(R.integer.fcr_animator_duration_short).toLong()
            when (FcrDeviceOrientation.Companion.get(context)) {
                FcrDeviceOrientation.PhonePortrait -> {
                    binding.phonePortLayout.fcrBoardToolBoxWrapper.run {
                        startAnimation(AnimationUtils.loadAnimation(context, R.anim.fcr_board_slide_in_bottom_fade_in))
                    }
                    postDelayed({ binding.phonePortLayout.boardToolBox.animateGuide() }, delayTime)
                }

                FcrDeviceOrientation.PhoneLandscape -> {
                    binding.phoneLoadLayout.fcrBoardToolBoxWrapper.run {
                        startAnimation(AnimationUtils.loadAnimation(context, R.anim.fcr_board_slide_in_right_fade_in))
                    }
                    postDelayed({ binding.phoneLoadLayout.boardToolBox.animateGuide() }, delayTime)
                }

                FcrDeviceOrientation.TabletPortrait, FcrDeviceOrientation.TabletLandscape -> {
                    binding.tabletLayout.fcrBoardToolBoxWrapper.run {
                        startAnimation(AnimationUtils.loadAnimation(context, R.anim.fcr_board_slide_in_bottom_fade_in))
                    }
                }
            }
        }

        if (enable) {
            FcrToast.normal(context, R.string.fcr_board_toast_two_finger_move)
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
        whiteboardApp?.setStrokeWidth(width.toFloat())
        store.dispatch(WhiteboardUiAction.ChangeStrokeWidth(width))
    }

    private fun setToolType(toolType: ToolType) {
        val current = store.state.value.drawState.toolType
        if (toolType != current) {
            showToolBoxToast(findForgeConfig().provider.toolToast(toolType))
        }

        whiteboardApp?.setCurrentTool(toWhiteboardToolType(toolType))
        store.dispatch(WhiteboardUiAction.ChangeTool(toolType))
    }

    private fun showToolBoxToast(resId: Int) {
        FcrToast.normal(context, resId)
    }

    /**
     * 更新绘制设置
     */
    private fun updateDrawSettings(drawState: DrawState) {
        binding.phonePortLayout.run {
            boardToolBox.setDrawConfig(drawState)
            boardColorPickLayout.setDrawConfig(drawState)
            boardShapePickLayout.selectTool(drawState.toolType)
            boardBgPickLayout.setBoardBackgroundColor(drawState.backgroundColor)
        }

        binding.phoneLoadLayout.run {
            boardToolBox.setDrawConfig(drawState)
            boardColorPickLayout.setDrawConfig(drawState)
            boardShapePickLayout.selectTool(drawState.toolType)
            boardBgPickLayout.setBoardBackgroundColor(drawState.backgroundColor)
        }

        binding.tabletLayout.run {
            boardToolBox.setDrawConfig(drawState)
            boardColorPickLayout.setDrawConfig(drawState)
            boardShapePickLayout.selectTool(drawState.toolType)
            boardBgPickLayout.setBoardBackgroundColor(drawState.backgroundColor)
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
    private fun updateToolboxSelection(layoutState: LayoutState) {
        val type = when (true) {
            layoutState.strokeShown -> FcrBoardToolBoxType.Stroke
            layoutState.bgPickShown -> FcrBoardToolBoxType.Background
            layoutState.downloadShown -> FcrBoardToolBoxType.Download
            else -> FcrBoardToolBoxType.Tool
        }
        listOf(
            binding.phonePortLayout.boardToolBox,
            binding.phoneLoadLayout.boardToolBox,
            binding.tabletLayout.boardToolBox
        ).forEach { it.setSelectionType(type) }
    }

    private fun syncBoardViewState() {
        val current = store.state.value.drawState
        setStrokeColor(current.strokeColor)
        setStrokeWidth(current.strokeWidth)
        setToolType(current.toolType)
        whiteboardApp?.setFontSize(32f)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        adjustLayoutForOrientation()
    }

    private fun adjustLayoutForOrientation() {
        binding.tabletLayout.root.isVisible = false
        binding.phonePortLayout.root.isVisible = false
        binding.phoneLoadLayout.root.isVisible = false
        when (FcrDeviceOrientation.Companion.get(context)) {
            FcrDeviceOrientation.TabletPortrait, FcrDeviceOrientation.TabletLandscape -> {
                binding.tabletLayout.root.isVisible = true
            }

            FcrDeviceOrientation.PhonePortrait -> {
                binding.phonePortLayout.root.isVisible = true
            }

            FcrDeviceOrientation.PhoneLandscape -> {
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
                        callback.onFailure(ForgeError(message = e.message))
                    }
                }
            }

            override fun onFailure(error: RoomError) {
                callback.onFailure(ForgeError(message = error.message))
            }
        })
    }

    private fun toWhiteboardToolType(type: ToolType): WhiteboardToolType =
        when (type) {
            ToolType.SELECTOR -> WhiteboardToolType.SELECTOR
            ToolType.LASER_POINTER -> WhiteboardToolType.LASER
            ToolType.ERASER -> WhiteboardToolType.ERASER
            ToolType.CURVE -> WhiteboardToolType.CURVE
            ToolType.STRAIGHT -> WhiteboardToolType.LINE
            ToolType.ARROW -> WhiteboardToolType.ARROW
            ToolType.RECTANGLE -> WhiteboardToolType.RECTANGLE
            ToolType.ELLIPSE -> WhiteboardToolType.ELLIPSE
            ToolType.TRIANGLE -> WhiteboardToolType.TRIANGLE
            ToolType.TEXT -> WhiteboardToolType.TEXT
            ToolType.HAND -> WhiteboardToolType.GRAB
            ToolType.CLICKER -> WhiteboardToolType.POINTER
            else -> WhiteboardToolType.CURVE
        }

    fun WhiteboardToolType?.toToolType() = when (this) {
        WhiteboardToolType.CURVE -> ToolType.CURVE
        WhiteboardToolType.RECTANGLE -> ToolType.RECTANGLE
        WhiteboardToolType.SELECTOR -> ToolType.SELECTOR
        WhiteboardToolType.LINE -> ToolType.STRAIGHT
        WhiteboardToolType.ARROW -> ToolType.ARROW
        WhiteboardToolType.TEXT -> ToolType.TEXT
        WhiteboardToolType.ELLIPSE -> ToolType.ELLIPSE
        WhiteboardToolType.TRIANGLE -> ToolType.TRIANGLE
        WhiteboardToolType.ERASER -> ToolType.ERASER
        WhiteboardToolType.LASER -> ToolType.LASER_POINTER
        WhiteboardToolType.GRAB -> ToolType.HAND
        WhiteboardToolType.POINTER -> ToolType.CLICKER
        else -> ToolType.CURVE
    }
}

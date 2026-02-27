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
import io.agora.board.forge.ui.ForgeUiToolType
import io.agora.board.forge.ui.internal.ForgeProgressCallback
import io.agora.board.forge.ui.R
import io.agora.board.forge.ui.common.ext.ForgeToast
import io.agora.board.forge.ui.whiteboard.state.DrawState
import io.agora.board.forge.ui.whiteboard.state.LayoutState
import io.agora.board.forge.ui.whiteboard.state.WhiteboardStateStore
import io.agora.board.forge.ui.whiteboard.state.WhiteboardUiAction
import io.agora.board.forge.ui.whiteboard.state.WhiteboardUiState
import io.agora.board.forge.ui.databinding.FcrBoardControlComponentBinding
import io.agora.board.forge.ui.internal.DeviceOrientation
import io.agora.board.forge.ui.internal.animateHide
import io.agora.board.forge.ui.internal.animateShow
import io.agora.board.forge.ui.internal.findForgeConfig
import io.agora.board.forge.ui.internal.BitmapUtils
import io.agora.board.forge.ui.whiteboard.component.FcrBoardBgPickLayout
import io.agora.board.forge.ui.whiteboard.component.FcrBoardColorPickLayout
import io.agora.board.forge.ui.whiteboard.component.FcrBoardShapePickLayout
import io.agora.board.forge.ui.whiteboard.component.FcrBoardToolBoxLayout
import io.agora.board.forge.ui.whiteboard.component.FcrBoardToolBoxType
import io.agora.board.forge.ui.whiteboard.component.FcrBoardUiDownloadingState
import io.agora.board.forge.ui.whiteboard.component.ToolBoxItem
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
            override fun onToolClick(toolType: ForgeUiToolType) {
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
                        ForgeToast.normal(context, toast)
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

            override fun onFailure(error: Exception) {
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

    private fun setToolType(toolType: ForgeUiToolType) {
        val current = store.state.value.drawState.toolType
        if (toolType != current) {
            showToolBoxToast(findForgeConfig().provider.toolToast(toolType))
        }

        whiteboardApp?.setCurrentTool(toWhiteboardToolType(toolType))
        store.dispatch(WhiteboardUiAction.ChangeTool(toolType))
    }

    private fun showToolBoxToast(resId: Int) {
        ForgeToast.normal(context, resId)
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

        if (drawState.canDraw) {
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
        whiteboardApp?.setFontSize(current.fontSize)
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

    private fun toWhiteboardToolType(type: ForgeUiToolType): WhiteboardToolType =
        when (type) {
            ForgeUiToolType.SELECTOR -> WhiteboardToolType.SELECTOR
            ForgeUiToolType.LASER_POINTER -> WhiteboardToolType.LASER
            ForgeUiToolType.ERASER -> WhiteboardToolType.ERASER
            ForgeUiToolType.CURVE -> WhiteboardToolType.CURVE
            ForgeUiToolType.STRAIGHT -> WhiteboardToolType.LINE
            ForgeUiToolType.ARROW -> WhiteboardToolType.ARROW
            ForgeUiToolType.RECTANGLE -> WhiteboardToolType.RECTANGLE
            ForgeUiToolType.ELLIPSE -> WhiteboardToolType.ELLIPSE
            ForgeUiToolType.TRIANGLE -> WhiteboardToolType.TRIANGLE
            ForgeUiToolType.TEXT -> WhiteboardToolType.TEXT
            ForgeUiToolType.HAND -> WhiteboardToolType.GRAB
            ForgeUiToolType.CLICKER -> WhiteboardToolType.POINTER
            else -> WhiteboardToolType.CURVE
        }

    fun WhiteboardToolType?.toToolType() = when (this) {
        WhiteboardToolType.CURVE -> ForgeUiToolType.CURVE
        WhiteboardToolType.RECTANGLE -> ForgeUiToolType.RECTANGLE
        WhiteboardToolType.SELECTOR -> ForgeUiToolType.SELECTOR
        WhiteboardToolType.LINE -> ForgeUiToolType.STRAIGHT
        WhiteboardToolType.ARROW -> ForgeUiToolType.ARROW
        WhiteboardToolType.TEXT -> ForgeUiToolType.TEXT
        WhiteboardToolType.ELLIPSE -> ForgeUiToolType.ELLIPSE
        WhiteboardToolType.TRIANGLE -> ForgeUiToolType.TRIANGLE
        WhiteboardToolType.ERASER -> ForgeUiToolType.ERASER
        WhiteboardToolType.LASER -> ForgeUiToolType.LASER_POINTER
        WhiteboardToolType.GRAB -> ForgeUiToolType.HAND
        WhiteboardToolType.POINTER -> ForgeUiToolType.CLICKER
        else -> ForgeUiToolType.CURVE
    }
}

package io.agora.board.forge.ui.component

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.agora.board.forge.RoomCallback
import io.agora.board.forge.RoomError
import io.agora.board.forge.ui.R
import io.agora.board.forge.ui.model.model.ForgeError
import io.agora.board.forge.ui.model.model.ForgeProgressCallback
import io.agora.board.forge.ui.model.model.ToolType
import io.agora.board.forge.ui.databinding.FcrBoardControlComponentBinding
import io.agora.board.forge.whiteboard.SimpleWhiteboardListener
import io.agora.board.forge.whiteboard.WhiteboardApplication
import io.agora.board.forge.whiteboard.WhiteboardToolType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
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
    private var whiteboardApp: WhiteboardApplication? = null
    private var drawConfig = defaultDrawConfig(context)
    private var layoutShownData = FcrBoardUiLayoutShownData()

    private val isDownloading = AtomicBoolean(false)
    private var downloadHideJob: Job? = null

    init {
        setupToolBoxListener()
        setupStrokeSettingListener()
        setupShapePickListener()
        setupBgPickLayout()
        adjustLayoutForOrientation()
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
                        FcrCenterToast.normal(context, toast)
                        setBoardBackgroundColor(color)
                    }
                }
                layoutShownData.bgPickShown = false
                updateFloatLayouts()
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
            Log.d("WhiteboardControlLayout", "Undo stack length updated: $length")
            listOf(
                binding.phonePortLayout.boardToolBox,
                binding.phoneLoadLayout.boardToolBox,
                binding.tabletLayout.boardToolBox
            ).forEach { it.setUndoEnabled(length > 0) }
        }

        override fun onRedoStackLengthUpdate(whiteboard: WhiteboardApplication, length: Int) {
            Log.d("WhiteboardControlLayout", "Redo stack length updated: $length")
            listOf(
                binding.phonePortLayout.boardToolBox,
                binding.phoneLoadLayout.boardToolBox,
                binding.tabletLayout.boardToolBox
            ).forEach { it.setRedoEnabled(length > 0) }
        }
    }

    fun attachWhiteboard(app: WhiteboardApplication) {
        this.whiteboardApp = app
        syncBoardViewState()
        updateDrawSettings()
        app.addListener(whiteboardListener)
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
            if (layoutShownData.toolShown) {
                layoutShownData.toolShown = false
            } else {
                layoutShownData.run {
                    toolShown = true
                    strokeShown = false
                    downloadShown = false
                    bgPickShown = false
                }
                setToolType(item.tools[item.index])
            }
            binding.phonePortLayout.boardShapePickLayout.setTools(item.tools)
            binding.phoneLoadLayout.boardShapePickLayout.setTools(item.tools)
            binding.tabletLayout.boardShapePickLayout.setTools(item.tools)
        } else {
            layoutShownData.run {
                toolShown = false
                strokeShown = false
                downloadShown = false
                bgPickShown = false
            }
            setToolType(item.tools[0])
        }

        updateFloatLayouts()
    }

    private fun onColorPickClick() {
        if (layoutShownData.strokeShown) {
            layoutShownData.strokeShown = false
        } else {
            layoutShownData.run {
                toolShown = false
                strokeShown = true
                downloadShown = false
                bgPickShown = false
            }
            showToolBoxToast(R.string.fcr_board_toast_change_color)
        }

        updateFloatLayouts()
    }

    private fun onDownloadClick() {
        performDownload()
    }

    private fun performDownload() {
        if (!layoutShownData.downloadShown) {
            layoutShownData.run {
                toolShown = false
                strokeShown = false
                downloadShown = true
                bgPickShown = false
            }
            updateFloatLayouts()
        }

        if (isDownloading.get()) return
        isDownloading.set(true)
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
                            FcrBitmapUtils.combineBitmapsVertically(res.toList())
                        }
                        bitmap?.let {
                            val imageName = "board_${System.currentTimeMillis()}"
                            val result = FcrBitmapUtils.saveToGallery(context, it, imageName)
                            if (result == FcrBitmapUtils.SUCCESS) {
                                downloadLayout.setDownloadState(FcrBoardUiDownloadingState.SUCCESS)
                                downloadHideJob = launch {
                                    delay(1500)
                                    layoutShownData.run {
                                        toolShown = false
                                        strokeShown = false
                                        downloadShown = false
                                        bgPickShown = false
                                    }
                                    updateFloatLayouts()
                                }
                            } else {
                                downloadLayout.setDownloadState(FcrBoardUiDownloadingState.FAILURE)
                            }
                        }
                    } catch (e: Exception) {
                        downloadLayout.setDownloadState(FcrBoardUiDownloadingState.FAILURE)
                    } finally {
                        isDownloading.set(false)
                    }
                }
            }

            override fun onFailure(error: ForgeError) {
                downloadLayout.setDownloadState(FcrBoardUiDownloadingState.FAILURE)
                isDownloading.set(false)
            }
        })
    }

    private fun onBgPickClick() {
        if (layoutShownData.bgPickShown) {
            layoutShownData.bgPickShown = false
        } else {
            layoutShownData.run {
                bgPickShown = true
                toolShown = false
                strokeShown = false
                downloadShown = false
            }
            showToolBoxToast(R.string.fcr_board_toast_change_bg)
        }
        updateFloatLayouts()
    }

    private fun syncBoardBackground(color: Int, onSync: (Boolean) -> Unit) {
        whiteboardApp?.setBackgroundColor(color)
        onSync(true)
    }

    private fun setBoardBackgroundColor(color: Int) {
        whiteboardApp?.setBackgroundColor(color)
        drawConfig = drawConfig.copy(backgroundColor = color)
        updateDrawSettings()
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
            updateDrawSettings()
        }

        if (enable) {
            FcrCenterToast.normal(context, R.string.fcr_board_toast_two_finger_move)
        }
    }

    private fun onLocalWritableChanged(writable: Boolean) {
        syncBoardViewState()
        updateDrawSettings()
    }

    private fun setStrokeColor(color: Int) {
        whiteboardApp?.setStrokeColor(color)
        drawConfig = drawConfig.copy(strokeColor = color)
        updateDrawSettings()
    }

    private fun setStrokeWidth(width: Int) {
        whiteboardApp?.setStrokeWidth(width.toFloat())
        drawConfig = drawConfig.copy(strokeWidth = width)
        updateDrawSettings()
    }

    private fun setToolType(toolType: ToolType) {
        if (toolType != drawConfig.toolType) {
            showToolBoxToast(toolType.toastResId())
        }

        whiteboardApp?.setCurrentTool(toWhiteboardToolType(toolType))
        drawConfig = drawConfig.copy(toolType = toolType)
        updateDrawSettings()
    }

    private fun showToolBoxToast(resId: Int) {
        FcrCenterToast.normal(context, resId)
    }

    /**
     * 更新绘制设置
     */
    private fun updateDrawSettings() {
        binding.phonePortLayout.run {
            boardToolBox.setDrawConfig(drawConfig)
            boardColorPickLayout.setDrawConfig(drawConfig)
            boardShapePickLayout.selectTool(drawConfig.toolType)
            boardBgPickLayout.setBoardBackgroundColor(drawConfig.backgroundColor)
        }

        binding.phoneLoadLayout.run {
            boardToolBox.setDrawConfig(drawConfig)
            boardColorPickLayout.setDrawConfig(drawConfig)
            boardShapePickLayout.selectTool(drawConfig.toolType)
            boardBgPickLayout.setBoardBackgroundColor(drawConfig.backgroundColor)
        }

        binding.tabletLayout.run {
            boardToolBox.setDrawConfig(drawConfig)
            boardColorPickLayout.setDrawConfig(drawConfig)
            boardShapePickLayout.selectTool(drawConfig.toolType)
            boardBgPickLayout.setBoardBackgroundColor(drawConfig.backgroundColor)
        }

        updateToolboxSelection()
    }

    /**
     * 布局显示数据变更时更新浮动布局
     */
    private fun updateFloatLayouts() {
        binding.phonePortLayout.run {
            boardShapePickLayout.isVisible = layoutShownData.toolShown
            boardColorPickLayout.isVisible = layoutShownData.strokeShown
        }

        binding.phoneLoadLayout.run {
            boardShapePickLayout.isVisible = layoutShownData.toolShown
            boardColorPickLayout.isVisible = layoutShownData.strokeShown
        }

        binding.tabletLayout.run {
            boardShapePickLayout.isVisible = layoutShownData.toolShown
            boardColorPickLayout.isVisible = layoutShownData.strokeShown
        }

        setDownloadLayoutVisible(layoutShownData.downloadShown)
        setBgPickLayoutVisible(layoutShownData.bgPickShown)
        updateToolboxSelection()
    }

    private fun updateToolboxSelection() {
        val type = when (true) {
            layoutShownData.strokeShown -> FcrBoardToolBoxType.Stroke
            layoutShownData.bgPickShown -> FcrBoardToolBoxType.Background
            layoutShownData.downloadShown -> FcrBoardToolBoxType.Download
            else -> FcrBoardToolBoxType.Tool
        }
        listOf(
            binding.phonePortLayout.boardToolBox,
            binding.phoneLoadLayout.boardToolBox,
            binding.tabletLayout.boardToolBox
        ).forEach { it.setSelectionType(type) }
    }

    private fun syncBoardViewState() {
        whiteboardApp?.run {
            setStrokeColor(drawConfig.strokeColor)
            setStrokeWidth(drawConfig.strokeWidth.toFloat())
            setCurrentTool(toWhiteboardToolType(drawConfig.toolType))
            setFontSize(32f)
        }
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
}

package io.agora.board.sample.page.apaas

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import io.agora.board.forge.ui.R
import io.agora.board.forge.ui.component.FcrBitmapUtils
import io.agora.board.forge.ui.component.FcrBoardBgPickLayout
import io.agora.board.forge.ui.component.FcrBoardColorPickLayout
import io.agora.board.forge.ui.component.FcrBoardShapePickLayout
import io.agora.board.forge.ui.component.FcrBoardToolBoxLayout
import io.agora.board.forge.ui.component.FcrBoardToolBoxType
import io.agora.board.forge.ui.component.FcrBoardUiDownloadingState
import io.agora.board.forge.ui.component.FcrBoardUiLayoutShownData
import io.agora.board.forge.ui.component.FcrCenterToast
import io.agora.board.forge.ui.component.FcrDeviceOrientation
import io.agora.board.forge.ui.component.FcrPermissionsFragment
import io.agora.board.forge.ui.component.ToolBoxItem
import io.agora.board.forge.ui.component.animateHide
import io.agora.board.forge.ui.component.animateShow
import io.agora.board.forge.ui.component.defaultDrawConfig
import io.agora.board.forge.ui.component.toastResId
import io.agora.board.forge.ui.contract.BoardMainWindow
import io.agora.board.forge.ui.contract.model.ForgeCallback
import io.agora.board.forge.ui.contract.model.ForgeError
import io.agora.board.forge.ui.contract.model.ForgeProgressCallback
import io.agora.board.forge.ui.contract.model.RoomConnectionState
import io.agora.board.forge.ui.contract.model.ToolType
import io.agora.board.forge.ui.contract.model.UserInfo
import io.agora.board.forge.ui.databinding.FcrBoardControlComponentBinding
import io.agora.board.forge.ui.internal.control.BoardRoomObserverAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * author : fenglibin
 * date : 2024/5/27
 * description : 白板控制组件
 */
class FcrWhiteboardControlComponent : FcrBaseComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : super(context, attr)
    constructor(context: Context, attr: AttributeSet, defStyleAttr: Int) : super(context, attr, defStyleAttr)

    private val binding = FcrBoardControlComponentBinding.inflate(LayoutInflater.from(context), this, true)

    private var boardMainView: BoardMainWindow? = null
    private var drawConfig = defaultDrawConfig(context)
    private var layoutShownData = FcrBoardUiLayoutShownData()

    // 图片下载中标志
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
                    FcrBoardToolBoxType.Download -> onDownloadClick()
                    FcrBoardToolBoxType.Background -> onBgPickClick()
                    FcrBoardToolBoxType.Clear -> {
                        boardMainView?.getWhiteboardApp()?.clean()
                    }

                    FcrBoardToolBoxType.Undo -> {
                        boardMainView?.getWhiteboardApp()?.undo()
                    }

                    FcrBoardToolBoxType.Redo -> {
                        boardMainView?.getWhiteboardApp()?.redo()
                    }
                }
            }
        }
        binding.phonePortLayout.boardToolBox.setToolBoxListener(toolBoxListener)
        binding.phoneLoadLayout.boardToolBox.setToolBoxListener(toolBoxListener)
        binding.tabletLayout.boardToolBox.setToolBoxListener(toolBoxListener)

        binding.tabletLayout.flExitDraw.setOnClickListener {

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

    private val boardRoomObserver = object : BoardRoomObserverAdapter() {
        override fun onConnectionStateUpdated(state: RoomConnectionState) {
            if (state == RoomConnectionState.Connected) {
                setBoardView(boardRoomControl.getMainWindow())
                setBoardBackgroundColor(Color.parseColor("#ffe655"))
            }
        }

        override fun onBoardBackgroundColorUpdated(color: String, operatorUser: UserInfo?) {
            setBoardBackgroundColor(fromRGBString(color))
        }
    }

    override fun initView(provider: IUIProvider) {
        super.initView(provider)

        boardRoomControl.addObserver(boardRoomObserver)
        setBoardView(boardRoomControl.getMainWindow())
        setBoardBackgroundColor(
            fromRGBString(
                boardRoomControl.getBoardBackgroundColor()
            )
        )
    }

    override fun release() {
        boardRoomControl.removeObserver(boardRoomObserver)
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
        if (FcrPermissionsFragment.Companion.hasSaveImagePermissions(context)) {
            performDownload()
        } else {
            FcrPermissionsFragment.Companion.showSaveImagePermission(
                provider.getActivityPage(),
                onGranted = { performDownload() },
                onDenied = { FcrCenterToast.normal(context, R.string.fcr_board_toast_download_permission_denied) })
        }
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
        boardMainView?.getAllWindowsSnapshotImageList(object :
            ForgeProgressCallback<Array<Bitmap>> {
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
        boardRoomControl.setBoardBackground(toRGBString(color), object : ForgeCallback<Void?> {
            override fun onSuccess(res: Void?) {
                onSync(true)
            }

            override fun onFailure(error: ForgeError) {
                onSync(false)
            }
        })
    }

    private fun setBoardBackgroundColor(color: Int) {
        boardMainView?.setBackgroundColor(color)

        drawConfig = drawConfig.copy(backgroundColor = color)
        updateDrawSettings()
    }

    private fun fromRGBString(rgb: String?): Int {
        return runCatching {
            Color.parseColor(rgb)
        }.getOrElse {
            ContextCompat.getColor(context, R.color.fcr_whiteboard_bg_white)
        }
    }

    private fun toRGBString(@ColorInt color: Int): String {
        val r = String.format("%02x", Color.red(color))
        val g = String.format("%02x", Color.green(color))
        val b = String.format("%02x", Color.blue(color))
        return "#$r$g$b"
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
        boardMainView?.setStokeColor(color)

        drawConfig = drawConfig.copy(strokeColor = color)
        updateDrawSettings()
    }

    private fun setStrokeWidth(width: Int) {
        boardMainView?.setStokeWidth(width)

        drawConfig = drawConfig.copy(strokeWidth = width)
        updateDrawSettings()
    }

    private fun setToolType(toolType: ToolType) {
        if (toolType != drawConfig.toolType) {
            showToolBoxToast(toolType.toastResId())
        }

        boardMainView?.setToolType(toolType)
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

    private fun setBoardView(boardMainView: BoardMainWindow?) {
        this.boardMainView = boardMainView
        syncBoardViewState()
        updateDrawSettings()
    }

    private fun syncBoardViewState() {
        // FIXME: 2024/09/23
        boardMainView?.run {
            if (getOperationPrivilege()) {
                setStokeColor(drawConfig.strokeColor)
                setStokeWidth(drawConfig.strokeWidth)
                setToolType(drawConfig.toolType)
                setTextSize(32)
            }
            // setBackgroundColor(drawConfig.backgroundColor)
        } ?: run {
            Log.e("Room", "[INFO:CONSOLE] BoardMainView is null")
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
}

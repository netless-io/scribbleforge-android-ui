package io.agora.board.sample.page.app

import android.content.res.Configuration
import android.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
import io.agora.board.forge.Room
import io.agora.board.forge.RoomCallback
import io.agora.board.forge.RoomError
import io.agora.board.forge.common.app.RoomSyncState
import io.agora.board.forge.common.app.RoomSyncStateListener
import io.agora.board.forge.sample.databinding.ActivitySampleWhiteboardBinding
import io.agora.board.forge.whiteboard.WhiteboardApplication
import io.agora.board.sample.Constants
import io.agora.board.sample.component.ShapeConfig
import io.agora.board.sample.component.ShapeType
import io.agora.board.sample.util.JsonUtils

// 用于测试屏幕共享的辅助类
class ShareScreenTestHelper {

    // 模拟屏幕共享宽高状态, 用于测试
    data class SharingSyncState(val width: Int, val height: Int)

    lateinit var activity: SampleWhiteboardActivity
    lateinit var binding: ActivitySampleWhiteboardBinding

    private var whiteboardApp: WhiteboardApplication? = null
        get() = activity.whiteboardApp ?: field

    private var room: Room? = null
    private var syncState: RoomSyncState? = null

    private fun isSharing(): Boolean {
        return Constants.userId == "android_c4p2V9"
    }

    fun onCreate(activity: SampleWhiteboardActivity) {
        this.activity = activity
        this.binding = activity.binding

        var lastWidth = 0
        var lastHeight = 0

        binding.contentOuterLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val width = binding.contentOuterLayout.width
            val height = binding.contentOuterLayout.height

            if (width > 0 && height > 0 && (width != lastWidth || height != lastHeight) && whiteboardApp != null) {
                lastWidth = width
                lastHeight = height

                requestSyncState()
            }
        }

        binding.shapeDrawingView.shapeList = listOf(
            ShapeConfig(0.2f, 0.2f, ShapeType.CIRCLE, size = 0.1f, color = Color.RED),
            ShapeConfig(0.5f, 0.5f, ShapeType.RECTANGLE, size = 0.15f, color = Color.GREEN),
            ShapeConfig(0.8f, 0.8f, ShapeType.TRIANGLE, size = 0.2f, color = Color.BLUE)
        )
    }

    private fun stateForOrientation(orientation: Int): SharingSyncState {
        return if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            SharingSyncState(600, 1920)
        } else {
            SharingSyncState(1920, 600)
        }
    }

    private fun requestSyncState() {
        syncState?.getState(object : RoomCallback<String> {
            override fun onSuccess(state: String) {
                updateContentOuterLayoutRatio(parseSyncState(state))
            }

            override fun onFailure(error: RoomError) {}
        })
    }

    fun onJoinRoom(room: Room) {
        this.room = room
        this.syncState = RoomSyncState(room)

        if (isSharing()) {
            val state = stateForOrientation(activity.resources.configuration.orientation)
            syncState?.setState(toJson(state))
        }

        syncState?.addListener(object : RoomSyncStateListener {
            override fun onStateChanged(state: String) {
                updateContentOuterLayoutRatio(parseSyncState(state))
            }
        })

        requestSyncState()
    }

    fun onConfigurationChanged(newConfig: Configuration) {
        if (isSharing()) {
            val state = stateForOrientation(newConfig.orientation)
            syncState?.setState(toJson(state))
        }
    }

    private fun updateContentOuterLayoutRatio(state: SharingSyncState) {
        // 保持 contentOuterLayout 相对父布局 centerCrop 效果
        val contentOuterLayout = binding.contentOuterLayout
        val params = contentOuterLayout.layoutParams as? ConstraintLayout.LayoutParams ?: return
        params.dimensionRatio = "${state.width}:${state.height}"
        contentOuterLayout.layoutParams = params

        // 旋转时候互换宽高保持画笔粗细，此处假定初始化白板的宽高使用屏幕录制宽高
        whiteboardApp?.updateViewport(state.width, state.height)
    }

    private fun parseSyncState(json: String): SharingSyncState {
        return JsonUtils.fromJson(json, SharingSyncState::class.java)
    }

    private fun toJson(state: SharingSyncState): String {
        return JsonUtils.toJson(state)
    }
}

package io.agora.board.sample.page.apaas

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import io.agora.board.forge.RoomCallback
import io.agora.board.forge.RoomError
import io.agora.board.forge.sample.databinding.ActivityApaasSampleBinding
import io.agora.board.forge.sample.databinding.LayoutChangeViewModeBinding
import io.agora.board.forge.ui.ForgeUI
import io.agora.board.forge.ui.api.ForgeUIController
import io.agora.board.forge.ui.contract.BoardRoomControl
import io.agora.board.forge.ui.contract.model.ActiveInfo
import io.agora.board.forge.ui.contract.model.RoomConnectionState
import io.agora.board.forge.ui.contract.model.UserInfo
import io.agora.board.forge.ui.host.ForgeBoardMainWindowHost
import io.agora.board.sample.Constants
import io.agora.board.sample.component.WhiteboardElementSelectionManager
import io.agora.board.sample.util.PermissionHelper
import io.agora.board.sample.util.RtmHelper
import kotlinx.coroutines.runBlocking

/**
 * 使用灵动会议 UI 测试白板功能
 */
class ApaasSampleActivity : AppCompatActivity() {
    private var rtmHelper: RtmHelper = RtmHelper()
    private val binding: ActivityApaasSampleBinding by lazy { ActivityApaasSampleBinding.inflate(layoutInflater) }

    private var forgeUIController: ForgeUIController? = null
    private var selectionManager: WhiteboardElementSelectionManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()

        initView()

        runBlocking {
            try {
                initControl()
            } catch (e: Exception) {
                showToast("${e.message}")
            }
        }

        this.getBoardControl()?.addObserver(object : io.agora.board.forge.ui.contract.BoardRoomObserver {
            override fun onConnectionStateUpdated(state: RoomConnectionState) {
                showToast("Connection state changed: $state")
                when (state) {
                    RoomConnectionState.Connected -> {
                        binding.boardLayout.removeAllViews()
                        binding.boardLayout.run {
                            getBoardControl()?.getMainWindow()?.run {
                                addView(this.getContentView())
                                binding.pageIndicatorLayout.attachWhiteboard(getWhiteboardApp())
                            }
                        }

                        getBoardControl()?.getMainWindow()?.let { mainWindow ->
                            if (mainWindow is ForgeBoardMainWindowHost) {
                                val whiteboardApp = (mainWindow as ForgeBoardMainWindowHost).getWhiteboardApp()
                                whiteboardApp.let { app ->
                                    selectionManager?.attachWhiteboard(app)
                                }
                            }
                        }
                    }

                    RoomConnectionState.Disconnected -> {
                        binding.boardLayout.removeAllViews()
                        selectionManager?.detachWhiteboard()
                    }

                    else -> {}
                }
            }

            override fun onBoardActiveInfoUpdated(info: ActiveInfo, operatorUser: UserInfo?) {
                // Handle board active info updates
            }

            override fun onBoardBackgroundColorUpdated(color: String, operatorUser: UserInfo?) {
                // Handle background color updates
            }

            override fun onBoardLog(log: String, extra: String?, type: io.agora.board.forge.ui.contract.model.LogType) {
                // Handle board log messages
            }
        })

        // TODO: Re-enable after migrating WhiteboardControlComponent to forge-ui
        // binding.whiteboardControllerLayout.initView(this@ApaasSampleActivity)

        selectionManager = WhiteboardElementSelectionManager(Constants.currentUser.userId)
        selectionManager?.setAttributesLayout(binding.whiteboardElementAttributesLayout)
        this.getBoardControl()?.open()
    }

    private fun initView() {
        // 销毁白板，移除同步数据
        binding.destroyBoard.setOnClickListener {
            if (rtmHelper.isLogin()) {
                // Access appManager through the room
                getBoardRoom()?.appManager?.terminalApp("MainWhiteboard")
            }
        }

        binding.openBoard.setOnClickListener {
            if (rtmHelper.isLogin()) {
                this.getBoardControl()?.open()
            }
        }

        binding.closeBoard.setOnClickListener {
            if (rtmHelper.isLogin()) {
                this.getBoardControl()?.close()
            }
        }

        binding.viewMode.setOnClickListener {
            if (rtmHelper.isLogin()) {
                val room = getBoardRoom()
                val userIds = room?.userManager?.userIds()
                val selfId = Constants.currentUser.userId
                if (userIds?.isNotEmpty() == true) {
                    PermissionHelper.showUserListDialog(this, userIds.toList(), selfId) { userId ->
                        val whiteboardApp = getWhiteboardApp()
                        whiteboardApp?.getViewMode(userId, object : RoomCallback<String> {
                            override fun onSuccess(result: String) {
                                showViewModeDialog(this@ApaasSampleActivity, result, userId)
                            }

                            override fun onFailure(error: RoomError) {
                                showToast("getViewMode error: $error")
                            }
                        })
                    }
                    return@setOnClickListener
                }
            }
        }

        binding.permission.setOnClickListener {
            if (rtmHelper.isLogin()) {
                val room = getBoardRoom()
                if (room != null) {
                    val userIds = room.userManager.userIds()
                    val selfId = Constants.currentUser.userId
                    if (userIds.isNotEmpty()) {
                        PermissionHelper.showUserListDialog(this, userIds.toList(), selfId) { userId ->
                            val whiteboardApp = getWhiteboardApp()
                            if (whiteboardApp != null) {
                                PermissionHelper.showAppPermissionDialog(
                                    this, room, userId, whiteboardApp.appId, "WhiteboardApplication"
                                )
                            }
                        }
                        return@setOnClickListener
                    }
                }
            }
        }

        binding.tvUserId.text = Constants.currentUser.userId
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private suspend fun initControl() {
        rtmHelper.login()

        val config = ForgeUI.createDefaultConfig(
            appId = Constants.roomId, // Using roomId as appId placeholder
            roomId = Constants.roomId,
            roomToken = Constants.roomToken,
            userId = Constants.currentUser.userId,
            userName = Constants.currentUser.nickName
        )

        forgeUIController = ForgeUI.attach(this, config, rtmHelper.rtmClient())
        forgeUIController?.init()
    }

    fun getActivityPage(): FragmentActivity {
        return this
    }

    fun getBoardControl(): BoardRoomControl? {
        return forgeUIController?.getBoardRoomControl()
    }

    override fun onDestroy() {
        super.onDestroy()
        this.getBoardControl()?.destroy()
        forgeUIController?.destroy()
    }

    // Helper methods to access SDK functionality
    private fun getBoardRoom() = getBoardControl()?.getMainWindow()?.let { mainWindow ->
        if (mainWindow is ForgeBoardMainWindowHost) {
            (mainWindow as ForgeBoardMainWindowHost).getWhiteboardApp()?.room
        } else null
    }

    private fun getWhiteboardApp() = getBoardControl()?.getMainWindow()?.let { mainWindow ->
        if (mainWindow is ForgeBoardMainWindowHost) {
            (mainWindow as ForgeBoardMainWindowHost).getWhiteboardApp()
        } else null
    }

    fun showViewModeDialog(context: Context, viewModeOrFollowing: String, userId: String? = null) {
        var dialog: AlertDialog? = null

        val binding = LayoutChangeViewModeBinding.inflate(LayoutInflater.from(context))
        binding.lyFreeMode.setOnClickListener {
            val whiteboardApp = getWhiteboardApp()
            whiteboardApp?.setViewModeToFree(userId)
            dialog?.dismiss()
        }

        binding.lyMainMode.setOnClickListener {
            val whiteboardApp = getWhiteboardApp()
            whiteboardApp?.setViewModeToMain(userId)
            dialog?.dismiss()
        }

        // 设置选中项背景色
        val ly = when (viewModeOrFollowing) {
            "free" -> binding.lyFreeMode
            "main" -> binding.lyMainMode
            else -> binding.lyFollowMode
        }
        ly.setBackgroundColor(Color.GRAY)
        if (viewModeOrFollowing != "free" && viewModeOrFollowing != "main") {
            binding.followerInput.setText(viewModeOrFollowing)
        }

        binding.buttonPageChange.setOnClickListener {
            if (binding.inputPageNumber.text.isNotEmpty()) {
                val whiteboardApp = getWhiteboardApp()
                whiteboardApp?.setFreeModeUserPageIndex(binding.inputPageNumber.text.toString().toInt(), userId!!)
                dialog?.dismiss()
            }
        }

        dialog = AlertDialog.Builder(context).setTitle("设置${userId}的视角模式") // 显示用户或 follower
            .setView(binding.root).setPositiveButton("确定") { dialog, _ ->
                binding.followerInput.text.toString().let {
                    if (it.isNotEmpty()) {
                        val whiteboardApp = getWhiteboardApp()
                        whiteboardApp?.setViewModeToFollow(it, userId!!)
                    }
                }
                dialog.dismiss()
            }.setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }.create()
        dialog.show()
    }
}

package io.agora.board.sample.page

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import io.agora.board.forge.ApplicationListener
import io.agora.board.forge.Room
import io.agora.board.forge.RoomCallback
import io.agora.board.forge.RoomError
import io.agora.board.forge.RoomListener
import io.agora.board.forge.RoomOptions
import io.agora.board.forge.imagerydoc.ImageryDocApplication
import io.agora.board.forge.imagerydoc.ImageryDocOption
import io.agora.board.forge.rtm.RtmSocketProvider
import io.agora.board.forge.sample.databinding.ActivitySampleWindowBinding
import io.agora.board.forge.slide.SlideApplication
import io.agora.board.forge.slide.SlideOption
import io.agora.board.forge.whiteboard.WhiteboardApplication
import io.agora.board.forge.whiteboard.WhiteboardOption
import io.agora.board.forge.whiteboard.WhiteboardToolInfoOptions
import io.agora.board.forge.whiteboard.WhiteboardToolType
import io.agora.board.forge.windowmanager.WindowManager
import io.agora.board.forge.windowmanager.WindowManagerOption
import io.agora.board.sample.Constants
import io.agora.board.sample.ForgeTestConstants
import io.agora.board.sample.component.WhiteboardElementSelectionManager
import io.agora.board.sample.util.KvStore
import io.agora.board.sample.util.RtmHelper
import io.agora.board.sample.util.addFullView
import io.agora.board.sample.util.randomString
import io.agora.board.sample.util.toggleVisibility
import kotlinx.coroutines.launch

/**
 * 窗口管理器示例
 */
class SampleWindowActivity : BaseActivity<ActivitySampleWindowBinding>() {
    companion object {
        private const val MAIN_WHITEBOARD_ID = "MainWhiteboard"

        private const val SLIDE_ID = "SlideApp"
        private const val WHITEBOARD_ID = "Whiteboard"
        private const val IMAGERYDOC_ID = "ImageryDoc"
    }

    private var rtmHelper: RtmHelper = RtmHelper()

    private var room: Room? = null
    private var whiteboard: WhiteboardApplication? = null
    private var windowmanager: WindowManager? = null
    private var selectionManager: WhiteboardElementSelectionManager? = null

    private var joinRoomCallback = object : RoomCallback<Boolean> {
        override fun onSuccess(result: Boolean) {
            showToast("join room success")
            handleJoinRoomSuccess(room!!)
        }

        override fun onFailure(error: RoomError) {
            showToast("join room failure: ${error.message}")
        }
    }

    override fun inflateBinding(inflater: LayoutInflater) = ActivitySampleWindowBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.launchWhiteboard.setOnClickListener {
            room?.launchApp(
                WhiteboardApplication.TYPE,
                appId = MAIN_WHITEBOARD_ID,
                option = WhiteboardOption(
                    width = 1920,
                    height = 1080,
                    defaultToolbarStyle = WhiteboardToolInfoOptions(
                        tool = WhiteboardToolType.CURVE,
                    ),
                )
            )
        }

        binding.launchImageryDoc.setOnClickListener {
            val displayMode = if (KvStore.isDocContinuousMode()) {
                ImageryDocOption.DisplayMode.CONTINUOUS
            } else {
                ImageryDocOption.DisplayMode.SINGLE
            }
            val inheritWhiteboardId = if (KvStore.isDocInheritWhiteboardId()) MAIN_WHITEBOARD_ID else null

            windowmanager?.launchApp(
                ImageryDocApplication.TYPE,
                ImageryDocOption(
                    images = ForgeTestConstants.ImageryDocImages,
                    displayMode = displayMode,
                    inheritWhiteboardId = inheritWhiteboardId,
                ),
                appId = IMAGERYDOC_ID,
            )
        }

        binding.launchSlide.setOnClickListener {
            windowmanager?.launchApp(
                SlideApplication.TYPE,
                SlideOption(
                    prefix = "https://white-cover.oss-cn-hangzhou.aliyuncs.com/flat/dynamicConvert",
                    taskId = "46e8ff5db5714fec818f5594a6c55083",
                    inheritWhiteboardId = MAIN_WHITEBOARD_ID,
                ),
                appId = "${SLIDE_ID}_${randomString(6)}",
            )
        }

        binding.addApp.setOnClickListener {
            windowmanager?.launchApp(
                WhiteboardApplication.TYPE,
                appId = WHITEBOARD_ID,
                option = WhiteboardOption(
                    width = 1920,
                    height = 1080,
                    maxScaleRatio = 2f,
                    defaultToolbarStyle = WhiteboardToolInfoOptions(
                        tool = WhiteboardToolType.CURVE,
                    ),
                ),
            )
        }

        binding.listApps.setOnClickListener {
            binding.appListLayout.toggleVisibility()
        }

        binding.userPermissions.setOnClickListener {
            binding.userWritableLayout.toggleVisibility()
        }

        binding.settings.setOnClickListener {
            binding.windowSettingLayout.toggleVisibility()
        }

        binding.toggleWhiteboardControl.setOnClickListener {
            binding.whiteboardControlLayout.toggleVisibility()
        }

        binding.testButton1.setOnClickListener {
            room?.joinRoom(joinRoomCallback)
        }

        binding.testButton2.setOnClickListener {
            room?.leaveRoom()
        }

        @SuppressLint("SetTextI18n")
        binding.tvUserId.text = "id: ${Constants.currentUser.userId}"

        selectionManager = WhiteboardElementSelectionManager(Constants.currentUser.userId)
        selectionManager?.setAttributesLayout(binding.whiteboardElementAttributesLayout)

        lifecycleScope.launch {
            rtmHelper.login()
            rtmHelper.onMessageReceived = {
                room?.launchApp(
                    WhiteboardApplication.TYPE,
                    appId = MAIN_WHITEBOARD_ID,
                    option = WhiteboardOption(
                        width = 1920,
                        height = 1080,
                        defaultToolbarStyle = WhiteboardToolInfoOptions(
                            tool = WhiteboardToolType.CURVE,
                        ),
                    )
                )
            }
            joinRoom()
        }
    }

    private fun joinRoom() {
        val roomOptions = RoomOptions(
            context = applicationContext,
            roomId = Constants.roomId,
            roomToken = Constants.roomToken,
            userId = Constants.userId,
        ).apply {
            nickName(Constants.userId)
            socketProvider(RtmSocketProvider(rtmHelper.rtmClient()))
            region(Constants.BOARD_REGION)
            appIdentifier("appIdentifier")
            // enable window manager
            windowManagerOptions(WindowManagerOption(ratio = 16f / 9))
            writable(Constants.writable)
            timeout(5)
        }

        room = Room(roomOptions)
        room?.setAppExtraOptionsProvider { _, appId ->
            when (appId) {
                MAIN_WHITEBOARD_ID -> mapOf(WhiteboardApplication.KEY_TRANSPARENT to true)
                else -> null
            }
        }
        room?.addListener(object : RoomListener {
            override fun onError(room: Room, error: RoomError) {
                lifecycleScope.launch { showToast("[Room] error: ${error.message}") }
            }
        })
        room?.addAppListener(object : ApplicationListener {
            override fun onAppLaunch(appId: String) {
                when (appId) {
                    MAIN_WHITEBOARD_ID -> {
                        val whiteboardApp = room?.getApp(appId) as WhiteboardApplication
                        whiteboardApp.setBackgroundColor(Color.YELLOW)

                        whiteboard = whiteboardApp
                        binding.boardLayout.removeAllViews()
                        binding.boardLayout.addFullView(whiteboardApp.getView()!!)
                        binding.whiteboardControlLayout.attachWhiteboard(whiteboardApp)
                        binding.windowSettingLayout.attachWhiteboard(whiteboardApp)
                        binding.pageIndicatorLayout.attachWhiteboard(whiteboardApp)
                        selectionManager?.attachWhiteboard(whiteboardApp)
                    }

                    else -> {}
                }
            }

            override fun onAppTerminate(appId: String) {
                when (appId) {
                    MAIN_WHITEBOARD_ID -> {
                        whiteboard = null
                        binding.boardLayout.removeAllViews()
                        binding.windowSettingLayout.detachWhiteboard()
                        binding.pageIndicatorLayout.detachWhiteboard()
                        binding.whiteboardControlLayout.detachWhiteboard()
                        selectionManager?.detachWhiteboard()
                    }
                }
            }
        })
        room?.joinRoom(joinRoomCallback)
    }

    private fun handleJoinRoomSuccess(room: Room) {
        binding.appListLayout.attachRoom(room)
        binding.windowSettingLayout.attachRoom(room)
        binding.userWritableLayout.attachRoom(room)

        room.windowManager?.let { wm ->
            windowmanager = wm
            windowmanager?.addAppListener(object : ApplicationListener {
                override fun onAppLaunch(appId: String) {
                    if (windowmanager?.appManager?.getApp(appId)?.name == WhiteboardApplication.TYPE) {
                        val windowApp = windowmanager?.appManager?.getApp(appId) as WhiteboardApplication
                        windowApp.setBackgroundColor(Color.YELLOW)
                    }
                }

                override fun onAppTerminate(appId: String) {

                }
            })

            binding.windowLayout.removeAllViews()
            binding.windowLayout.addFullView(wm.getView())
            binding.windowSettingLayout.attachWindowManager(wm)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.userWritableLayout.detachRoom()
        room?.release()
    }
}

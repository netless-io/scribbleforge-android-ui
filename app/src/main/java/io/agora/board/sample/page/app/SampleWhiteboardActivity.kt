package io.agora.board.sample.page.app

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar.LayoutParams
import androidx.lifecycle.lifecycleScope
import io.agora.board.forge.ApplicationListener
import io.agora.board.forge.Room
import io.agora.board.forge.RoomCallback
import io.agora.board.forge.RoomError
import io.agora.board.forge.RoomOptions
import io.agora.board.forge.rtm.RtmSocketProvider
import io.agora.board.forge.sample.databinding.ActivitySampleWhiteboardBinding
import io.agora.board.forge.whiteboard.WhiteboardApplication
import io.agora.board.forge.whiteboard.WhiteboardOption
import io.agora.board.forge.whiteboard.WhiteboardToolInfoOptions
import io.agora.board.forge.whiteboard.WhiteboardToolType
import io.agora.board.sample.Constants
import io.agora.board.sample.page.BaseActivity
import io.agora.board.sample.util.RtmHelper
import io.agora.board.sample.util.toggleVisibility
import kotlinx.coroutines.launch

/**
 * whiteboardApp 示例
 */
class SampleWhiteboardActivity : BaseActivity() {
    companion object {
        private const val WHITEBOARD_APP_ID = "MainWhiteboard"
    }

    internal val binding by lazy { ActivitySampleWhiteboardBinding.inflate(layoutInflater) }
    internal var whiteboardApp: WhiteboardApplication? = null

    private var rtmHelper: RtmHelper = RtmHelper()
    private var shareScreenTestHelper = ShareScreenTestHelper()

    private var room: Room? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.launchWhiteboard.setOnClickListener {
            room?.launchApp(
                type = WhiteboardApplication.TYPE,
                appId = WHITEBOARD_APP_ID,
                option = WhiteboardOption(
                    width = 1080,
                    height = 1920,
                    maxScaleRatio = 1f,
                    defaultToolbarStyle = WhiteboardToolInfoOptions(
                        tool = WhiteboardToolType.CURVE,
                        fontSize = 12,
                    ),
                )
            )
        }

        binding.listApps.setOnClickListener {
            binding.appListLayout.toggleVisibility()
        }

        binding.settings.setOnClickListener {
            binding.windowSettingLayout.toggleVisibility()
        }

        binding.editWhiteboard.setOnClickListener {
            binding.whiteboardControlLayout.toggleVisibility()
        }

        lifecycleScope.launch {
            rtmHelper.login()
            joinRoom()
        }

        shareScreenTestHelper.onCreate(this)

        binding.extButton1.setOnClickListener {
            whiteboardApp?.clean()
            room?.leaveRoom()
        }

        binding.extButton2.setOnClickListener {
            joinRoom()
        }

        binding.extButton3.setOnClickListener {
            whiteboardApp?.clean()
        }
    }

    private fun joinRoom() {
        val roomOptions = RoomOptions(
            context = applicationContext,
            roomId = Constants.roomId,
            roomToken = Constants.roomToken,
            userId = Constants.userId
        ).apply {
            socketProvider(RtmSocketProvider(rtmHelper.rtmClient()))
            region(Constants.BOARD_REGION)
            appIdentifier("appIdentifier")
        }

        room = Room(roomOptions)
        room?.addAppListener(object : ApplicationListener {
            override fun onAppLaunch(appId: String) {
                when (appId) {
                    WHITEBOARD_APP_ID -> {
                        whiteboardApp = room?.getApp(appId) as? WhiteboardApplication
                        binding.contentLayout.removeAllViews()
                        binding.contentLayout.addView(
                            whiteboardApp?.getView(),
                            ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                        )

                        binding.whiteboardControlLayout.attachWhiteboard(whiteboardApp!!)
                        binding.windowSettingLayout.attachWhiteboard(whiteboardApp!!)

                        whiteboardApp?.setWhiteboardTransparent(true)
                        whiteboardApp?.setBackgroundColor(Color.parseColor("#2F00FFFF"))
                    }
                }
            }

            override fun onAppTerminate(appId: String) {
                when (appId) {
                    WHITEBOARD_APP_ID -> {
                        whiteboardApp = null
                        binding.contentLayout.removeAllViews()
                        binding.whiteboardControlLayout.detachWhiteboard()
                        binding.windowSettingLayout.detachWhiteboard()
                    }
                }
            }
        })

        room?.joinRoom(object : RoomCallback<Boolean> {
            override fun onSuccess(result: Boolean) {
                showToast("join room success")
                binding.appListLayout.attachRoom(room!!)

                shareScreenTestHelper.onJoinRoom(room!!)
            }

            override fun onFailure(error: RoomError) {
                showToast("join room failure: ${error.message}")
            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        shareScreenTestHelper.onConfigurationChanged(newConfig)
    }

    override fun onDestroy() {
        super.onDestroy()
        room?.leaveRoom()
    }
}

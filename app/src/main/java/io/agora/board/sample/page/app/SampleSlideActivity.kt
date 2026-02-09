package io.agora.board.sample.page.app

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import io.agora.board.forge.ApplicationManager
import io.agora.board.forge.Room
import io.agora.board.forge.RoomCallback
import io.agora.board.forge.RoomError
import io.agora.board.forge.RoomOptions
import io.agora.board.forge.rtm.RtmSocketProvider
import io.agora.board.forge.sample.databinding.ActivitySampleSlideBinding
import io.agora.board.forge.slide.SlideApplication
import io.agora.board.forge.slide.SlideOption
import io.agora.board.sample.Constants
import io.agora.board.sample.page.BaseActivity
import io.agora.board.sample.util.RtmHelper
import io.agora.board.sample.util.addFullView
import io.agora.board.sample.util.attachSlideDevView
import io.agora.board.sample.util.detachSlideDevView
import io.agora.board.sample.util.toggleVisibility
import kotlinx.coroutines.launch

/**
 * SlideApp 示例
 */
class SampleSlideActivity : BaseActivity() {
    companion object {
        const val MAIN_SLIDE_ID = "MainSlideApp"
        const val MAIN_WHITEBOARD_ID = "MainWhiteboard"
    }

    internal val binding: ActivitySampleSlideBinding by lazy {
        ActivitySampleSlideBinding.inflate(layoutInflater)
    }

    private var rtmHelper: RtmHelper = RtmHelper()
    private var room: Room? = null
    private var slideApp: SlideApplication? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()
        lockLandscape()

        binding.launchSlide.setOnClickListener {
            room?.appManager?.launchApp(
                SlideApplication.TYPE,
                SlideOption(
                    prefix = "https://white-cover.oss-cn-hangzhou.aliyuncs.com/flat/dynamicConvert",
                    taskId = "46e8ff5db5714fec818f5594a6c55083",
                    inheritWhiteboardId = MAIN_WHITEBOARD_ID
                ),
                appId = MAIN_SLIDE_ID
            )
        }

        binding.listApps.setOnClickListener {
            binding.appListLayout.toggleVisibility()
        }

        binding.settings.setOnClickListener {

        }

        lifecycleScope.launch {
            rtmHelper.login()
            joinRoom()
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
        room?.appManager?.addListener(object : ApplicationManager.Listener {
            override fun onAppLaunch(appId: String) {
                when (appId) {
                    MAIN_SLIDE_ID -> {
                        slideApp = room?.appManager?.getApp(appId) as? SlideApplication

                        binding.contentLayout.removeAllViews()
                        binding.contentLayout.addFullView(slideApp?.getView()!!)
                        this@SampleSlideActivity.attachSlideDevView(slideApp)

                        // api 示例
                        slideApp?.permissionManager?.canSwitchPage()
                        slideApp?.permissionManager?.setFullOperation()
                    }
                }
            }

            override fun onAppTerminate(appId: String) {
                when (appId) {
                    MAIN_SLIDE_ID -> {
                        binding.contentLayout.removeAllViews()
                        this@SampleSlideActivity.detachSlideDevView()
                        slideApp = null
                    }
                }
            }
        })

        room?.joinRoom(object : RoomCallback<Boolean> {
            override fun onSuccess(result: Boolean) {
                showToast("join room success")
                binding.appListLayout.attachRoom(room!!)
            }

            override fun onFailure(error: RoomError) {
                showToast("join room failure: ${error.message}")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        room?.leaveRoom()
    }
}

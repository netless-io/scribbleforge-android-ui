package io.agora.board.sample.page.app

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar.LayoutParams
import androidx.lifecycle.lifecycleScope
import io.agora.board.forge.ApplicationManager
import io.agora.board.forge.Room
import io.agora.board.forge.RoomCallback
import io.agora.board.forge.RoomError
import io.agora.board.forge.RoomOptions
import io.agora.board.forge.imagerydoc.ImageryDocApplication
import io.agora.board.forge.imagerydoc.ImageryDocOption
import io.agora.board.forge.rtm.RtmSocketProvider
import io.agora.board.forge.sample.databinding.ActivitySampleImageryDocBinding
import io.agora.board.sample.Constants
import io.agora.board.sample.ForgeTestConstants
import io.agora.board.sample.page.BaseActivity
import io.agora.board.sample.util.RtmHelper
import io.agora.board.sample.util.toggleVisibility
import kotlinx.coroutines.launch

/**
 * ImageryDoc 示例
 */
class SampleImageryDocActivity : BaseActivity() {
    companion object {
        const val MAIN_IMAGERY_DOC = "MainImageryDoc"
    }

    private val binding: ActivitySampleImageryDocBinding by lazy {
        ActivitySampleImageryDocBinding.inflate(layoutInflater)
    }

    private var rtmHelper: RtmHelper = RtmHelper()
    private var room: Room? = null
    private var imageryDocApplication: ImageryDocApplication? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.launchDoc.setOnClickListener {
            room?.appManager?.launchApp(
                ImageryDocApplication.TYPE,
                ImageryDocOption(
                    images = ForgeTestConstants.ImageryDocImages,
                    displayMode = ImageryDocOption.DisplayMode.CONTINUOUS,
                ),
                appId = MAIN_IMAGERY_DOC,
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
                    MAIN_IMAGERY_DOC -> {
                        imageryDocApplication = room?.appManager?.getApp(appId) as? ImageryDocApplication
                        binding.contentLayout.removeAllViews()
                        binding.contentLayout.addView(
                            imageryDocApplication?.getView(),
                            ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                        )
                    }
                }
            }

            override fun onAppTerminate(appId: String) {
                when (appId) {
                    MAIN_IMAGERY_DOC -> {
                        binding.contentLayout.removeAllViews()
                        imageryDocApplication = null
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

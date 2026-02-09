package io.agora.board.sample.page.app

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar.LayoutParams
import io.agora.board.forge.ApplicationListener
import io.agora.board.forge.Room
import io.agora.board.forge.RoomOptions
import io.agora.board.forge.common.dev.FakeSocketProvider
import io.agora.board.forge.sample.databinding.ActivityWhiteboardUiBinding
import io.agora.board.forge.whiteboard.WhiteboardApplication
import io.agora.board.forge.whiteboard.WhiteboardOption
import io.agora.board.forge.whiteboard.WhiteboardToolInfoOptions
import io.agora.board.forge.whiteboard.WhiteboardToolType
import io.agora.board.sample.Constants
import io.agora.board.sample.page.BaseActivity

/**
 * whiteboard ui activity
 */
class WhiteboardUIActivity : BaseActivity() {
    companion object {
        private const val WHITEBOARD_APP_ID = "MainWhiteboard"
    }

    internal val binding by lazy { ActivityWhiteboardUiBinding.inflate(layoutInflater) }
    internal var whiteboardApp: WhiteboardApplication? = null
    private lateinit var room: Room

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.launchWhiteboard.setOnClickListener {
            room.launchApp(
                type = WhiteboardApplication.TYPE, appId = WHITEBOARD_APP_ID, option = WhiteboardOption(
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

        joinRoom()
    }

    private fun joinRoom() {
        val roomOptions = RoomOptions(
            context = this,
            roomId = Constants.roomId,
            roomToken = Constants.roomToken,
            userId = Constants.userId,
        ).apply {
            writable(true)
            socketProvider(FakeSocketProvider())
            region(Constants.BOARD_REGION)
            appIdentifier("123/123")
        }

        room = Room(roomOptions)
        room.addAppListener(object : ApplicationListener {
            override fun onAppLaunch(appId: String) {
                when (appId) {
                    WHITEBOARD_APP_ID -> {
                        whiteboardApp = room.getApp(appId) as? WhiteboardApplication
                        binding.contentLayout.removeAllViews()
                        binding.contentLayout.addView(
                            whiteboardApp?.getView(),
                            ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                        )

                        binding.whiteboardControlLayout.attachWhiteboard(whiteboardApp!!)
                    }
                }
            }

            override fun onAppTerminate(appId: String) {
                when (appId) {
                    WHITEBOARD_APP_ID -> {
                        whiteboardApp = null
                        binding.contentLayout.removeAllViews()
                        binding.whiteboardControlLayout.detachWhiteboard()
                    }
                }
            }
        })

        room.joinRoom()
    }

    override fun onDestroy() {
        super.onDestroy()
        room.leaveRoom()
    }
}

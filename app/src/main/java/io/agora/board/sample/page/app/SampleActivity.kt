package io.agora.board.sample.page.app

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.ActionBar.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.agora.board.forge.ApplicationManager
import io.agora.board.forge.Room
import io.agora.board.forge.RoomCallback
import io.agora.board.forge.RoomError
import io.agora.board.forge.RoomOptions
import io.agora.board.forge.rtm.RtmSocketProvider
import io.agora.board.forge.sample.databinding.ActivitySampleBinding
import io.agora.board.forge.whiteboard.WhiteboardApplication
import io.agora.board.forge.whiteboard.WhiteboardOption
import io.agora.board.forge.whiteboard.WhiteboardToolType
import io.agora.board.forge.whiteboard.launchWhiteboard
import io.agora.board.sample.Constants
import io.agora.board.sample.component.EmptyRoomCallback
import io.agora.board.sample.util.RtmHelper
import kotlinx.coroutines.launch

class SampleActivity : AppCompatActivity() {
    companion object {
        private const val WHITEBOARD_APP_ID = "MainWhiteboard"
    }

    private val binding: ActivitySampleBinding by lazy { ActivitySampleBinding.inflate(layoutInflater) }
    private var rtmHelper: RtmHelper = RtmHelper()

    private var room: Room? = null
    private var whiteboardApp: WhiteboardApplication? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.joinRoom.setOnClickListener {
            if (rtmHelper.isLogin()) {
                joinRoom()
            }
        }

        binding.launchWhiteboard.setOnClickListener {
            room?.launchWhiteboard(
                option = WhiteboardOption(width = 1920, height = 1080),
                appId = WHITEBOARD_APP_ID,
                callback = EmptyRoomCallback(),
            )
        }

        binding.strokeColor.setOnClickListener(object : OnClickListener {
            var index = 1
            val colors = listOf(
                Color.RED,
                Color.GREEN,
                Color.BLUE,
                Color.YELLOW,
                Color.CYAN,
                Color.MAGENTA,
            )

            override fun onClick(v: View?) {
                whiteboardApp?.setStrokeColor(colors[index])
                index++
                if (index >= colors.size) {
                    index = 0
                }
            }
        })

        binding.curve.setOnClickListener { whiteboardApp?.setCurrentTool(WhiteboardToolType.CURVE) }

        binding.eraser.setOnClickListener { whiteboardApp?.setCurrentTool(WhiteboardToolType.ERASER) }

        binding.selector.setOnClickListener { whiteboardApp?.setCurrentTool(WhiteboardToolType.SELECTOR) }

        binding.background.setOnClickListener {
            whiteboardApp?.setWhiteboardTransparent(true)
            whiteboardApp?.setBackgroundColor(Color.TRANSPARENT)
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
                handleAppLaunch(appId)
            }

            override fun onAppTerminate(appId: String) {
                handleAppTerminate(appId)
            }
        })

        room?.joinRoom(object : RoomCallback<Boolean> {
            override fun onSuccess(result: Boolean) {
                showToast("join room success")
            }

            override fun onFailure(error: RoomError) {
                showToast("join room failure: ${error.message}")
            }
        })

        room?.leaveRoom()
    }

    private fun handleAppTerminate(appId: String) {
        if (appId != WHITEBOARD_APP_ID) {
            return
        }
        whiteboardApp = null
        binding.boardLayout.removeAllViews()
    }

    private fun handleAppLaunch(appId: String) {
        if (appId != WHITEBOARD_APP_ID) {
            return
        }
        whiteboardApp = (room?.appManager?.getApp(appId) as? WhiteboardApplication)?.also {
            binding.boardLayout.removeAllViews()
            binding.boardLayout.addView(
                it.getWhiteboardView(),
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            )
            Log.e("SampleActivity", "handleAppLaunch run: ${it.getWhiteboardView()}")
        }
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        room?.leaveRoom()
    }
}

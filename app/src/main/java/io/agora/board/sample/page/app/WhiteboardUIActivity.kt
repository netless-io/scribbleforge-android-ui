package io.agora.board.sample.page.app

import android.os.Bundle
import io.agora.board.forge.Room
import io.agora.board.forge.RoomOptions
import io.agora.board.forge.common.dev.FakeSocketProvider
import io.agora.board.forge.sample.databinding.ActivityWhiteboardUiBinding
import io.agora.board.forge.ui.api.WhiteboardController
import io.agora.board.forge.ui.api.WhiteboardControllerConfig
import io.agora.board.sample.Constants
import io.agora.board.sample.page.BaseActivity

/**
 * whiteboard ui activity
 */
class WhiteboardUIActivity : BaseActivity() {
    internal val binding by lazy { ActivityWhiteboardUiBinding.inflate(layoutInflater) }
    private lateinit var whiteboardController: WhiteboardController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()

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

        val room = Room(roomOptions)

        whiteboardController = WhiteboardController(
            container = binding.whiteboardContainer,
            config = WhiteboardControllerConfig(
                appId = "MainWhiteboard",
            )
        )

        whiteboardController.start(room)
    }

    override fun onDestroy() {
        super.onDestroy()
        whiteboardController.stop()
    }
}

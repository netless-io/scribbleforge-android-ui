package io.agora.board.ui.sample.page

import android.os.Bundle
import android.view.LayoutInflater
import io.agora.board.forge.Room
import io.agora.board.forge.RoomOptions
import io.agora.board.forge.common.dev.FakeSocketProvider
import io.agora.board.forge.ui.sample.databinding.ActivityWhiteboardUiBinding
import io.agora.board.forge.ui.whiteboard.WhiteboardController
import io.agora.board.forge.ui.whiteboard.WhiteboardControllerConfig
import io.agora.board.ui.sample.Constants

/**
 * whiteboard ui activity
 */
class WhiteboardUIActivity : BaseActivity<ActivityWhiteboardUiBinding>() {
    private lateinit var room: Room
    private lateinit var controller: WhiteboardController

    override fun inflateBinding(inflater: LayoutInflater) = ActivityWhiteboardUiBinding.inflate(inflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        controller = WhiteboardController(
            context = this,
            config = WhiteboardControllerConfig()
        )
        controller.attach(binding.whiteboardContainer)
        controller.start(room)
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.stop()
    }
}

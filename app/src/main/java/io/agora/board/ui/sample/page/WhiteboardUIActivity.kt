package io.agora.board.ui.sample.page

import android.os.Bundle
import android.view.LayoutInflater
import io.agora.board.forge.Room
import io.agora.board.forge.RoomCallback
import io.agora.board.forge.RoomError
import io.agora.board.forge.RoomOptions
import io.agora.board.forge.common.dev.FakeSocketProvider
import io.agora.board.forge.rtm.RtmSocketProvider
import io.agora.board.forge.ui.sample.databinding.ActivityWhiteboardUiBinding
import io.agora.board.forge.ui.whiteboard.WhiteboardController
import io.agora.board.forge.ui.whiteboard.WhiteboardControllerConfig
import io.agora.board.ui.sample.util.ConfigLoader
import io.agora.board.ui.sample.util.RtmHelper

/**
 * whiteboard ui activity
 */
class WhiteboardUIActivity : BaseActivity<ActivityWhiteboardUiBinding>() {
    private lateinit var room: Room
    private lateinit var controller: WhiteboardController

    private var currentWritable: Boolean = ConfigLoader.writable

    override fun inflateBinding(inflater: LayoutInflater) =
        ActivityWhiteboardUiBinding.inflate(inflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.toggleWritable.setOnClickListener {
            val targetWritable = !currentWritable
            room.setWritable(writable = targetWritable, callback = object : RoomCallback<Boolean> {
                override fun onSuccess(result: Boolean) {
                    currentWritable = result
                }

                override fun onFailure(error: RoomError) {

                }
            })
        }

        val socketProvider = if (RtmHelper.getInstance().isLogin()) {
            RtmSocketProvider(RtmHelper.getInstance().rtmClient())
        } else {
            FakeSocketProvider()
        }

        val roomOptions = RoomOptions(
            context = this,
            roomId = ConfigLoader.roomId,
            roomToken = ConfigLoader.roomToken,
            userId = ConfigLoader.userId,
        ).apply {
            writable(currentWritable)
            socketProvider(socketProvider)
            region(ConfigLoader.boardRegion)
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

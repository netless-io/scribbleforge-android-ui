package io.agora.board.forge.ui.host

import android.content.Context
import io.agora.board.forge.ApplicationManager
import io.agora.board.forge.Room
import io.agora.board.forge.RoomCallback
import io.agora.board.forge.RoomError
import io.agora.board.forge.RoomOptions
import io.agora.board.forge.imagerydoc.ImageryDocApplication
import io.agora.board.forge.imagerydoc.ImageryDocFactory
import io.agora.board.forge.rtm.RtmSocketProvider
import io.agora.board.forge.whiteboard.WhiteboardApplication
import io.agora.board.forge.whiteboard.WhiteboardFactory
import io.agora.board.forge.whiteboard.WhiteboardOption
import io.agora.board.forge.whiteboard.WhiteboardToolInfoOptions
import io.agora.board.forge.whiteboard.WhiteboardToolType
import io.agora.board.forge.whiteboard.launchWhiteboard
import io.agora.board.forge.ui.contract.BoardMainWindow
import io.agora.board.forge.ui.contract.BoardRoom
import io.agora.board.forge.ui.contract.BoardRoomListener
import io.agora.board.forge.ui.contract.model.ForgeCallback
import io.agora.board.forge.ui.contract.model.ForgeError
import io.agora.board.forge.ui.contract.model.Region
import io.agora.board.forge.ui.contract.model.RoomConnectionState
import io.agora.board.forge.ui.contract.model.RoomJoinConfig
import io.agora.rtm.RtmClient

/**
 * Host implementation of BoardRoom using Forge SDK
 * This is the ONLY layer that can reference the Forge SDK directly
 */
class ForgeBoardRoomHost(
    val context: Context,
    private val rtmClient: RtmClient
) : BoardRoom {

    private lateinit var room: Room
    private var whiteboardApp: WhiteboardApplication? = null
    private var listener: BoardRoomListener? = null

    override fun init(appId: String, region: Region) {
        // Initialization happens during join() in this implementation
    }

    override fun join(
        config: RoomJoinConfig,
        callback: ForgeCallback<BoardMainWindow>
    ) {
        val roomOptions = RoomOptions(
            context = context.applicationContext,
            roomId = config.roomId,
            roomToken = config.roomToken,
            userId = config.userId ?: "default_user",
        ).socketProvider(RtmSocketProvider(rtmClient))
            .nickName("user_${config.userId ?: "default"}")
            .region(regionToSdkRegion(config.boardRatio)) // Note: using boardRatio as placeholder for region
            .appIdentifier("123/123")

        room = Room(roomOptions)

        room.appManager.addListener(object : ApplicationManager.Listener {
            override fun onAppLaunch(appId: String) {
                whiteboardApp = room.appManager.getRoomApp(appId) as WhiteboardApplication
                callback.onSuccess(ForgeBoardMainWindowHost(whiteboardApp!!))
            }

            override fun onAppTerminate(appId: String) {
                listener?.onConnectionStateUpdated(RoomConnectionState.Disconnected)
            }
        })

        room.joinRoom(object : RoomCallback<Boolean> {
            override fun onSuccess(result: Boolean) {
                val whiteboardOption = WhiteboardOption(
                    width = 1920,
                    height = 1080,
                    maxScaleRatio = 2f,
                    defaultToolbarStyle = WhiteboardToolInfoOptions(tool = WhiteboardToolType.CURVE),
                )
                room.launchWhiteboard(
                    whiteboardOption,
                    "MainWhiteboard",
                    callback = object : RoomCallback<WhiteboardApplication> {
                        override fun onSuccess(result: WhiteboardApplication) {
                            // Whiteboard launched successfully
                        }

                        override fun onFailure(error: RoomError) {
                            // Handle failure
                        }
                    })
            }

            override fun onFailure(error: RoomError) {
                callback.onFailure(ForgeError(message = error.message))
            }
        })
    }

    override fun leave() {
        room.leaveRoom()
    }

    override fun destroy() {
        room.leaveRoom()
    }

    override fun setBoardRoomListener(listener: BoardRoomListener) {
        this.listener = listener
    }

    /**
     * Get the underlying Room from SDK
     * This is exposed for advanced use cases but should be used carefully
     */
    fun getRoom(): Room {
        return room
    }

    private fun regionToSdkRegion(region: Float): String {
        // This is a placeholder - actual implementation should map Region enum to SDK region values
        return "cn"
    }
}

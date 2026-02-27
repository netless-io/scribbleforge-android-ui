package io.agora.board.forge.ui.whiteboard

import android.content.Context
import android.view.ViewGroup
import io.agora.board.forge.ApplicationListener
import io.agora.board.forge.Room
import io.agora.board.forge.RoomCallback
import io.agora.board.forge.RoomError
import io.agora.board.forge.WritableListener
import io.agora.board.forge.ui.ForgeUIConfig
import io.agora.board.forge.whiteboard.WhiteboardApplication

class WhiteboardController(
    context: Context,
    private val config: WhiteboardControllerConfig,
    private val forgeUIConfig: ForgeUIConfig = ForgeUIConfig()
) {
    val view: WhiteboardContainerView = WhiteboardContainerView(context, forgeUIConfig)

    private var room: Room? = null
    private var whiteboardApp: WhiteboardApplication? = null
    private var whiteboardControlLayout: WhiteboardControlLayout? = null

    private var started: Boolean = false

    private val appListener = object : ApplicationListener {
        override fun onAppLaunch(appId: String) {
            if (appId == config.appId) {
                handleAppLaunch()
            }
        }

        override fun onAppTerminate(appId: String) {
            if (appId == config.appId) {
                handleAppTerminate()
            }
        }
    }

    private val writableListener = object : WritableListener {
        override fun onWritableChanged(userId: String, writable: Boolean) {
            if (userId == room?.userManager?.userId) {
                whiteboardControlLayout?.setWritable(writable)
            }
        }
    }

    init {
        whiteboardControlLayout = view.whiteboardControlLayout
    }

    fun attach(container: ViewGroup) {
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        container.addView(view, params)
    }

    /**
     * Start whiteboard.
     *
     * Default behavior: controller will join the room internally.
     * If the room is already joined externally, joinRoom will be ignored by SDK.
     */
    fun start(room: Room) {
        if (started) return
        started = true
        this.room = room
        room.addAppListener(appListener)
        room.addWritableListener(writableListener)

        room.joinRoom(object : RoomCallback<Boolean> {
            override fun onSuccess(result: Boolean) {
                launchWhiteboardApp()
            }

            override fun onFailure(error: RoomError) {
                started = false
            }
        })
    }

    /**
     * Stop whiteboard.
     */
    fun stop() {
        if (!started) return
        started = false

        cleanup()

        room?.removeAppListener(appListener)
        room?.removeWritableListener(writableListener)
        room?.leaveRoom()
    }

    private fun launchWhiteboardApp() {
        room?.launchApp(
            type = WhiteboardApplication.TYPE,
            appId = config.appId,
            option = config.whiteboardOption,
        )
    }

    private fun handleAppLaunch() {
        val app = room?.getApp(config.appId) as? WhiteboardApplication ?: return
        whiteboardApp = app
        view.addWhiteboardView(app.getView()!!)
        whiteboardControlLayout?.bind(app)
    }

    private fun handleAppTerminate() {
        cleanup()
    }

    private fun cleanup() {
        whiteboardControlLayout?.unbind()
        whiteboardApp = null
    }
}

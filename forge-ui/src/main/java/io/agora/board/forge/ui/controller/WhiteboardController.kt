package io.agora.board.forge.ui.controller

import android.widget.FrameLayout
import io.agora.board.forge.ApplicationListener
import io.agora.board.forge.Room
import io.agora.board.forge.RoomCallback
import io.agora.board.forge.RoomError
import io.agora.board.forge.ui.ForgeUIConfig
import io.agora.board.forge.ui.component.WhiteboardContainer
import io.agora.board.forge.ui.component.WhiteboardControlLayout
import io.agora.board.forge.ui.internal.addMatchParent
import io.agora.board.forge.whiteboard.WhiteboardApplication

class WhiteboardController(
    private val container: FrameLayout,
    private val config: WhiteboardControllerConfig,
    private val forgeUIConfig: ForgeUIConfig = ForgeUIConfig()
) {
    private var room: Room? = null
    private var whiteboardApp: WhiteboardApplication? = null
    private var whiteboardContainer: WhiteboardContainer? = null
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

    init {
        whiteboardContainer = WhiteboardContainer(container.context, forgeUIConfig).also {
            whiteboardControlLayout = it.whiteboardControlLayout
        }
    }

    fun start(room: Room, selfJoin: Boolean = false) {
        if (started) return
        started = true
        this.room = room
        room.addAppListener(appListener)

        if (selfJoin) {
            launchWhiteboardApp()
            return
        } else {
            room.joinRoom(object : RoomCallback<Boolean> {
                override fun onSuccess(result: Boolean) {
                    launchWhiteboardApp()
                }

                override fun onFailure(error: RoomError) {
                }
            })
        }
    }

    fun stop() {
        if (!started) return
        started = false

        cleanup()

        room?.removeAppListener(appListener)
        room?.leaveRoom()
    }

    private fun launchWhiteboardApp() {
        room?.launchApp(
            type = WhiteboardApplication.TYPE, appId = config.appId, option = config.whiteboardOption
        )
    }

    private fun handleAppLaunch() {
        val app = room?.getApp(config.appId) as? WhiteboardApplication ?: return
        whiteboardApp = app
        whiteboardContainer?.addWhiteboardView(app.getView()!!)
        whiteboardControlLayout?.attachWhiteboard(app)
        container.addMatchParent((whiteboardContainer))
    }

    private fun handleAppTerminate() {
        cleanup()
    }

    private fun cleanup() {
        container.removeView(whiteboardContainer)
        whiteboardControlLayout?.detachWhiteboard()
        whiteboardControlLayout = null
        whiteboardApp = null
    }
}

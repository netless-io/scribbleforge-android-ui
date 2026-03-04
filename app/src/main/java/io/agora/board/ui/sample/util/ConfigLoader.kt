package io.agora.board.ui.sample.util

import android.content.Context
import io.agora.board.forge.RoomRegion
import com.google.gson.Gson
import java.io.IOException

data class RoomConfig(
    val forgeRoomId: String = "",
    val forgeRoomToken: String = "",
    val forgeUserId: String = "",
    val forgeRtmAppId: String = "",
    val forgeRtmToken: String = "",
    val forgeRegionEndpoint: String = "",
)

object ConfigLoader {
    private const val CONFIG_FILE = "config.json"

    private const val FALLBACK_ENDPOINT = "https://forge-persistence.netless.group"
    private const val FALLBACK_RTM_APP_ID = "d578d862f85a4545bab8d1f416e4fbd2"

    var roomConfig: RoomConfig? = null
        private set

    fun init(context: Context) {
        roomConfig = load(context)
    }

    private fun load(context: Context): RoomConfig? {
        return try {
            context.assets.open(CONFIG_FILE).bufferedReader().use { reader ->
                Gson().fromJson(reader, RoomConfig::class.java)
            }
        } catch (e: IOException) {
            null
        }
    }

    val boardRegion: RoomRegion
        get() {
            val endpoint = roomConfig?.forgeRegionEndpoint?.takeIf { it.isNotBlank() }
            return if (endpoint.isNullOrBlank()) RoomRegion.CN else RoomRegion.PRIVATE(endpoint)
        }

    val endpointForRequest: String
        get() = roomConfig?.forgeRegionEndpoint?.takeIf { it.isNotBlank() } ?: FALLBACK_ENDPOINT

    val forgeRtmAppId: String
        get() = roomConfig?.forgeRtmAppId?.takeIf { it.isNotBlank() } ?: FALLBACK_RTM_APP_ID

    val currentUser: User
        get() = User(
            userId = userId,
            nickName = userId,
            rtmToken = rtmToken,
            roomToken = roomToken,
        )

    var roomId: String
        get() = KvStore.getRoomId(roomConfig?.forgeRoomId ?: "")
        set(value) = KvStore.setRoomId(value)

    var roomToken: String
        get() = KvStore.getRoomToken(roomConfig?.forgeRoomToken ?: "")
        set(value) = KvStore.setRoomToken(value)

    var rtmToken: String
        get() = KvStore.getUserRtmToken(userId, roomConfig?.forgeRegionEndpoint ?: "")
        set(value) = KvStore.setUserRtmToken(userId, value)

    var userId: String
        get() = KvStore.getUserId()
        set(value) = KvStore.setUserId(value)

    var writable: Boolean
        get() = KvStore.isWritable(true)
        set(value) = KvStore.setWriteable(value)
}

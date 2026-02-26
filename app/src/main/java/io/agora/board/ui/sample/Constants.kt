package io.agora.board.ui.sample

import io.agora.board.forge.RoomRegion
import io.agora.board.ui.sample.util.KvStore

data class User(
    val userId: String,
    var rtmToken: String,
    var roomToken: String,
    val nickName: String? = null,
)

object Constants {
    @JvmField
    val ENDPOINT = "https://forge-persistence.netless.group"

    @JvmField
    val BOARD_REGION = RoomRegion.PRIVATE(ENDPOINT)

    const val BOARD_ROOM_ID = "b75707d0d4a211f0bc6c5d79065e35bd"
    const val BOARD_ROOM_TOKEN = "NETLESSROOM_YWs9SFpDQ2xQN3dpWEdmSTRWUzJGTlFGZldJYWp0bDVBM001NkRaJm5vbmNlPTE3NjUyNDU1Nzc3NzIwMCZyb2xlPTAmc2lnPTI5OWY4OGI4M2I1MjBiM2ViYzA0OWM0NWU5YjJhZWY4MTQ2MDQ2NGI1ZTViYjVmYjkwMzE3ZmRhMTM0MjFkYzMmdXVpZD1iNzU3MDdkMGQ0YTIxMWYwYmM2YzVkNzkwNjVlMzViZA"

    val currentUser: User
        get() = User(
            userId = userId,
            nickName = userId,
            rtmToken = rtmToken,
            roomToken = roomToken,
        )

    var roomId: String
        get() = KvStore.getRoomId(BOARD_ROOM_ID)
        set(value) = KvStore.setRoomId(value)

    var roomToken: String
        get() = KvStore.getRoomToken(BOARD_ROOM_TOKEN)
        set(value) = KvStore.setRoomToken(value)

    var rtmToken: String
        get() = KvStore.getUserRtmToken(userId, "")
        set(value) = KvStore.setUserRtmToken(userId, value)

    var userId: String
        get() = KvStore.getUserId()
        set(value) = KvStore.setUserId(value)

    var writable: Boolean
        get() = KvStore.isWritable(true)
        set(value) = KvStore.setWriteable(value)
}

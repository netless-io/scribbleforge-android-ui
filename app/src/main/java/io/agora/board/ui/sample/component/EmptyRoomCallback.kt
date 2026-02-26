package io.agora.board.ui.sample.component

import android.util.Log
import io.agora.board.forge.RoomCallback
import io.agora.board.forge.RoomError

class EmptyRoomCallback<T> : RoomCallback<T> {
    override fun onSuccess(result: T) {
        Log.e("EmptyRoomCallback", "onSuccess")
    }

    override fun onFailure(error: RoomError) {
        Log.e("EmptyRoomCallback", "onFailure")
    }
}

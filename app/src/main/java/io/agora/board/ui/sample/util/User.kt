package io.agora.board.ui.sample.util

data class User(
    val userId: String,
    var rtmToken: String,
    var roomToken: String,
    val nickName: String? = null,
)

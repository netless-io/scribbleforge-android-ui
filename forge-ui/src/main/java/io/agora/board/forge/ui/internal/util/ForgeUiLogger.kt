package io.agora.board.forge.ui.internal.util

import io.agora.board.forge.Logger

/**
 * Logger utility for board operations
 */
object ForgeUiLogger {
    fun d(tag: String, msg: String) {
        Logger.d("[$tag] $msg")
    }

    fun i(tag: String, msg: String) {
        Logger.i("[$tag] $msg")
    }

    fun e(tag: String, s: String) {
        Logger.e("[$tag] $s")
    }

    fun e(tag: String, s: String, e: Throwable) {
        Logger.e("[$tag] $s", e)
    }
}

package io.agora.board.forge.ui.internal.util

import android.util.Log

/**
 * Logger utility for board operations
 */
object Logger {
    const val TAG = "Board"

    fun i(tag: String, msg: String) {
        Log.i(TAG, "$tag: $msg")
    }

    fun d(tag: String, msg: String) {
        Log.d(TAG, "$tag: $msg")
    }

    fun e(tag: String, s: String, e: Exception? = null) {
        Log.e(TAG, "$tag: $s", e)
    }
}

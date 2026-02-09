package io.agora.board.forge.ui.internal.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Coroutines helper for main thread operations
 */
class CoroutinesHelper {
    companion object {
        private val mainScope = CoroutineScope(Dispatchers.Main)

        /**
         * Execute on main thread
         */
        fun runMainThread(run: () -> Unit) {
            mainScope.launch {
                run.invoke()
            }
        }

        /**
         * Execute on IO thread
         */
        fun runThread(run: () -> Unit) {
            mainScope.launch(Dispatchers.IO) {
                run.invoke()
            }
        }

        fun getMainScope(): CoroutineScope {
            return mainScope
        }

        fun cancel() {
            mainScope.cancel()
        }
    }
}

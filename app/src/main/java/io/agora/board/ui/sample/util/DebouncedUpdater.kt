package io.agora.board.ui.sample.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DebouncedUpdater(
    private val coroutineScope: CoroutineScope = MainScope(),
    private val delayMs: Long = 1000L,
    private val action: () -> Unit
) {
    private var debounceJob: Job? = null

    fun invoke() {
        debounceJob?.cancel() // 取消之前的
        debounceJob = coroutineScope.launch {
            delay(delayMs)      // 延迟执行
            action()            // 保证最后一次执行
        }
    }
}

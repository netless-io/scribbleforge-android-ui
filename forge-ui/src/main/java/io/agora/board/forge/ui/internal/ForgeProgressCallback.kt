package io.agora.board.forge.ui.internal

import io.agora.board.forge.ui.whiteboard.component.ForgeError

interface ForgeProgressCallback<T> {
    fun onSuccess(res: T) {}

    fun onFailure(error: ForgeError) {}

    fun onProgress(progress: Int) {}
}

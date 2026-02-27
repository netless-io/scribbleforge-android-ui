package io.agora.board.forge.ui.internal

interface ForgeProgressCallback<T> {
    fun onSuccess(res: T) {}

    fun onFailure(error: Exception) {}

    fun onProgress(progress: Int) {}
}

package io.agora.board.forge.ui

interface ForgeProgressCallback<T> {
    fun onSuccess(res: T) {}

    fun onFailure(error: ForgeError) {}

    fun onProgress(progress: Int) {}
}

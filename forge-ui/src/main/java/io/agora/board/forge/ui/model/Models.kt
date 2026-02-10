package io.agora.board.forge.ui.model.model

/**
 * Error data class for API callbacks
 */
data class ForgeError(
    var code: Int = -1,
    var message: String? = "",
    var httpCode: Int? = null
)

/**
 * Progress callback interface for operations that report progress
 */
interface ForgeProgressCallback<T> {
    fun onSuccess(res: T) {}

    fun onFailure(error: ForgeError) {}

    fun onProgress(progress: Int) {}
}

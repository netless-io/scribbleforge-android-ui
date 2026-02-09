package io.agora.board.forge.ui.internal.util

import io.agora.board.forge.ui.contract.model.ForgeError

/**
 * Sealed class for board operation results
 */
sealed class BoardResult<out T> {
    data class Success<out T>(val data: T) : BoardResult<T>()
    data class Failure(val error: ForgeError) : BoardResult<Nothing>()
}

package io.agora.board.forge.ui.contract.model

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Error data class for API callbacks
 */
data class ForgeError(
    var code: Int = -1,
    var message: String? = "",
    var httpCode: Int? = null
)

/**
 * Callback interface for async operations
 */
interface ForgeCallback<T> {
    fun onSuccess(res: T?)

    fun onFailure(error: ForgeError)
}

/**
 * Progress callback interface for operations that report progress
 */
interface ForgeProgressCallback<T> {
    fun onSuccess(res: T) {}

    fun onFailure(error: ForgeError) {}

    fun onProgress(progress: Int) {}
}

/**
 * Observer manager interface
 */
interface IObserver<T> {
    fun addObserver(observer: T?)

    fun removeObserver(observer: T?)

    fun getObserver(): List<T>

    fun forEachObserver(callback: ((T) -> Unit)?)

    fun release()
}

/**
 * Abstract observer implementation using CopyOnWriteArrayList for thread safety
 */
abstract class AbstractObserver<T> : IObserver<T> {
    private val observers: MutableList<T> = CopyOnWriteArrayList()

    override fun addObserver(observer: T?) {
        observer?.let { h ->
            synchronized(this) {
                if (!observers.contains(h)) {
                    observers.add(h)
                }
            }
        }
    }

    override fun removeObserver(observer: T?) {
        observer?.let { h ->
            synchronized(this) {
                if (observers.contains(h)) {
                    observers.remove(h)
                }
            }
        }
    }

    override fun getObserver(): List<T> {
        return observers.toList()
    }

    override fun forEachObserver(callback: ((T) -> Unit)?) {
        observers.forEach { callback?.invoke(it) }
    }

    override fun release() {
        observers.clear()
    }
}

/**
 * User info placeholder class
 */
class UserInfo

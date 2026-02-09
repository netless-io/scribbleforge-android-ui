package io.agora.board.sample.util

import io.agora.board.sample.Constants
import io.agora.rtm.ErrorInfo
import io.agora.rtm.MessageEvent
import io.agora.rtm.ResultCallback
import io.agora.rtm.RtmClient
import io.agora.rtm.RtmConfig
import io.agora.rtm.RtmEventListener
import io.agora.rtm.SubscribeOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RtmHelper {
    private var rtmClient: RtmClient
    private var isLogin = false
    private var channelName = "test_channel"

    var onMessageReceived: ((MessageEvent) -> Unit)? = null

    init {
        RtmClient.release()
        val config = RtmConfig.Builder(
            Constants.AGORA_ID,
            Constants.currentUser.userId,
        ).presenceTimeout(5).build()
        rtmClient = RtmClient.create(config)
        rtmClient.addEventListener(object : RtmEventListener {
            override fun onMessageEvent(event: MessageEvent) {
                if (event.channelName == channelName) {
                    onMessageReceived?.invoke(event)
                }
            }
        })
    }

    suspend fun login() = suspendCoroutine { cont ->
        rtmClient.login(Constants.currentUser.rtmToken, object : ResultCallback<Void> {
            override fun onSuccess(p0: Void?) {
                isLogin = true
                subscribe()
                cont.resume(Unit)
            }

            override fun onFailure(err: ErrorInfo) {
                cont.resumeWithException(RuntimeException(err.toString()))
            }
        })
    }

    private fun subscribe() {
        rtmClient.subscribe(channelName, SubscribeOptions(), object : ResultCallback<Void> {
            override fun onSuccess(p0: Void?) {
            }

            override fun onFailure(err: ErrorInfo) {
            }
        })
    }

    suspend fun logout() = suspendCoroutine { cont ->
        rtmClient.logout(object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {
                isLogin = false
                cont.resume(Unit)
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                cont.resumeWithException(RuntimeException("logout rtm failed"))
            }
        })
    }

    fun isLogin(): Boolean {
        return isLogin
    }

    fun rtmClient(): RtmClient {
        return rtmClient
    }
}

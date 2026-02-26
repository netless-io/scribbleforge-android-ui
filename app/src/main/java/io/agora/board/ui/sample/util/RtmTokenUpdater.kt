package io.agora.board.ui.sample.util

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.agora.board.ui.sample.Constants
import kotlinx.coroutines.delay
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RtmTokenUpdater(val context: Context) {
    private val client = OkHttpClient()
    private val gson = Gson()

    suspend fun getRtmToken() {
        repeat(3) {
            try {
                val token = requestToken(KvStore.getUserId())
                KvStore.setUserRtmToken(KvStore.getUserId(), token)
                return
            } catch (e: Exception) {
                Log.e("RtmTokenUpdater", "refresh token failed: $e")
            }
            delay(1000)
        }
    }

    private suspend fun requestToken(userId: String) = suspendCoroutine<String> { cont ->
        val request = Request.Builder()
            .url("${Constants.ENDPOINT}/${Constants.roomId}/$userId/rtm/token")
            .post("".toRequestBody())
            .addHeader("Token", Constants.roomToken)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                cont.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("RtmTokenUpdater", "request token failed: ${response.code}")
                    return
                }
                try {
                    val token = gson.fromJson(response.body?.string(), JsonObject::class.java)["token"].asString
                    cont.resume(token)
                } catch (e: Exception) {
                    Log.e("RtmTokenUpdater", "parse token failed: $e")
                    cont.resumeWithException(e)
                }
            }
        })
    }
}

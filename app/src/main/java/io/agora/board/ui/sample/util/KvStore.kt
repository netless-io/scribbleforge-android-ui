package io.agora.board.ui.sample.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

object KvStore {
    private const val PREFS_NAME = "forge_sample_prefs"
    private const val KEY_UID = "uid"
    private const val KEY_RTM_TOKEN_PREFIX = "rtmToken_"
    private const val KEY_ROOM_ID = "roomId"
    private const val KEY_ROOM_TOKEN = "roomToken"
    private const val KEY_WRITEABLE = "isWriteable"

    // AppSettings keys
    const val KEY_DOC_DISPLAY_MODE = "imageryDoc.displayMode"
    const val KEY_DOC_INHERITWHITEBOARDID = "imageryDoc.inheritWhiteboardId"
    const val KEY_SLIDE_INHERITWHITEBOARDID = "slide.inheritWhiteboardId"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        getOrCreateUID()
    }

    fun setUserRtmToken(userId: String, rtmToken: String) {
        prefs.edit().putString(KEY_RTM_TOKEN_PREFIX + userId, rtmToken).apply()
    }

    fun getUserRtmToken(userId: String, defaultValue: String): String {
        return prefs.getString(KEY_RTM_TOKEN_PREFIX + userId, defaultValue) ?: defaultValue
    }

    fun getRoomId(defaultValue: String): String {
        return prefs.getString(KEY_ROOM_ID, defaultValue) ?: defaultValue
    }

    fun setRoomId(roomId: String) {
        prefs.edit().putString(KEY_ROOM_ID, roomId).apply()
    }

    fun getRoomToken(defaultValue: String): String {
        return prefs.getString(KEY_ROOM_TOKEN, defaultValue) ?: defaultValue
    }

    fun setRoomToken(roomToken: String) {
        prefs.edit().putString(KEY_ROOM_TOKEN, roomToken).apply()
    }

    fun getUserId(): String = getOrCreateUID()

    fun setUserId(userId: String) {
        saveUID(userId)
    }

    fun isWritable(defaultValue: Boolean): Boolean {
        return prefs.getBoolean(KEY_WRITEABLE, defaultValue)
    }

    fun setWriteable(isWriteable: Boolean) {
        prefs.edit().putBoolean(KEY_WRITEABLE, isWriteable).apply()
    }

    fun isDocContinuousMode(defaultValue: Boolean = false): Boolean {
        return prefs.getBoolean(KEY_DOC_DISPLAY_MODE, defaultValue)
    }

    fun setDocContinuousMode(isContinuous: Boolean) {
        prefs.edit().putBoolean(KEY_DOC_DISPLAY_MODE, isContinuous).apply()
    }

    fun isDocInheritWhiteboardId(defaultValue: Boolean = true): Boolean {
        return prefs.getBoolean(KEY_DOC_INHERITWHITEBOARDID, defaultValue)
    }

    fun setDocInheritWhiteboardId(inherit: Boolean) {
        prefs.edit().putBoolean(KEY_DOC_INHERITWHITEBOARDID, inherit).apply()
    }

    fun isSlideInheritWhiteboardId(defaultValue: Boolean = true): Boolean {
        return prefs.getBoolean(KEY_SLIDE_INHERITWHITEBOARDID, defaultValue)
    }

    fun setSlideInheritWhiteboardId(inherit: Boolean) {
        prefs.edit().putBoolean(KEY_SLIDE_INHERITWHITEBOARDID, inherit).apply()
    }

    private val gson = Gson()

    fun set(key: String, value: Any) {
        with(prefs.edit()) {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
                else -> putString(key, gson.toJson(value))
            }
            apply()
        }
    }

    fun get(key: String, default: Any): Any {
        return when (default) {
            is String -> prefs.getString(key, default) ?: default
            is Int -> prefs.getInt(key, default)
            is Boolean -> prefs.getBoolean(key, default)
            is Float -> prefs.getFloat(key, default)
            is Long -> prefs.getLong(key, default)
            else -> gson.fromJson(prefs.getString(key, ""), default::class.java) ?: default
        }
    }

    private fun getOrCreateUID(): String {
        return prefs.getString(KEY_UID, null) ?: "android_${randomString(6)}".also {
            prefs.edit().putString(KEY_UID, it).apply()
        }
    }

    private fun saveUID(uid: String) {
        prefs.edit().putString(KEY_UID, uid).apply()
    }
}

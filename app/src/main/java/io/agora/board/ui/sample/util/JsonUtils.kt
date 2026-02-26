package io.agora.board.ui.sample.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object JsonUtils {
    private val gson = Gson()

    fun toJson(src: Any): String {
        return gson.toJson(src)
    }

    /**
     * Deserializes a JSON string into an object of the specified type.
     *
     * @param T The type of the object to be returned.
     * @param json The JSON string to deserialize.
     * @param classOfT The class of T.
     * @return The deserialized object of type T.
     */
    fun <T> fromJson(json: String, classOfT: Class<T>): T {
        return gson.fromJson(json, classOfT)
    }

    fun <T> fromJson(json: String, typeToken: TypeToken<T>): T {
        return gson.fromJson(json, typeToken)
    }
}

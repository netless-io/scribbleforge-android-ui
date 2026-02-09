package io.agora.board.forge.common.webview

import com.google.gson.Gson
import org.junit.Test
import java.util.Base64
import kotlin.system.measureTimeMillis

class UtilsTest {

    @Test
    fun benchmarkGson() {
        val gson = Gson()

        // 准备 1MB 的数据
        val sizeInBytes = 1 * 1024 * 1024 // 1MB
        val byteArray = ByteArray(sizeInBytes) { it.toByte() }

        val serializeTime = measureTimeMillis {
            val json = gson.toJson(byteArray)
            println("Serialized JSON length: ${json.length}")
        }
        println("GSON serialization time: ${serializeTime}ms")
        val json = gson.toJson(byteArray)

        val deserializeTime = measureTimeMillis {
            val result: ByteArray = gson.fromJson(json, ByteArray::class.java)
            println("Deserialized array size: ${result.size}")
        }
        println("GSON deserialization time: ${deserializeTime}ms")

        val base64EncodeTime = measureTimeMillis {
            val base64 = Base64.getEncoder().encodeToString(byteArray)
            println("Base64 string length: ${base64.length}")
        }
        println("Base64 encode time: ${base64EncodeTime}ms")
    }
}

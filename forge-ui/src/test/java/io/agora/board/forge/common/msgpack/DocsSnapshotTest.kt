package io.agora.board.forge.common.msgpack

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DocsSnapshotTest {

    @Test
    fun testToBufferAndFromBuffer_RoundTrip() {
        val testData = mapOf(
            "app1" to "data1".toByteArray(),
            "app2" to "data2".toByteArray(),
            "app3" to "longer test data".toByteArray()
        )
        val original = DocsSnapshot(testData)

        val buffer = original.toBuffer()
        val decoded = DocsSnapshot.fromBuffer(buffer)

        // 验证大小
        assertEquals(original.size, decoded.size)
        assertEquals(3, decoded.size)

        // 验证每个键值对
        for ((appId, originalData) in testData) {
            val decodedData = decoded.data[appId]
            assertEquals("App ID $appId data should match", String(originalData), String(decodedData!!))
        }
    }

    @Test
    fun testEmptySnapshot() {
        val original = DocsSnapshot(emptyMap())

        val buffer = original.toBuffer()
        val decoded = DocsSnapshot.fromBuffer(buffer)

        assertEquals(0, decoded.size)
        assertEquals(0, decoded.data.size)
    }

    @Test
    fun testSingleEntry() {
        val testData = mapOf("single-app" to "single-data".toByteArray())
        val original = DocsSnapshot(testData)

        val buffer = original.toBuffer()
        val decoded = DocsSnapshot.fromBuffer(buffer)

        assertEquals(1, decoded.size)
        assertEquals("single-data", String(decoded.data["single-app"]!!))
    }

    @Test
    fun testBinaryData() {
        // 测试包含二进制数据的情况
        val binaryData = byteArrayOf(0x00, 0x01, 0x02, 0xFF.toByte(), 0xFE.toByte())
        val testData = mapOf("binary-app" to binaryData)
        val original = DocsSnapshot(testData)

        val buffer = original.toBuffer()
        val decoded = DocsSnapshot.fromBuffer(buffer)

        assertEquals(1, decoded.size)
        val decodedBinary = decoded.data["binary-app"]!!
        assertEquals(binaryData.size, decodedBinary.size)
        for (i in binaryData.indices) {
            assertEquals("Binary data at index $i should match", binaryData[i], decodedBinary[i])
        }
    }

    @Test
    fun testSizeProperty() {
        val testData = mapOf(
            "app1" to "data1".toByteArray(),
            "app2" to "data2".toByteArray()
        )
        val snapshot = DocsSnapshot(testData)

        assertEquals(2, snapshot.size)
        assertEquals(testData.size, snapshot.size)
    }
}
package io.agora.board.forge.common.msgpack

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DocsUpdateTest {
    // 用于测试解码的 MessagePack 字节数组，等价于 {"abcdef": new Uint8Array([1,2,3,255])}
    private val testMsgPackBytes = arrayOf(
        129,                            // map of 1 element
        166, 97, 98, 99, 100, 101, 102, // key: 'abcdef'
        196, 4, 1, 2, 3, 255            // bin(4): [1,2,3,255]
    ).map { it.toByte() }.toByteArray()

    /**
     * 测试 DocsUpdate 的序列化和反序列化
     */
    @Test
    fun testDocsUpdateSerialization() {
        val update = DocsUpdate(
            updates = mapOf(
                "app1" to byteArrayOf(10, 20),
                "app2" to byteArrayOf(30, 40),
            )
        )

        // 序列化
        val serialized = update.toBuffer()
        // 反序列化
        val deserialized = DocsUpdate.fromBuffer(serialized)

        // 获取反序列化后的更新内容
        val app1Update = deserialized.getUpdate("app1")
        val app2Update = deserialized.getUpdate("app2")

        // 校验内容不为 null
        assertNotNull(app1Update)
        assertNotNull(app2Update)

        // 校验内容长度
        assertEquals(2, app1Update?.size)
        assertEquals(2, app2Update?.size)

        assertArrayEquals(byteArrayOf(10, 20), app1Update)
        assertArrayEquals(byteArrayOf(30, 40), app2Update)
    }

    /**
     * 测试从 MessagePack 字节数组解码 DocsUpdate
     */
    @Test
    fun testDecodeFromArrayOfLiteral() {
        // 解码
        val docsUpdate = DocsUpdate.fromBuffer(testMsgPackBytes)

        // 校验 map 大小
        assertEquals(1, docsUpdate.size)

        // 获取 key 为 'abcdef' 的内容
        val buffer = docsUpdate.getUpdate("abcdef")

        assertNotNull(buffer)
        // 校验内容
        assertArrayEquals(byteArrayOf(1, 2, 3, -1), buffer)
    }
}


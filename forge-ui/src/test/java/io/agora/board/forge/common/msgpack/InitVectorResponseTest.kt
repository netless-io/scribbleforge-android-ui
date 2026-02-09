package io.agora.board.forge.common.msgpack

import org.junit.Assert.assertEquals
import org.junit.Test

class InitVectorResponseTest {

    @Test
    fun testToBufferAndFromBuffer_RoundTrip() {
        val testDocsUpdate = mapOf(
            "doc1" to "update1".toByteArray(),
            "doc2" to "update2".toByteArray(),
            "doc3" to "longer update data".toByteArray()
        )
        val original = InitVectorResponse(testDocsUpdate)

        val buffer = original.toBuffer()
        val decoded = InitVectorResponse.fromBuffer(buffer)

        // 验证大小
        assertEquals(original.docsUpdate.size, decoded.docsUpdate.size)
        assertEquals(3, decoded.docsUpdate.size)

        // 验证每个文档更新
        for ((docId, originalUpdate) in testDocsUpdate) {
            val decodedUpdate = decoded.docsUpdate[docId]
            assertEquals("Doc ID $docId update should match", String(originalUpdate), String(decodedUpdate!!))
        }
    }

    @Test
    fun testEmptyDocsUpdate() {
        val original = InitVectorResponse(emptyMap())

        val buffer = original.toBuffer()
        val decoded = InitVectorResponse.fromBuffer(buffer)

        assertEquals(0, decoded.docsUpdate.size)
    }

    @Test
    fun testSingleDocUpdate() {
        val testDocsUpdate = mapOf("single-doc" to "single-update".toByteArray())
        val original = InitVectorResponse(testDocsUpdate)

        val buffer = original.toBuffer()
        val decoded = InitVectorResponse.fromBuffer(buffer)

        assertEquals(1, decoded.docsUpdate.size)
        assertEquals("single-update", String(decoded.docsUpdate["single-doc"]!!))
    }

    @Test
    fun testBinaryUpdateData() {
        // 测试包含二进制更新数据的情况
        val binaryUpdate = byteArrayOf(0x00, 0x01, 0x02, 0xFF.toByte(), 0xFE.toByte())
        val testDocsUpdate = mapOf("binary-doc" to binaryUpdate)
        val original = InitVectorResponse(testDocsUpdate)

        val buffer = original.toBuffer()
        val decoded = InitVectorResponse.fromBuffer(buffer)

        assertEquals(1, decoded.docsUpdate.size)
        val decodedUpdate = decoded.docsUpdate["binary-doc"]!!
        assertEquals(binaryUpdate.size, decodedUpdate.size)
        for (i in binaryUpdate.indices) {
            assertEquals("Binary update data at index $i should match", binaryUpdate[i], decodedUpdate[i])
        }
    }

    @Test
    fun testMultipleDocuments() {
        val testDocsUpdate = mapOf(
            "doc-001" to "update-1.0".toByteArray(),
            "doc-002" to "update-2.0".toByteArray(),
            "doc-003" to "update-3.0".toByteArray(),
            "doc-004" to "update-4.0".toByteArray()
        )
        val original = InitVectorResponse(testDocsUpdate)

        val buffer = original.toBuffer()
        val decoded = InitVectorResponse.fromBuffer(buffer)

        assertEquals(4, decoded.docsUpdate.size)
        
        // 验证所有文档更新都正确解码
        testDocsUpdate.forEach { (docId, expectedUpdate) ->
            val actualUpdate = decoded.docsUpdate[docId]
            assertEquals("Update for $docId should match", String(expectedUpdate), String(actualUpdate!!))
        }
    }

    @Test
    fun testSpecialCharactersInDocId() {
        val testDocsUpdate = mapOf(
            "doc-with-special-chars_123" to "update-data".toByteArray(),
            "doc.with.dots" to "another-update".toByteArray(),
            "doc@with@symbols" to "third-update".toByteArray()
        )
        val original = InitVectorResponse(testDocsUpdate)

        val buffer = original.toBuffer()
        val decoded = InitVectorResponse.fromBuffer(buffer)

        assertEquals(3, decoded.docsUpdate.size)
        testDocsUpdate.forEach { (docId, expectedUpdate) ->
            val actualUpdate = decoded.docsUpdate[docId]
            assertEquals("Update for special doc ID $docId should match", String(expectedUpdate), String(actualUpdate!!))
        }
    }

    @Test
    fun testLargeUpdateData() {
        // 测试较大的更新数据
        val largeUpdate = "This is a large update data that contains multiple sentences and should test the serialization and deserialization of larger binary data chunks.".toByteArray()
        val testDocsUpdate = mapOf("large-doc" to largeUpdate)
        val original = InitVectorResponse(testDocsUpdate)

        val buffer = original.toBuffer()
        val decoded = InitVectorResponse.fromBuffer(buffer)

        assertEquals(1, decoded.docsUpdate.size)
        val decodedUpdate = decoded.docsUpdate["large-doc"]!!
        assertEquals("Large update data should match", String(largeUpdate), String(decodedUpdate))
    }
}
package io.agora.board.forge.common.msgpack

import org.junit.Assert.assertEquals
import org.junit.Test

class InitVectorReqTest {

    @Test
    fun testToBufferAndFromBuffer_RoundTrip() {
        val testDocVectors = mapOf(
            "doc1" to "vector1".toByteArray(),
            "doc2" to "vector2".toByteArray(),
            "doc3" to "longer vector data".toByteArray()
        )
        val original = InitVectorReq(testDocVectors)

        val buffer = original.toBuffer()
        val decoded = InitVectorReq.fromBuffer(buffer)

        // 验证大小
        assertEquals(original.docVectors.size, decoded.docVectors.size)
        assertEquals(3, decoded.docVectors.size)

        // 验证每个文档向量
        for ((docId, originalVector) in testDocVectors) {
            val decodedVector = decoded.docVectors[docId]
            assertEquals("Doc ID $docId vector should match", String(originalVector), String(decodedVector!!))
        }
    }

    @Test
    fun testEmptyDocVectors() {
        val original = InitVectorReq(emptyMap())

        val buffer = original.toBuffer()
        val decoded = InitVectorReq.fromBuffer(buffer)

        assertEquals(0, decoded.docVectors.size)
    }

    @Test
    fun testSingleDocVector() {
        val testDocVectors = mapOf("single-doc" to "single-vector".toByteArray())
        val original = InitVectorReq(testDocVectors)

        val buffer = original.toBuffer()
        val decoded = InitVectorReq.fromBuffer(buffer)

        assertEquals(1, decoded.docVectors.size)
        assertEquals("single-vector", String(decoded.docVectors["single-doc"]!!))
    }

    @Test
    fun testBinaryVectorData() {
        // 测试包含二进制向量数据的情况
        val binaryVector = byteArrayOf(0x00, 0x01, 0x02, 0xFF.toByte(), 0xFE.toByte())
        val testDocVectors = mapOf("binary-doc" to binaryVector)
        val original = InitVectorReq(testDocVectors)

        val buffer = original.toBuffer()
        val decoded = InitVectorReq.fromBuffer(buffer)

        assertEquals(1, decoded.docVectors.size)
        val decodedVector = decoded.docVectors["binary-doc"]!!
        assertEquals(binaryVector.size, decodedVector.size)
        for (i in binaryVector.indices) {
            assertEquals("Binary vector data at index $i should match", binaryVector[i], decodedVector[i])
        }
    }

    @Test
    fun testMultipleDocuments() {
        val testDocVectors = mapOf(
            "doc-001" to "version-1.0".toByteArray(),
            "doc-002" to "version-2.0".toByteArray(),
            "doc-003" to "version-3.0".toByteArray(),
            "doc-004" to "version-4.0".toByteArray()
        )
        val original = InitVectorReq(testDocVectors)

        val buffer = original.toBuffer()
        val decoded = InitVectorReq.fromBuffer(buffer)

        assertEquals(4, decoded.docVectors.size)
        
        // 验证所有文档都正确解码
        testDocVectors.forEach { (docId, expectedVector) ->
            val actualVector = decoded.docVectors[docId]
            assertEquals("Vector for $docId should match", String(expectedVector), String(actualVector!!))
        }
    }

    @Test
    fun testSpecialCharactersInDocId() {
        val testDocVectors = mapOf(
            "doc-with-special-chars_123" to "vector-data".toByteArray(),
            "doc.with.dots" to "another-vector".toByteArray(),
            "doc@with@symbols" to "third-vector".toByteArray()
        )
        val original = InitVectorReq(testDocVectors)

        val buffer = original.toBuffer()
        val decoded = InitVectorReq.fromBuffer(buffer)

        assertEquals(3, decoded.docVectors.size)
        testDocVectors.forEach { (docId, expectedVector) ->
            val actualVector = decoded.docVectors[docId]
            assertEquals("Vector for special doc ID $docId should match", String(expectedVector), String(actualVector!!))
        }
    }
}
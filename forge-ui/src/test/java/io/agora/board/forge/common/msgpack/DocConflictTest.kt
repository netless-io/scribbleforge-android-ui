package io.agora.board.forge.common.msgpack

import org.junit.Assert.assertEquals
import org.junit.Test
import org.msgpack.core.MessagePack

class DocConflictTest {
    private val testBuffer: ByteArray = intArrayOf(
        0x82,                                                   // map of 2 key-value pairs
        0xA5, 0x64, 0x6F, 0x63, 0x49, 0x64,                     // "docId"
        0xA7, 0x64, 0x6F, 0x63, 0x2D, 0x31, 0x32, 0x33,         // "doc-123"
        0xAD, 0x76, 0x65, 0x72, 0x73, 0x69, 0x6F, 0x6E,
        0x56, 0x65, 0x63, 0x74, 0x6F, 0x72,                     // "versionVector"
        0xC4, 0x06, 0x76, 0x31, 0x2E, 0x32, 0x2E, 0x33          // binary "v1.2.3" (0xC4 = bin8, 0x06 = length 6)
    ).map { it.toByte() }.toByteArray()

    @Test
    fun testFromBuffer_WithManualEncodedBuffer() {
        val conflict = DocConflict.fromBuffer(testBuffer)

        assertEquals("doc-123", conflict.docId)
        assertEquals("v1.2.3", String(conflict.versionVector))
    }

    @Test
    fun testToBufferAndFromBuffer_RoundTrip() {
        val original = DocConflict(docId = "doc-123", versionVector = "v1.2.3".toByteArray())

        val buffer = original.toBuffer()
        val decoded = DocConflict.fromBuffer(buffer)

        // 自定义比较逻辑，因为equals方法已被移除
        assertEquals(original.docId, decoded.docId)
        assertEquals(String(original.versionVector), String(decoded.versionVector))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testFromBuffer_MissingDocId_ThrowsException() {
        // Manually encode only versionVector (missing docId)
        val buffer = MessagePack.newDefaultBufferPacker().use { packer ->
            packer.packMapHeader(1)
            packer.packString("versionVector")
            packer.packString("v1.2.3")
            packer.toByteArray()
        }

        DocConflict.fromBuffer(buffer)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testFromBuffer_MissingVersionVector_ThrowsException() {
        // Manually encode only docId (missing versionVector)
        val buffer = MessagePack.newDefaultBufferPacker().use { packer ->
            packer.packMapHeader(1)
            packer.packString("docId")
            packer.packString("doc-123")
            packer.toByteArray()
        }

        DocConflict.fromBuffer(buffer)
    }
}

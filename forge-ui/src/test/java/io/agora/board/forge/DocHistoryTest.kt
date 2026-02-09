package io.agora.board.forge

import io.agora.board.forge.internal.DocHistory
import io.agora.board.forge.internal.DocHistoryItem
import org.junit.Assert.assertTrue
import org.junit.Test

class DocHistoryTest {

    private fun sizeWithPadding(size: Int): Int {
        return (size + 3) / 4 * 4
    }

    @Test
    fun addItem_addsItemToBuffer() {
        val docHistory = DocHistory("room1", "user1")
        val initialBuffer = docHistory.getBuffer()
        assertTrue(initialBuffer.isEmpty())

        val item = DocHistoryItem("docId", "data1".toByteArray(), "origin1", 123456)
        docHistory.addItem(item)

        val updatedBuffer = docHistory.getBuffer()
        assertTrue(updatedBuffer.isNotEmpty())

        val expected = sizeWithPadding(16 + "data1".length + "origin1".length + "123456".length + "docId".length)
        assertTrue(updatedBuffer.size == expected)
    }

    @Test
    fun metaBuffer() {
        val docHistory = DocHistory("room1", "user1")
        val metaBuffer = docHistory.metaBuffer

        val expected = 8 + "room1".length + "user1".length
        assertTrue(metaBuffer.size == expected)
    }

    @Test
    fun decodeHistory() {
        val docHistory = DocHistory("room1", "user1")
        val item1 = DocHistoryItem("docId", "data1".toByteArray(), "origin1", 123456)
        val item2 = DocHistoryItem("docId", "data2".toByteArray(), "origin2", 123457)
        docHistory.addItem(item1)
        docHistory.addItem(item2)

        val inputBuffer = docHistory.metaBuffer + docHistory.getBuffer()

        val decoded = docHistory.decode(inputBuffer)
        assertTrue(decoded.roomId == "room1")
        assertTrue(decoded.userId == "user1")
        assertTrue(decoded.historyItems.size == 2)
        assertTrue(decoded.historyItems[0].timestamp == 123456L)
        assertTrue(decoded.historyItems[0].origin == "origin1")
        assertTrue(decoded.historyItems[0].buffer.contentEquals("data1".toByteArray()))
        assertTrue(decoded.historyItems[1].timestamp == 123457L)
        assertTrue(decoded.historyItems[1].origin == "origin2")
        assertTrue(decoded.historyItems[1].buffer.contentEquals("data2".toByteArray()))
    }
}

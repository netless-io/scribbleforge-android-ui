package io.agora.board.forge.internal

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ConflictRateLimiterTest {

    private lateinit var rateLimiter: ConflictRateLimiter

    @Before
    fun setUp() {
        rateLimiter = ConflictRateLimiter(conflictInterval = 500L)
    }

    @Test
    fun testCanHandleInitiallyTrue() {
        val appId = "app1"
        val publisher = "user1"

        val result = rateLimiter.canHandle(appId, publisher)
        assertTrue("第一次调用 should return true", result)
        assertTrue(rateLimiter.getFrequentPublishers().isEmpty())
    }

    @Test
    fun testCanHandleWithinIntervalFalse() {
        val appId = "app1"
        val publisher = "user1"

        rateLimiter.canHandle(appId, publisher) // 第一次调用，返回 true
        val result = rateLimiter.canHandle(appId, publisher) // 紧接着调用，应该返回 false
        assertFalse("短时间内第二次调用 should return false", result)
        assertTrue(rateLimiter.getFrequentPublishers().contains(appId to publisher))
    }

    @Test
    fun testCanHandleAfterIntervalTrue() {
        val appId = "app1"
        val publisher = "user1"

        rateLimiter.canHandle(appId, publisher) // 第一次调用
        Thread.sleep(600) // 超过 conflictInterval
        val result = rateLimiter.canHandle(appId, publisher)
        assertTrue("超过冲突间隔 should return true", result)
        assertFalse(rateLimiter.getFrequentPublishers().contains(appId to publisher))
    }

    @Test
    fun testMarkHandledRemovesFromFrequent() {
        val appId = "app1"
        val publisher = "user1"

        rateLimiter.canHandle(appId, publisher) // 第一次调用
        rateLimiter.canHandle(appId, publisher) // 第二次调用，加入 frequent
        assertTrue(rateLimiter.getFrequentPublishers().contains(appId to publisher))

        rateLimiter.markHandled(appId, publisher)
        assertFalse(rateLimiter.getFrequentPublishers().contains(appId to publisher))
    }

    @Test
    fun testMultiplePublishers() {
        val app1 = "app1"
        val app2 = "app2"
        val user1 = "user1"
        val user2 = "user2"

        assertTrue(rateLimiter.canHandle(app1, user1))
        assertTrue(rateLimiter.canHandle(app2, user2))
        assertTrue(rateLimiter.getFrequentPublishers().isEmpty())

        // 重复调用，触发 frequent
        assertFalse(rateLimiter.canHandle(app1, user1))
        assertFalse(rateLimiter.canHandle(app2, user2))
        val frequent = rateLimiter.getFrequentPublishers()
        assertTrue(frequent.contains(app1 to user1))
        assertTrue(frequent.contains(app2 to user2))
    }
}

package io.agora.board.forge.common.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ForgeEventBusTest {
    
    private lateinit var eventBus: ForgeEventBus
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        eventBus = ForgeEventBus(CoroutineScope(Dispatchers.Main))
    }
    
    @After
    fun tearDown() {
        eventBus.clear()
        Dispatchers.resetMain()
    }
    
    @Test
    fun testRegisterAndPostEvent() {
        // 准备测试数据
        val testAppId = "test-app-001"
        val testEventType = "user-action"
        val testDetail = JSONObject().apply {
            put("action", "click")
            put("target", "button")
        }
        
        var receivedEvent: ForgeEvent? = null
        val latch = CountDownLatch(1)
        
        // 注册监听器
        val listener = ForgeEventListener { event ->
            receivedEvent = event
            latch.countDown()
        }
        eventBus.register(listener)
        
        // 验证注册成功
        assertEquals(1, eventBus.getListenerCount())
        
        // 发送事件
        val originalEvent = ForgeEvent(testAppId, testEventType, testDetail)
        eventBus.post(originalEvent)
        
        // 推进调度器执行协程
        testDispatcher.scheduler.advanceUntilIdle()
        
        // 等待事件处理完成
        assertTrue("事件处理超时", latch.await(1, TimeUnit.SECONDS))
        
        // 验证事件内容
        assertNotNull("应该收到事件", receivedEvent)
        assertEquals("appId应该匹配", testAppId, receivedEvent?.appId)
        assertEquals("事件类型应该匹配", testEventType, receivedEvent?.type)
        assertEquals("事件详情应该匹配", testDetail, receivedEvent?.data)
    }
    
    @Test
    fun testUnregisterListener() {
        var eventCount = 0
        val latch = CountDownLatch(1)
        
        val listener = ForgeEventListener { event ->
            eventCount++
            latch.countDown()
        }
        
        // 注册监听器
        eventBus.register(listener)
        assertEquals(1, eventBus.getListenerCount())
        
        // 发送第一个事件
        eventBus.post(ForgeEvent("app1", "event1", "data1"))
        testDispatcher.scheduler.advanceUntilIdle()
        latch.await(1, TimeUnit.SECONDS)
        assertEquals("应该收到第一个事件", 1, eventCount)
        
        // 取消注册
        eventBus.unregister(listener)
        assertEquals(0, eventBus.getListenerCount())
        
        // 发送第二个事件
        eventBus.post(ForgeEvent("app2", "event2", "data2"))
        testDispatcher.scheduler.advanceUntilIdle()
        
        // 验证没有收到第二个事件
        assertEquals("取消注册后不应该再收到事件", 1, eventCount)
    }
    
    @Test
    fun testExceptionHandling() {
        var goodListenerCalled = false
        var badListenerCalled = false
        val latch = CountDownLatch(2) // 等待两个监听器都执行
        
        // 正常的监听器
        val goodListener = ForgeEventListener { event ->
            goodListenerCalled = true
            latch.countDown()
        }
        
        // 会抛异常的监听器
        val badListener = ForgeEventListener { event ->
            badListenerCalled = true
            latch.countDown()
            throw RuntimeException("测试异常")
        }
        
        // 注册两个监听器
        eventBus.register(goodListener)
        eventBus.register(badListener)
        assertEquals(2, eventBus.getListenerCount())
        
        // 发送事件
        eventBus.post(ForgeEvent("test-app", "test-event", "test-data"))
        
        // 推进调度器执行协程
        testDispatcher.scheduler.advanceUntilIdle()
        
        // 等待两个监听器都执行
        assertTrue("监听器执行超时", latch.await(1, TimeUnit.SECONDS))
        
        // 验证两个监听器都被调用了（异常不应该影响其他监听器）
        assertTrue("正常监听器应该被调用", goodListenerCalled)
        assertTrue("异常监听器也应该被调用", badListenerCalled)
    }
    
    @Test
    fun testGetDefaultAndClearAll() {
        // 测试单例模式
        val defaultBus1 = ForgeEventBus.getDefault()
        val defaultBus2 = ForgeEventBus.getDefault()
        assertSame("getDefault应该返回同一个实例", defaultBus1, defaultBus2)
        
        var eventReceived = false
        val latch = CountDownLatch(1)
        
        // 在默认实例上注册监听器
        val listener = ForgeEventListener { event ->
            eventReceived = true
            latch.countDown()
        }
        defaultBus1.register(listener)
        assertEquals(1, defaultBus1.getListenerCount())

        // 使用便捷方法发送事件
        defaultBus1.post(ForgeEvent("test-app-1", "test-type-1", "test-data-1"))
        
        // 推进调度器执行协程
        testDispatcher.scheduler.advanceUntilIdle()

        latch.await(1, TimeUnit.SECONDS)
        assertTrue("应该收到事件", eventReceived)

        // 测试清空所有监听器
        defaultBus1.unregisterAll()
        assertEquals(0, defaultBus1.getListenerCount())
        
        // 再次发送事件，应该没有监听器响应
        eventReceived = false
        defaultBus1.post(ForgeEvent("test-app-2", "test-type-2", "test-data-2"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse("清空后不应该收到事件", eventReceived)
    }
}

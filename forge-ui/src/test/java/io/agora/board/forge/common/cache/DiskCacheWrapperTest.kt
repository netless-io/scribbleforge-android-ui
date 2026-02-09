package io.agora.board.forge.common.cache

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileOutputStream

class DiskCacheWrapperTest {
    private lateinit var cacheDir: File
    private lateinit var diskCache: DiskCache

    private val TEST_CACHE_SIZE = 1024 * 1024L // 1MB

    @JvmField
    @Rule
    var tempDir: TemporaryFolder = TemporaryFolder()

    @Before
    fun setup() {
        cacheDir = tempDir.newFolder("disk_cache_test")
        cacheDir.mkdirs()
        diskCache = DiskCacheWrapper.create(cacheDir, TEST_CACHE_SIZE)
    }

    @After
    fun tearDown() {
        cacheDir.deleteRecursively()
    }

    @Test
    fun testCreate() {
        assertNotNull(diskCache)
        assertTrue(diskCache is DiskCacheWrapper)
    }

    @Test
    fun testPutAndGetFile() {
        val testKey = "test_key"
        val testContent = "Hello, World!"

        // Write to cache
        diskCache.put(testKey, object : DiskCache.Writer {
            override fun write(file: File) {
                FileOutputStream(file).use { outputStream ->
                    outputStream.write(testContent.toByteArray())
                }
            }
        })

        // Read from cache
        val cachedFile = diskCache.get(testKey)

        // Verify file exists and contains the expected content
        assertNotNull(cachedFile)
        assertTrue(cachedFile!!.exists())
        assertEquals(testContent, cachedFile.readText())
    }

    @Test
    fun testPutExistingKey() {
        val testKey = "test_key"

        // Write initial content
        diskCache.put(testKey) { file ->
            FileOutputStream(file).use { outputStream ->
                outputStream.write("Initial content".toByteArray())
            }
        }

        // Try to overwrite with new content
        diskCache.put(testKey) { file ->
            FileOutputStream(file).use { outputStream ->
                outputStream.write("New content".toByteArray())
            }
        }

        // Verify the content is still the initial content (since put() returns early if key exists)
        val cachedFile = diskCache.get(testKey)
        assertEquals("Initial content", cachedFile!!.readText())
    }

    @Test
    fun testDelete() {
        val testKey = "test_key_to_delete"

        // Write to cache
        diskCache.put(testKey, object : DiskCache.Writer {
            override fun write(file: File) {
                FileOutputStream(file).use { outputStream ->
                    outputStream.write("Delete me".toByteArray())
                }
            }
        })

        // Verify file exists
        assertNotNull(diskCache.get(testKey))

        // Delete the file
        diskCache.delete(testKey)

        // Verify file no longer exists
        assertNull(diskCache.get(testKey))
    }

    @Test
    fun testClear() {
        val testKeys = listOf("key1", "key2", "key3")

        // Write multiple files to cache
        testKeys.forEach { key ->
            diskCache.put(key, object : DiskCache.Writer {
                override fun write(file: File) {
                    FileOutputStream(file).use { outputStream ->
                        outputStream.write("Content for $key".toByteArray())
                    }
                }
            })
        }

        // Verify all files exist
        testKeys.forEach { key ->
            assertNotNull(diskCache.get(key))
        }

        // Clear the cache
        diskCache.clear()

        // Verify no files exist
        testKeys.forEach { key ->
            assertNull(diskCache.get(key))
        }
    }
}

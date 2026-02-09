package io.agora.board.forge.common

import org.junit.Assert.*

import org.junit.Test

class BasePermissionTest {

    private class TestPermission(flags: Int) : BasePermission(flags)

    @Test
    fun testGetAndSetFlags() {
        val perm = TestPermission(0)
        assertEquals(0, perm.getFlags())
        perm.setFlags(123)
        assertEquals(123, perm.getFlags())
    }

    @Test
    fun testAddAndRemoveFlags() {
        val perm = TestPermission(0)
        perm.addFlags(0b0010)
        assertEquals(0b0010, perm.getFlags())
        perm.addFlags(0b1000)
        assertEquals(0b1010, perm.getFlags())
        perm.removeFlags(0b0010)
        assertEquals(0b1000, perm.getFlags())
    }

    @Test
    fun testHasPermission() {
        val perm = TestPermission(0b1100)
        assertTrue(perm.hasPermission(0b1000))
        assertTrue(perm.hasPermission(0b0100))
        assertFalse(perm.hasPermission(0b0010))
    }

    @Test
    fun testNoneAndAllConstants() {
        val perm = TestPermission(BasePermission.NONE)
        assertEquals(0, perm.getFlags())
        perm.setFlags(BasePermission.ALL)
        assertEquals(Int.MAX_VALUE, perm.getFlags())
    }
}

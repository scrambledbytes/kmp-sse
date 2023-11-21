package cc.scrambledbytes.sse

import junit.framework.TestCase.assertFalse
import org.junit.Test
import kotlin.test.assertTrue

class SseLineTests {
    @Test
    fun testIsDispatch() {
        assertTrue(SseLine("").isDispatch)
        assertTrue(SseLine("\n").isDispatch)
        assertFalse(SseLine(":\n").isDispatch)
    }

    @Test
    fun testIsIgnore() {
        assertTrue(SseLine(":").isIgnore)
        assertTrue(SseLine(":\n").isIgnore)
        assertTrue(SseLine(": \n").isIgnore)
        assertTrue(SseLine(":a\n").isIgnore)
        assertFalse(SseLine("\n").isIgnore)
        assertFalse(SseLine("a").isIgnore)
    }

    @Test
    fun isProcessField() {
        assertTrue(SseLine("a:b").isProcessField)
        assertTrue(SseLine("a:").isProcessField)
        assertTrue(SseLine("a:\n").isProcessField)
        assertFalse(SseLine(":").isProcessField)
        assertFalse(SseLine(":a").isProcessField)
    }
}

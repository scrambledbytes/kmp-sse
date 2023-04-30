package cc.scrambledbytes.sse

import junit.framework.TestCase.assertFalse
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SseBufferTests {
    @Test
    fun testIsEmpty() {
        assertTrue(SseBuffer().isEmpty)
        assertTrue(SseBuffer(data = "\n").isEmpty)
        assertFalse(SseBuffer(data = "a").isEmpty)
    }

    @Test
    fun testToSseEvent() {
        // remove LF at the end
        assertEquals("", SseBuffer(data = "\n").toSseEvent().data)
        assertEquals("a", SseBuffer(data = "a\n").toSseEvent().data)
        assertEquals("\na", SseBuffer(data = "\na").toSseEvent().data)
    }
}

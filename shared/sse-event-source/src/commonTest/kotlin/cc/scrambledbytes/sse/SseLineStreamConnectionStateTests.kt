package cc.scrambledbytes.sse

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class SseLineStreamConnectionStateTests {
    @Test
    fun testIsFailedWhenAborted() {
        val state = SseLineStream.ConnectionState(200, "", true)
        assertTrue(state.isAborted)
    }

    @Test
    fun testIsFailedWhen204() {
        val state = SseLineStream.ConnectionState(204, "", false)
        assertTrue(state.isAborted)
    }

    @Test
    fun testIsFailedWhen401() {
        val state = SseLineStream.ConnectionState(401, "", false)
        assertTrue(state.isAborted)
    }

    @Test
    fun testIsFailedWhen403() {
        val state = SseLineStream.ConnectionState(403, "", false)
        assertTrue(state.isAborted)
    }

    @Test
    fun testIsNotRetryWhenAborted() {
        val state = SseLineStream.ConnectionState(200, "", true)
        assertFalse(state.isRetry)
    }

    @Test
    fun testIsRetryWhenNot200() {
        val state = SseLineStream.ConnectionState(201, "", false)
        assertTrue(state.isRetry)
    }
}
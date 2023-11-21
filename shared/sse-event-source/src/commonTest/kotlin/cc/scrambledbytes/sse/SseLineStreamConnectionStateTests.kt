package cc.scrambledbytes.sse

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SseLineStreamConnectionStateTests {
    @Test
    fun testIsFailedWhenAborted() {
        val state = SseLineStream.ConnectionState(200, "", true)
        assertTrue(state.isAborted)
    }

    @Test
    fun testIsFailedWhen204() {
        val state = SseLineStream.ConnectionState(204, "", false)
        assertTrue(state.isFailed)
    }

    @Test
    fun testIsFailedWhen401() {
        val state = SseLineStream.ConnectionState(401, "", false)
        assertTrue(state.isFailed)
    }

    @Test
    fun testIsFailedWhen403() {
        val state = SseLineStream.ConnectionState(403, "", false)
        assertTrue(state.isFailed)
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

package cc.scrambledbytes.sse.impl

import app.cash.turbine.test
import cc.scrambledbytes.sse.*
import cc.scrambledbytes.sse.ReadyState.CLOSED
import cc.scrambledbytes.sse.ReadyState.CONNECTING
import cc.scrambledbytes.sse.mock.FakeSseLineStreamProvider
import junit.framework.TestCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class HandleConnectDelayedTest {
    lateinit var provider: FakeSseLineStreamProvider

    @Before
    fun setup() {
        provider = FakeSseLineStreamProvider()
    }

    @Test
    fun testWhenRetryOpenConnection() = runBlocking {
        val state = SseLineStream.ConnectionState(201, contentType = "a", isAborted = false)
        val source = SseEventSource(VALID_URL, provider, delayProvider = { it.seconds })
        source as SseEventSourceImpl
        provider.connectionState = state
        source.open()

        source.state.test {
            assertEquals(0, source.connectionAttempt)
            assertEquals(SseEventSource.State(), awaitItem())
            assertEquals(SseEventSource.State(statusCode = 201, ready = CLOSED), awaitItem())
            assertEquals(SseEventSource.State(statusCode = 201, ready = CONNECTING), awaitItem())
            delay(10)
            assertEquals(1, source.connectionAttempt)
            assertEquals(SseEventSource.State(statusCode = 201, ready = CLOSED), awaitItem())
            assertEquals(SseEventSource.State(statusCode = 201, ready = CONNECTING), awaitItem())
            delay(10)
            assertEquals(2, source.connectionAttempt)
        }
    }


    @Test
    fun testCloseAfterMaxAttempts() = runBlocking {
        val state = SseLineStream.ConnectionState(201, contentType = "a", isAborted = false)
        val source = SseEventSource(
            VALID_URL,
            provider,
            delayProvider = { if (it == 2) null else it.seconds })
        source as SseEventSourceImpl
        provider.connectionState = state
        source.open()

        source.state.test {
            assertEquals(0, source.connectionAttempt)
            assertEquals(SseEventSource.State(), awaitItem())
            assertEquals(SseEventSource.State(statusCode = 201, ready = CLOSED), awaitItem())
            assertEquals(SseEventSource.State(statusCode = 201, ready = CONNECTING), awaitItem())
            delay(10)
            assertEquals(1, source.connectionAttempt)
            assertEquals(SseEventSource.State(statusCode = 201, ready = CLOSED), awaitItem())
            assertEquals(SseEventSource.State(statusCode = 201, ready = CONNECTING), awaitItem())
            delay(10)
            assertEquals(2, source.connectionAttempt)
            assertEquals(SseEventSource.State(statusCode = 201, ready = CLOSED), awaitItem())
            expectNoEvents()
        }

        delay(2_000) // wait for cleanup
        TestCase.assertTrue(provider.onCloseVisited)
    }


}

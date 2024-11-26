package cc.scrambledbytes.sse.impl

import app.cash.turbine.test
import cc.scrambledbytes.sse.ReadyState.CLOSED
import cc.scrambledbytes.sse.ReadyState.CONNECTING
import cc.scrambledbytes.sse.SseEventSource
import cc.scrambledbytes.sse.SseEventSourceImpl
import cc.scrambledbytes.sse.SseLineStream
import cc.scrambledbytes.sse.VALID_URL
import cc.scrambledbytes.sse.mock.FakeSseLineStreamProvider
import cc.scrambledbytes.sse.util.launchOnDefault
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class HandleConnectDelayedTest {
    lateinit var provider: FakeSseLineStreamProvider

    @BeforeTest
    fun setup() {
        provider = FakeSseLineStreamProvider()
    }

    @Test
    fun testWhenRetryOpenConnection() = runTest(timeout = 1.minutes) {
        val state = SseLineStream.ConnectionState(201, contentType = "a", isAborted = false)
        val source = SseEventSource(VALID_URL, provider, delayProvider = { it.seconds })
        source as SseEventSourceImpl
        provider.connectionState = state
        launchOnDefault {
            source.open()
        }


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
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun testCloseAfterMaxAttempts() = runTest(timeout = 1.minutes) {
        val state = SseLineStream.ConnectionState(201, contentType = "a", isAborted = false)
        val source = SseEventSource(
            VALID_URL,
            provider,
            delayProvider = { if (it == 2) null else it.seconds })
        source as SseEventSourceImpl
        provider.connectionState = state
        launchOnDefault {
            source.open()
        }

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

            cancelAndIgnoreRemainingEvents()
        }
    }


}

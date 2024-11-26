package cc.scrambledbytes.sse.impl

import app.cash.turbine.test
import cc.scrambledbytes.sse.ReadyState.CLOSED
import cc.scrambledbytes.sse.ReadyState.CONNECTING
import cc.scrambledbytes.sse.ReadyState.OPEN
import cc.scrambledbytes.sse.SseEventSource
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

class HandleConnectedTest {
    lateinit var provider: FakeSseLineStreamProvider

    @BeforeTest
    fun setup() {
        provider = FakeSseLineStreamProvider()
    }

    @Test
    fun testWhenOkOpenConnection() = runTest(timeout = 1.minutes) {
        val state = SseLineStream.ConnectionState(200, contentType = "", isAborted = false)
        val source = SseEventSource(VALID_URL, provider)
        provider.connectionState = state

        launchOnDefault {
            source.open()
        }


        source.state.test {
            assertEquals(SseEventSource.State(), awaitItem())
            assertEquals(
                SseEventSource.State(statusCode = 200, ready = OPEN, isFailed = false),
                awaitItem()
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun testWhenRetryOpenConnection() = runTest(timeout = 1.minutes) {
        val state = SseLineStream.ConnectionState(201, contentType = "a", isAborted = false)
        val source = SseEventSource(VALID_URL, provider)
        provider.connectionState = state
        launchOnDefault {
            source.open()
        }

        source.state.test {
            assertEquals(SseEventSource.State(), awaitItem())
            assertEquals(
                SseEventSource.State(statusCode = 201, ready = CLOSED, isFailed = false),
                awaitItem()
            )
            assertEquals(
                SseEventSource.State(
                    statusCode = 201,
                    ready = CONNECTING,
                    isFailed = false
                ), awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testWhenFailClose() = runTest(timeout = 1.minutes) {
        val state = SseLineStream.ConnectionState(204, contentType = "a", isAborted = false)
        val source = SseEventSource(VALID_URL, provider)
        provider.connectionState = state
        launchOnDefault {
            source.open()
        }

        source.state.test {
            assertEquals(SseEventSource.State(), awaitItem())
            assertEquals(
                SseEventSource.State(statusCode = 204, ready = CLOSED, isFailed = true),
                awaitItem()
            )
            cancelAndConsumeRemainingEvents()
        }


    }
}

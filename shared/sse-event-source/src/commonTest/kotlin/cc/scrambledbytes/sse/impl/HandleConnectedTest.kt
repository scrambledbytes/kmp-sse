package cc.scrambledbytes.sse.impl

import app.cash.turbine.test
import cc.scrambledbytes.sse.ReadyState.*
import cc.scrambledbytes.sse.SseEventSource
import cc.scrambledbytes.sse.SseLineStream
import cc.scrambledbytes.sse.VALID_URL
import cc.scrambledbytes.sse.mock.FakeSseLineStreamProvider
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class HandleConnectedTest {
    lateinit var provider: FakeSseLineStreamProvider

    @Before
    fun setup() {
        provider = FakeSseLineStreamProvider()
    }

    @Test
    fun testWhenOkOpenConnection() = runBlocking {
        val state = SseLineStream.ConnectionState(200, contentType = "", isAborted = false)
        val source = SseEventSource(VALID_URL, provider)
        provider.connectionState = state
        source.open()

        source.state.test {
            assertEquals(SseEventSource.State(), awaitItem())
            assertEquals(
                SseEventSource.State(statusCode = 200, ready = OPEN, isFailed = false),
                awaitItem()
            )
            expectNoEvents()
        }
    }

    @Test
    fun testWhenRetryOpenConnection() = runBlocking {
        val state = SseLineStream.ConnectionState(201, contentType = "a", isAborted = false)
        val source = SseEventSource(VALID_URL, provider)
        provider.connectionState = state
        source.open()

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
            expectNoEvents()
        }
    }

    @Test
    fun testWhenFailClose() = runBlocking {
        val state = SseLineStream.ConnectionState(204, contentType = "a", isAborted = false)
        val source = SseEventSource(VALID_URL, provider)
        provider.connectionState = state
        source.open()

        source.state.test {
            assertEquals(SseEventSource.State(), awaitItem())
            assertEquals(
                SseEventSource.State(statusCode = 204, ready = CLOSED, isFailed = true),
                awaitItem()
            )
            expectNoEvents()
        }

        delay(2_000) // wait for cleanup
        assertTrue(provider.onCloseVisited)
    }
}

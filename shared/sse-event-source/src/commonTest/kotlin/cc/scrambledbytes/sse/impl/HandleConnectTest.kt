package cc.scrambledbytes.sse.impl

import app.cash.turbine.test
import cc.scrambledbytes.sse.ReadyState.CLOSED
import cc.scrambledbytes.sse.SseEventSource
import cc.scrambledbytes.sse.SseLineStream
import cc.scrambledbytes.sse.SseUrl
import cc.scrambledbytes.sse.mock.FakeSseLineStreamProvider
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class HandleConnectTest {

    lateinit var provider: FakeSseLineStreamProvider

    @Before
    fun setup() {
        provider = FakeSseLineStreamProvider()
    }

    @Test
    fun testOnOpenCreateStreamIsCalled() = runBlocking {
        val state = SseLineStream.ConnectionState(1, contentType = "a", isAborted = false)
        val someUrl = "https://test2.com"
        val source = SseEventSource(someUrl, provider, withCredentials = true)
        provider.connectionState = state
        source.open()

        source.state
            .test {
                assertEquals(SseEventSource.State(), awaitItem())
                assertEquals(SseEventSource.State(statusCode = 1, ready = CLOSED), awaitItem())
            }

        assertTrue(provider.createVisited)
        assertEquals(provider.createVisitedWithUrl, SseUrl(someUrl))
        assertEquals(provider.createVisitedWithCredentials, true)
        assertEquals(provider.createVisitedWithLastEventId, null)
    }
}
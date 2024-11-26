package cc.scrambledbytes.sse.impl

import app.cash.turbine.test
import cc.scrambledbytes.sse.ReadyState.CLOSED
import cc.scrambledbytes.sse.SseEventSource
import cc.scrambledbytes.sse.SseLineStream
import cc.scrambledbytes.sse.SseUrl
import cc.scrambledbytes.sse.mock.FakeSseLineStreamProvider
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HandleConnectTest {

    lateinit var provider: FakeSseLineStreamProvider

    @BeforeTest
    fun setup() {
        provider = FakeSseLineStreamProvider()
    }

    @Test
    fun testOnOpenCreateStreamIsCalled() = runTest {
        val state = SseLineStream.ConnectionState(1, contentType = "a", isAborted = false)
        val someUrl = "https://test2.com"
        val source = SseEventSource(someUrl, provider, withCredentials = true)
        provider.connectionState = state
        source.open()

        source.state
            .test {
                assertEquals(SseEventSource.State(), awaitItem())
                assertEquals(SseEventSource.State(statusCode = 1, ready = CLOSED), awaitItem())
                cancelAndIgnoreRemainingEvents()
                assertTrue(provider.createVisited)
                assertEquals(provider.createVisitedWithUrl, SseUrl(someUrl))
                assertEquals(provider.createVisitedWithCredentials, true)
                assertEquals(provider.createVisitedWithLastEventId, null)
                cancelAndIgnoreRemainingEvents()
            }


    }
}
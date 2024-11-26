package cc.scrambledbytes.sse

import app.cash.turbine.test
import cc.scrambledbytes.sse.ReadyState.CLOSED
import cc.scrambledbytes.sse.ReadyState.CONNECTING
import cc.scrambledbytes.sse.mock.FakeSseLineStreamProvider
import cc.scrambledbytes.sse.util.launchOnDefault
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class SseEventSourceTests {

    lateinit var provider: FakeSseLineStreamProvider


    @BeforeTest
    fun setup() {
        println("Setup")
        provider = FakeSseLineStreamProvider()
    }

    @Test
    fun testCreateParsesUrl() {
        SseEventSource(VALID_URL, provider)
        assertTrue(provider.parseVisited)
        assertEquals(provider.parseVisitedWith, SseUrl(VALID_URL))
    }

    @Test
    fun testExecuteIsCalledOnOpen() = runTest(timeout = 1.minutes) {
        val source = SseEventSource(VALID_URL, provider)
        assertFalse(provider.onExecuteVisited)

        launchOnDefault{
            source.open()
        }


        source.state.test {
            assertEquals(actual = awaitItem().ready, expected = CONNECTING)
            assertEquals(actual = awaitItem().ready, expected = CLOSED) // no proper response code
            assertTrue(provider.onExecuteVisited)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testOnCloseIsCalledOnClose() =  runTest(timeout = 1.minutes) {
        val source = SseEventSource(VALID_URL, provider)
        assertFalse(provider.onCloseVisited, message = "On close should not be visited")
        launchOnDefault {
            source.open() // won't close
        }

        source.state.test {
            assertEquals(actual = awaitItem().ready, expected = CONNECTING)
            assertEquals(actual = awaitItem().ready, expected = CLOSED) // no proper response code
            assertTrue(provider.onExecuteVisited)
            source.close()
            cancelAndIgnoreRemainingEvents()
        }


    }

    // TODO state tests (state transitions)
    // TODO data tests
    // TODO test reconnection
}

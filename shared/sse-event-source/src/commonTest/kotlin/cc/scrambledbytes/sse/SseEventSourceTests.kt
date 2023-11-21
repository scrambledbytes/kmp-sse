package cc.scrambledbytes.sse

import app.cash.turbine.test
import cc.scrambledbytes.sse.ReadyState.CLOSED
import cc.scrambledbytes.sse.ReadyState.CONNECTING
import cc.scrambledbytes.sse.mock.FakeSseLineStreamProvider
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SseEventSourceTests {

    lateinit var provider: FakeSseLineStreamProvider

    @Before
    fun setup() {
        provider = FakeSseLineStreamProvider()
    }

    @Test
    fun testCreateParsesUrl() {
        SseEventSource(VALID_URL, provider)
        assertTrue(provider.parseVisited)
        assertEquals(provider.parseVisitedWith, SseUrl(VALID_URL))
    }

    @Test
    fun testExecuteIsCalledOnOpen(): Unit = runTest {
        val source = SseEventSource(VALID_URL, provider)
        assertFalse(provider.onExecuteVisited)
        source.open()
        source.state.test {
            assertEquals(actual = awaitItem().ready, expected = CONNECTING)
            assertEquals(actual = awaitItem().ready, expected = CLOSED) // no proper response code
            assertTrue(provider.onExecuteVisited)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testOnCloseIsCalledOnClose(): Unit = runTest {
        val source = SseEventSource(VALID_URL, provider)
        assertFalse(provider.onCloseVisited, message = "On close should not be visited")
        source.open() // won't close

        source.state.test {
            assertEquals(actual = awaitItem().ready, expected = CONNECTING)
            assertEquals(actual = awaitItem().ready, expected = CLOSED) // no proper response code
            assertTrue(provider.onExecuteVisited)
            source.close()
        }

        assertTrue(provider.onCloseVisited, message = "OnClose was not visited")
    }

    // TODO state tests (state transitions)
    // TODO data tests
    // TODO test reconnection
}

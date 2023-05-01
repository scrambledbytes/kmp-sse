package cc.scrambledbytes.sse

import cc.scrambledbytes.sse.mock.FakeSseLineStreamProvider
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

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
    fun testExecuteIsCalledOnOpen(): Unit = runBlocking {
        val source = SseEventSource(VALID_URL, provider)
        assertFalse(provider.onExecuteVisited)
        source.open()
        assertTrue(provider.onExecuteVisited)
    }

    @Test
    fun testOnCloseIsCalledOnClose(): Unit = runBlocking {
        val source = SseEventSource(VALID_URL, provider)
        assertFalse(provider.onCloseVisited)
        source.close()
        assertTrue(provider.onCloseVisited)
    }

    // TODO state tests
    // TODO data tests
}
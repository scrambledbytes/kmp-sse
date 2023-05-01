package cc.scrambledbytes.sse

import cc.scrambledbytes.sse.mock.FakeSseLineStreamProvider
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

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
}